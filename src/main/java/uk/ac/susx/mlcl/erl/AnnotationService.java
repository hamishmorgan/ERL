/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl;

import com.beust.jcommander.internal.Lists;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.AnnotatorPool;
import edu.stanford.nlp.util.CoreMap;
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
import uk.ac.susx.mlcl.erl.linker.EntityLinkingAnnotator;
import uk.ac.susx.mlcl.erl.linker.EntityLinkingAnnotator.EntityKbIdAnnotation;
import uk.ac.susx.mlcl.erl.snlp.*;
import uk.ac.susx.mlcl.erl.xml.AnnotationToXML;
import uk.ac.susx.mlcl.erl.xml.XMLToStringSerializer;
import uk.ac.susx.mlcl.erl.xml.XomB;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @author hamish
 */
public class AnnotationService {

    private static final boolean DEBUG = true;
    private static final Logger LOG = LoggerFactory.getLogger(
            AnnotationService.class);
    //
//    private static final Charset CHARSET = Charset.forName("UTF-8");
    private final XomB xomb;
    private final AnnotationToXML xmler;
    private AnnotatorPool pool;
    private JsonFactory jsonFactory;

    public AnnotationService(AnnotatorPool pool, AnnotationToXML xmler, XomB xomb,
                             JsonFactory jsonFactory) {
        this.pool = pool;
        this.xmler = xmler;
        this.xomb = xomb;
        this.jsonFactory = jsonFactory;
    }

    public static AnnotationService newInstance(Properties props)
            throws ClassNotFoundException, InstantiationException, ConfigurationException, IllegalAccessException {

        AnnotatorPool pool = new AnnotatorPool();
        pool.register("tokenize", new TokenizerAnnotatorFactory(props));
        pool.register("cleanXml", new CleanXmlAnnotator2.Factory(props));
        pool.register("ssplit", new SentenceSplitAnnotatorFactory(props));
        pool.register("lemma", new MorphaAnnotatorFactory(props));
        pool.register("pos", new POSTaggerAnnotatorFactory(props));
        pool.register("parse", new ParserAnnotatorFactory(props));
        pool.register("ner", new NERAnnotatorFactory(props));
        pool.register("coref", new CorefAnnotatorFactory(props));
        pool.register("el", new EntityLinkingAnnotator.Factory(props));


        AnnotationToXML.Builder builder = AnnotationToXML.builder();
        builder.configure(new PropertiesConfiguration("sussexXml.properties"));
//            builder.addAnnotationToIgnore(
//                    CoreAnnotations.XmlContextAnnotation.class);
//            builder.addSimplifiedName(
//                    EntityLinkingAnnotator.EntityKbIdAnnotation.class, "link");

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
    public Annotation link(Annotation document) {
        Preconditions.checkNotNull(document, "document");

        final AnnotationPipeline pipeline = new AnnotationPipeline();

        final boolean tokenizeRequired;
        final boolean splitRequired;
        final boolean posRequired;
        final boolean nerRequired;
        final boolean nelRequired;

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

            if (empty) {
                // Don't need to do anything because there aren't any sentences
                posRequired = false;
                nerRequired = false;
                nelRequired = false;
            } else {

                // If we don't find NEL tags then that's required
                nelRequired = !containsNEL;
                // IF NEL is required and that aren't NER tags...
                nerRequired = nelRequired && !containsNER;
                // If NER is required and that aren't POS tags
                posRequired = nerRequired && !containsPOS;
            }

            splitRequired = false;
        } else {
            // There weren't any sentence so we need to do everything
            splitRequired = true;
            posRequired = true;
            nerRequired = true;
            nelRequired = true;
        }

        // If sentence splitting is required then check for document level tokens (not we don't need this otherwise,
        // and it may not be present on some de-serialized docs which only include the sentence level tokens.)
        if (splitRequired && !document.containsKey(CoreAnnotations.TokensAnnotation.class)) {
            // Check there is text to tokenize
            if (!document.containsKey(CoreAnnotations.TextAnnotation.class))
                throw new IllegalArgumentException("Unable to tokenize document because it does not contain the " +
                        "required annotation: TextAnnotation");
            tokenizeRequired = true;
        } else {
            tokenizeRequired = false;
        }


        LOG.debug("Addition processing steps required: "
                + (tokenizeRequired ? "tokenize " : "")
                + (splitRequired ? "ssplit " : "")
                + (posRequired ? "pos " : "")
                + (nerRequired ? "ner " : "")
                + (nelRequired ? "el " : ""));
        // Add the annotators in the correct order
        if (tokenizeRequired)
            pipeline.addAnnotator(pool.get("tokenize"));
        if (splitRequired)
            pipeline.addAnnotator(pool.get("ssplit"));
        if (posRequired)
            pipeline.addAnnotator(pool.get("pos"));
        if (nerRequired)
            pipeline.addAnnotator(pool.get("ner"));
        if (nelRequired)
            pipeline.addAnnotator(pool.get("el"));

        pipeline.annotate(document);

        return document;
    }


