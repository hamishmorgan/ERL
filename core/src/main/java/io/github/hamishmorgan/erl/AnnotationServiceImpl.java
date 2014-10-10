/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package io.github.hamishmorgan.erl;

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
import java.io.*;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author hamish
 */
public class AnnotationServiceImpl implements AnnotationService {

    private static final boolean DEBUG = true;
    private static final Logger LOG = LoggerFactory.getLogger(AnnotationServiceImpl.class);

    private final AnnotatorPool pool;
    private JsonUtil  jsonUtil;


    public AnnotationServiceImpl(AnnotatorPool pool, JsonUtil jsonUtil) {
        this.pool = pool;
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

    @Deprecated
    public void linkAsJson(String text, Writer writer) throws IOException {
        checkNotNull(text);
        checkNotNull(writer);

        final Annotation document = link(text);
        jsonUtil.annotationToJson(document, writer);
    }


}
