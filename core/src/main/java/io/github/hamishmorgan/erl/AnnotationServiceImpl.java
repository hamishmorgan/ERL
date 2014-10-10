/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package io.github.hamishmorgan.erl;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.AnnotatorPool;
import edu.stanford.nlp.util.CoreMap;
import io.github.hamishmorgan.erl.snlp.annotations.EntityKbIdAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author hamish
 */
public class AnnotationServiceImpl implements AnnotationService {

    private static final boolean DEBUG = true;
    private static final Logger LOG = LoggerFactory.getLogger(AnnotationServiceImpl.class);

    private final AnnotatorPool pool;
    private JsonFactory jsonFactory;

    public AnnotationServiceImpl(AnnotatorPool pool, JsonFactory jsonFactory) {
        this.pool = pool;
        this.jsonFactory = jsonFactory;
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
    @Override
    @Nonnull
    public Annotation link(@Nonnull Annotation document) {
        checkNotNull(document, "document");

        final EnumSet<Annotations> requiredAnnotators = EnumSet.noneOf(Annotations.class);

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
                    containsNER = containsNER || t.containsKey(NamedEntityTagAnnotation.class);
                    containsPOS = containsPOS || t.containsKey(CoreAnnotations.PartOfSpeechAnnotation.class);
                    empty = false;
                }
            }

            if (!empty) {

                // If we don't find NEL tags then that's required
                if (!containsNEL)
                    requiredAnnotators.add(Annotations.ENTITY_LINKING);

                // IF NEL is required and that aren't NER tags...
                if (requiredAnnotators.contains(Annotations.ENTITY_LINKING) && !containsNER)
                    requiredAnnotators.add(Annotations.ENTITY_RECOGNITION);

                // If NER is required and that aren't POS tags
                if (requiredAnnotators.contains(Annotations.ENTITY_RECOGNITION) && !containsPOS)
                    requiredAnnotators.add(Annotations.POS_TAG);
            }
        } else {
            // There weren't any sentence so we need to do everything
            requiredAnnotators.addAll(EnumSet.of(Annotations.SENTENCE_SPLIT, Annotations.POS_TAG,
                    Annotations.ENTITY_LINKING, Annotations.ENTITY_RECOGNITION));
        }

        // If sentence splitting is required then check for document level tokens (not we don't need this otherwise,
        // and it may not be present on some de-serialized docs which only include the sentence level tokens.)
        if (requiredAnnotators.contains(Annotations.SENTENCE_SPLIT)
                && !document.containsKey(TokensAnnotation.class)) {
            // Check there is text to tokenize
            if (!document.containsKey(TextAnnotation.class))
                throw new IllegalArgumentException("Unable to tokenize document because it does not contain the " +
                        "required annotation: TextAnnotation");
            requiredAnnotators.add(Annotations.TOKENIZE);
        }

        LOG.debug("Addition processing steps required: " + requiredAnnotators.toString());

        final List<Annotations> tmp = Lists.newArrayList(requiredAnnotators);
        Collections.sort(tmp, Annotations.ORDER);
        final AnnotationPipeline pipeline = new AnnotationPipeline();
        for (Annotations a : tmp)
            pipeline.addAnnotator(pool.get(a.name()));
        pipeline.annotate(document);

        return document;
    }


    @Override
    @Nonnull
    public Annotation link(String text) {
        checkNotNull(text, "text");
        final Annotation document = new Annotation(text);
        link(document);
        return document;
    }

    @Override
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

    @Override
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

    @VisibleForTesting
    public void printAnnotationAsJson(@Nonnull Annotation document) throws IOException {
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

    @VisibleForTesting
    public void annotationToJson(@Nonnull Annotation document, Writer writer) throws IOException {
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

}