    public Annotation link(String text) {
        Preconditions.checkNotNull(text, "text");
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
        Preconditions.checkNotNull(text);
        Preconditions.checkNotNull(output);
        Writer writer = new BufferedWriter(
                new OutputStreamWriter(output, charset));
        linkAsJson(text, writer);
        writer.flush();
    }

    public void linkAsJson(String text, Writer writer) throws IOException {
        Preconditions.checkNotNull(text);
        Preconditions.checkNotNull(writer);

        final Annotation document = link(text);
        annotationToJson(document, writer);
    }

    String annotationToJson(Annotation document) throws IOException {
        final StringWriter writer = new StringWriter();
        annotationToJson(document, writer);
        return writer.toString();
    }


    void printAnnotationAsJson(Annotation document) throws IOException {
        final PrintWriter writer = new PrintWriter(System.out);
        annotationToJson(document, writer);
        writer.flush();
    }


    private List<List<CoreLabel>> getEntityChunks(Annotation document) {
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
                if(!chunk.isEmpty())
                    chunks.add(chunk);
                chunk = Lists.newArrayList();
            }

            // add current token
            chunk.add(nextToken);
            prevId = nextId;
            prevType = nextType;
        }

        if(!chunk.isEmpty())
            chunks.add(chunk);
        return chunks;
    }

    private void writeEntityChunks(JsonGenerator generator, Annotation document,  List<List<CoreLabel>> chunks ) throws IOException {
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

    void annotationToJson(Annotation document, Writer writer) throws IOException {
        final  JsonGenerator generator = jsonFactory.createJsonGenerator(writer);
        generator.enablePrettyPrint();
        generator.writeStartArray();

        final List<List<CoreLabel>> chunks = getEntityChunks(document);
        writeEntityChunks(generator, document, chunks);

        generator.writeEndArray();
        generator.flush();
    }

    private void writeJsonObj(JsonGenerator generator, String text,
                              String entityId, String entityType)
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

    public void linkAsXml(String text, OutputStream out, Charset charset)
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
        Document htmlDoc = xomb.document().setDocType("html")
                .setRoot((Element) nodes.get(0)).build();

        return htmlDoc;
    }

    public void linkAsHtml(String text, OutputStream out, Charset charset)
            throws InstantiationException, IOException, XSLException, ParsingException {
        final Document xml = linkAHtml(text);
        writeXml(xml, out, charset, true);
    }

    private static void writeXml(Document document,
                                 OutputStream out, Charset charset,
                                 boolean decSkip)
            throws IOException {
        XMLToStringSerializer sr = new XMLToStringSerializer(
                out, charset.name());
        sr.setXmlDeclarationSkipped(decSkip);
        sr.setIndent(2);
        sr.write(document);
        sr.flush();
    }

    private static String xmlEncode(String input) {
        Preconditions.checkNotNull(input, "input");

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
}
