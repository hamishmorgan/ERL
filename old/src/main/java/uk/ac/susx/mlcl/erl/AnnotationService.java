/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl;

import com.beust.jcommander.internal.Lists;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.jackson.JacksonFactory;
import static com.google.common.base.Preconditions.*;
import com.google.common.base.Stopwatch;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.AnnotatorPool;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Factory;
import io.github.hamishmorgan.erl.snlp.annotators.*;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.xslt.XSLException;
import nu.xom.xslt.XSLTransform;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.hamishmorgan.erl.snlp.annotations.EntityKbIdAnnotation;
import io.github.hamishmorgan.erl.snlp.AnnotationToXML;
import uk.ac.susx.mlcl.lib.xml.XMLToStringSerializer;
import uk.ac.susx.mlcl.lib.xml.XomB;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author hamish
 */
public class AnnotationService {

    private static final boolean DEBUG = true;
    private static final Logger LOG = LoggerFactory.getLogger(AnnotationService.class);

    private final XomB xomb;
    private final AnnotationToXML xmler;
    private final AnnotatorPool pool;
    private JsonFactory jsonFactory;

    public AnnotationService(AnnotatorPool pool, AnnotationToXML xmler, XomB xomb,
                             JsonFactory jsonFactory) {
        this.pool = pool;
        this.xmler = xmler;
        this.xomb = xomb;
        this.jsonFactory = jsonFactory;
    }

    @Nonnull
    public static AnnotationService newInstance(Properties props)
            throws ClassNotFoundException, InstantiationException, ConfigurationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        AnnotatorPool pool = Anno.createPool(props);

        AnnotationToXML.Builder builder = AnnotationToXML.builder();
        builder.configure(new PropertiesConfiguration("sussexXml.properties"));

        AnnotationToXML xmler = builder.build();

        XomB xomb = new XomB();

        JsonFactory jsonFactory = new JacksonFactory();

