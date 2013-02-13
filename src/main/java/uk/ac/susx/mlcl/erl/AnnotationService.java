/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.AnnotatorPool;
import eu.ac.susx.mlcl.erl.linker.EntityLinkingAnnotator;
import eu.ac.susx.mlcl.erl.linker.EntityLinkingAnnotator.EntityKbIdAnnotation;
import eu.ac.susx.mlcl.erl.xml.AnnotationToXML;
import eu.ac.susx.mlcl.erl.xml.XMLToStringSerializer;
import eu.ac.susx.mlcl.erl.xml.XomB;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
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
import uk.ac.susx.mlcl.erl.snlp.CleanXmlAnnotator2;
import uk.ac.susx.mlcl.erl.snlp.CorefAnnotatorFactory;
import uk.ac.susx.mlcl.erl.snlp.MorphaAnnotatorFactory;
import uk.ac.susx.mlcl.erl.snlp.NERAnnotatorFactory;
import uk.ac.susx.mlcl.erl.snlp.POSTaggerAnnotatorFactory;
import uk.ac.susx.mlcl.erl.snlp.ParserAnnotatorFactory;
import uk.ac.susx.mlcl.erl.snlp.SentenceSplitAnnotatorFactory;
import uk.ac.susx.mlcl.erl.snlp.TokenizerAnnotatorFactory;

/**
 *
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

    public static AnnotationService newInstance(Properties props) throws ClassNotFoundException, InstantiationException, ConfigurationException, IllegalAccessException {

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
     *
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

    public Annotation link(String text) {
        Preconditions.checkNotNull(text, "text");
        final Annotation document = new Annotation(text);

        AnnotationPipeline pipeline = new AnnotationPipeline();
        pipeline.addAnnotator(pool.get("tokenize"));
        pipeline.addAnnotator(pool.get("ssplit"));
        pipeline.addAnnotator(pool.get("pos"));
        pipeline.addAnnotator(pool.get("ner"));
        pipeline.addAnnotator(pool.get("el"));

        pipeline.annotate(document);

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

    private void annotationToJson(Annotation document, Writer writer) throws IOException {
        JsonGenerator generator = jsonFactory.createJsonGenerator(writer);
        generator.enablePrettyPrint();

        generator.writeStartArray();

        final String documentText = document.get(TextAnnotation.class);
        List<CoreLabel> tokens = document.get(TokensAnnotation.class);

        Iterator<CoreLabel> it = tokens.iterator();

        int sequenceStart = 0;
        int sequenceEnd = 0;
        String currentEntityId = null;
        String currentEntityType = null;

        while (it.hasNext()) {

            final CoreLabel token = it.next();

            final String nextEntityId =
                    token.containsKey(EntityKbIdAnnotation.class)
                    ? token.get(EntityKbIdAnnotation.class)
                    : null;

            final String nextEntityType =
                    token.containsKey(NamedEntityTagAnnotation.class)
                    ? token.get(NamedEntityTagAnnotation.class)
                    : null;

            final boolean newSequence =
                    (currentEntityId == null
                     ? currentEntityId != nextEntityId
                     : !currentEntityId.equals(nextEntityId))
                    || (currentEntityType == null
                        ? currentEntityType != nextEntityType
                        : !currentEntityType.equals(nextEntityType));

            final boolean flushBuilder =
                    (newSequence || !it.hasNext()) && sequenceStart < sequenceEnd;


            if (!newSequence) {
                sequenceEnd = token.endPosition();
            }

            // new entity type so flush the old on and start over
            if (flushBuilder) {
                String seq = documentText.substring(sequenceStart, sequenceEnd);
                writeJsonObj(generator, seq, currentEntityId, currentEntityType);
                sequenceStart = sequenceEnd;

            }

            if (newSequence) {

                // If there is space between the start of the next token and the end 
                // of the current token then we need to produce a extra json object for
                // that data
                if (token.beginPosition() > sequenceEnd) {
                    writeJsonObj(generator, documentText.substring(
                            sequenceEnd, token.beginPosition()), null, null);
                }

                sequenceStart = token.beginPosition();
                sequenceEnd = token.endPosition();

                currentEntityId = nextEntityId;
                currentEntityType = nextEntityType;
            }
        }

        if (sequenceEnd < documentText.length()) {
            writeJsonObj(generator, documentText.substring(
                    sequenceEnd, documentText.length()), null, null);
        }


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