        return new AnnotationService(pool, xmler, xomb, jsonFactory);
    }

    /**
     * Pre-load models and annotators required for entity linking.
     * <p/>
     * The annotation models take a while to load (about 20 seconds currently), which cause an
     * irritating pause the first time the service is used. To avoid this, and to save time,
     * pre-load the annotators in the background by requesting a dummy link request.
     *
     * @param block whether the call should wait until loading is completed before returning
     * @throws InterruptedException when block is true, and the worker thread is interrupted.
     */
    public void preloadLinker(boolean block) throws InterruptedException {

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                LOG.debug("Starting pre-load of link annotors in background.");
                Stopwatch stopwatch = new Stopwatch().start();

                link("");

                LOG.debug("Loaded link annotors. (Elapsed time : {})",
                        stopwatch.stop());
            }
        }, "annotator-preloader");
        try {
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    LOG.warn("Annotator preloader encountered a problem.", e);
                }
            });
        } catch (SecurityException ex) {
            LOG.warn(ex.getLocalizedMessage(), ex);
        }
        thread.start();

        if (block) {
            thread.join();
        }
    }

    /**
     * @param document
     * @return
     */
    @Nonnull
    public Annotation link(@Nonnull Annotation document) {
        checkNotNull(document, "document");

        final EnumSet<Anno> requiredAnnotators = EnumSet.noneOf(Anno.class);

        // First check for NEL tags, but to do that we need to find and iterate sentences
        if (document.containsKey(CoreAnnotations.SentencesAnnotation.class)) {
            // The document has sentences... so that's a start

            final List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);


            boolean empty = true;
            boolean containsNEL = false;
            boolean containsNER = false;
            boolean containsPOS = false;

            for (CoreMap s : sentences) {
                List<CoreLabel> tokens = s.get(TokensAnnotation.class);
                for (CoreLabel t : tokens) {
                    containsNEL = containsNEL || t.containsKey(EntityKbIdAnnotation.class);
                    containsNER = containsNER || t.containsKey(CoreAnnotations.NamedEntityTagAnnotation.class);
                    containsPOS = containsPOS || t.containsKey(CoreAnnotations.PartOfSpeechAnnotation.class);
                    empty = false;
                }
            }

            if (!empty) {

                // If we don't find NEL tags then that's required
                if(!containsNEL)
                    requiredAnnotators.add(Anno.ENTITY_LINKING);

                // IF NEL is required and that aren't NER tags...
                if(requiredAnnotators.contains(Anno.ENTITY_LINKING) && !containsNER)
                    requiredAnnotators.add(Anno.ENTITY_RECOGNITION);

                // If NER is required and that aren't POS tags
                if(requiredAnnotators.contains(Anno.ENTITY_RECOGNITION) && !containsPOS)
                    requiredAnnotators.add(Anno.POS_TAG);
            }
        } else {
            // There weren't any sentence so we need to do everything
            requiredAnnotators.addAll(EnumSet.of(Anno.SENTENCE_SPLIT, Anno.POS_TAG,
                    Anno.ENTITY_LINKING, Anno.ENTITY_RECOGNITION));
        }

        // If sentence splitting is required then check for document level tokens (not we don't need this otherwise,
        // and it may not be present on some de-serialized docs which only include the sentence level tokens.)
        if (requiredAnnotators.contains(Anno.SENTENCE_SPLIT)
                && !document.containsKey(CoreAnnotations.TokensAnnotation.class)) {
            // Check there is text to tokenize
            if (!document.containsKey(CoreAnnotations.TextAnnotation.class))
                throw new IllegalArgumentException("Unable to tokenize document because it does not contain the " +
                        "required annotation: TextAnnotation");
            requiredAnnotators.add(Anno.TOKENIZE);
        }

        LOG.debug("Addition processing steps required: " + requiredAnnotators.toString());

        final List<Anno> tmp = Lists.newArrayList(requiredAnnotators);
        Collections.sort(tmp, Anno.ORDER);
        final AnnotationPipeline pipeline = new AnnotationPipeline();
        for(Anno a : tmp)
            pipeline.addAnnotator(pool.get(a.name()));
        pipeline.annotate(document);

        return document;
    }


    @Nonnull
    public Annotation link(String text) {
        checkNotNull(text, "text");
        final Annotation document = new Annotation(text);
        link(document);
        return document;
    }

    public String linkAsJson(String text) {
        try {
            StringWriter writer = new StringWriter();
            linkAsJson(text, writer);
            return writer.toString();
        } catch (IOException ex) {
            // StringWriter should never throw IOException
            throw new AssertionError(ex);
        }
    }

    public void linkAsJson(String text, OutputStream output, Charset charset) throws IOException {
        checkNotNull(text);
        checkNotNull(output);
        Writer writer = new BufferedWriter(
                new OutputStreamWriter(output, charset));
        linkAsJson(text, writer);
        writer.flush();
    }

    public void linkAsJson(String text, Writer writer) throws IOException {
        checkNotNull(text);
        checkNotNull(writer);

        final Annotation document = link(text);
        annotationToJson(document, writer);
    }

    public String annotationToJson(@Nonnull Annotation document) throws IOException {
        final StringWriter writer = new StringWriter();
        annotationToJson(document, writer);
        return writer.toString();
    }


    void printAnnotationAsJson(@Nonnull Annotation document) throws IOException {
        final PrintWriter writer = new PrintWriter(System.out);
        annotationToJson(document, writer);
        writer.flush();
    }


    private List<List<CoreLabel>> getEntityChunks(@Nonnull Annotation document) {
        final List<List<CoreLabel>> chunks = Lists.newArrayList();
        final List<CoreLabel> tokens = document.get(TokensAnnotation.class);
        final Iterator<CoreLabel> it = tokens.iterator();
        String prevId = null;
        String prevType = null;
        List<CoreLabel> chunk = Lists.newArrayList();
        while (it.hasNext()) {
            final CoreLabel nextToken = it.next();
            final String nextId = nextToken.get(EntityKbIdAnnotation.class);
            final String nextType = nextToken.get(NamedEntityTagAnnotation.class);

            final boolean entityChanged
                    = ((prevId != nextId) || ((prevId != null) && !prevId.equals(nextId)))
                    || ((prevType != nextType) || ((null != nextType) && !prevType.equals(nextType)));

            if (entityChanged) {
                if (!chunk.isEmpty())
                    chunks.add(chunk);
                chunk = Lists.newArrayList();
            }

            // add current token
            chunk.add(nextToken);
            prevId = nextId;
            prevType = nextType;
        }

        if (!chunk.isEmpty())
            chunks.add(chunk);
        return chunks;
    }

    private void writeEntityChunks(@Nonnull JsonGenerator generator, @Nonnull Annotation document, @Nonnull List<List<CoreLabel>> chunks) throws IOException {
        final String documentText = document.get(TextAnnotation.class);

        int prevEnd = 0;  // The index of the last character in the sequence
        for (List<CoreLabel> chunk : chunks) {
            if (chunk.isEmpty())
                continue;

            final int start = chunk.get(0).beginPosition();
            final int end = chunk.get(chunk.size() - 1).endPosition();
            final String seq = documentText.substring(start, end);
            final String id = chunk.get(0).get(EntityKbIdAnnotation.class);
            final String type = chunk.get(0).get(NamedEntityTagAnnotation.class);

            if (start > prevEnd) {
                writeJsonObj(generator, documentText.substring(prevEnd, start), null, null);
            }

            writeJsonObj(generator, seq, id, type);
            prevEnd = end;
        }

        if (prevEnd < documentText.length()) {
            writeJsonObj(generator, documentText.substring(prevEnd, documentText.length()), null, null);
        }
    }

    void annotationToJson(@Nonnull Annotation document, Writer writer) throws IOException {
        final JsonGenerator generator = jsonFactory.createJsonGenerator(writer);
        generator.enablePrettyPrint();
        generator.writeStartArray();

        final List<List<CoreLabel>> chunks = getEntityChunks(document);
        writeEntityChunks(generator, document, chunks);

        generator.writeEndArray();
        generator.flush();
    }

    private void writeJsonObj(@Nonnull JsonGenerator generator, @Nonnull String text,
                              @Nullable String entityId, @Nullable String entityType)
            throws IOException {

        generator.writeStartObject();
        generator.writeFieldName("text");
        generator.writeString(xmlEncode(text));

        if (entityId != null) {
            generator.writeFieldName("id");
            generator.writeString(entityId);
            generator.writeFieldName("url");
            generator.writeString("http://www.freebase.com/view" + entityId);
        }

        if (entityType != null) {
            generator.writeFieldName("type");
            generator.writeString(entityType);
        }
        generator.writeEndObject();
    }

    public Document linkAsXml(String text)
            throws InstantiationException {
        final Annotation document = link(text);
        return xmler.toDocument(document);
    }

    public void linkAsXml(String text, OutputStream out, @Nonnull Charset charset)
            throws InstantiationException, IOException {
        final Document xml = linkAsXml(text);
        writeXml(xml, out, charset, false);
    }

    public Document linkAHtml(String text) throws InstantiationException,
            IOException, XSLException, ParsingException {
        final Document xml = linkAsXml(text);
        XSLTransform transform = new XSLTransform(new nu.xom.Builder()
                .build(new File(
                        "src/main/resources/CoreNLP-to-HTML_2.xsl")));

        Nodes nodes = transform.transform(xml);
        return xomb.document().setDocType("html")
                .setRoot((Element) nodes.get(0)).build();
    }

    public void linkAsHtml(String text, OutputStream out, @Nonnull Charset charset)
            throws InstantiationException, IOException, XSLException, ParsingException {
        final Document xml = linkAHtml(text);
        writeXml(xml, out, charset, true);
    }

    private static void writeXml(Document document,
                                 OutputStream out, @Nonnull Charset charset,
                                 boolean decSkip)
            throws IOException {
        XMLToStringSerializer sr = new XMLToStringSerializer(
                out, charset.name());
        sr.setXmlDeclarationSkipped(decSkip);
        sr.setIndent(2);
        sr.write(document);
        sr.flush();
    }

    @Nonnull
    private static String xmlEncode(@Nonnull String input) {
        checkNotNull(input, "input");

        // Search through the string for the first character the requires encoding
        int i = 0;
        searching:
        while (i < input.length()) {
            switch (input.charAt(i)) {
                case '<':
                case '>':
                case '&':
                    break searching;
                default:
                    i++;
            }
        }

        // If no encoding is required just return the input string unmodified
        if (i == input.length()) {
            return input;
        }

        // Encoding required, write everything to a builder replacing special
        // characters with their escape sequences.
        final StringBuilder output = new StringBuilder();
        output.append(input, 0, i);
        while (i < input.length()) {
            switch (input.charAt(i)) {
                case '<':
                    output.append("&lt;");
                    break;
                case '>':
                    output.append("&gt;");
                    break;
                case '&':
                    output.append("&amp;");
                    break;
                default:
                    output.append(input.charAt(i));
            }
            i++;
        }

        return output.toString();
    }

    /**
     *
     */
    private enum Anno {
        TOKENIZE(TokenizerAnnotatorFactory.class, 0),
        CLEAN_XML(CleanXmlAnnotator2.Factory.class, 1),
        SENTENCE_SPLIT(SentenceSplitAnnotatorFactory.class, 2),
        LEMMATIZE(MorphaAnnotatorFactory.class, 3),
        POS_TAG(POSTaggerAnnotatorFactory.class, 4),
        PARSE(ParserAnnotatorFactory.class, 5),
        ENTITY_RECOGNITION(NERAnnotatorFactory.class, 6),
        COREFERRENCE_RESOLUTION(CorefAnnotatorFactory.class, 7),
        ENTITY_LINKING(EntityLinkingAnnotatorFactory.class, 8);
        /**
         *
         */
        public static final Comparator<Anno> ORDER = new Comparator<Anno>() {
            @Override
            public int compare(@Nonnull Anno o1, @Nonnull Anno o2) {
                return o1.order - o2.order;
            }
        };
        /**
         *
         */
        private final Class<? extends Factory<Annotator>> factoryClass;
        /**
         *
         */
        private final int order;

        private Anno(final Class<? extends Factory<Annotator>> factoryClass, final int order) {
            this.factoryClass = checkNotNull(factoryClass);
            this.order = order;

        }

        @Nonnull
        static AnnotatorPool createPool(final Properties props) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
            final AnnotatorPool pool = new AnnotatorPool();
            for (Anno annotator : Anno.values())
                pool.register(annotator.name(), annotator.newFactory(props));
            return pool;
        }

        public Factory<Annotator> newFactory(final Properties props) throws NoSuchMethodException,
                IllegalAccessException, InvocationTargetException, InstantiationException {
            return factoryClass.getConstructor(Properties.class).newInstance(props);
        }
    }
}
