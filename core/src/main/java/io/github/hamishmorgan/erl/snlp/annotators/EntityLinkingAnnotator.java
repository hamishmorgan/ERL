/*
 * Copyright (c) 2012-2013, Hamish Morgan.
 * All Rights Reserved.
 */
package io.github.hamishmorgan.erl.snlp.annotators;

import com.google.api.services.freebase.Freebase2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.CoreMap;
import io.github.hamishmorgan.erl.linker.*;
import io.github.hamishmorgan.erl.snlp.annotations.EntityKbIdAnnotation;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Hamish Morgan
 */
public class EntityLinkingAnnotator implements Annotator {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Freebase2.class);
    private final CandidateGenerator<String,String> generator;
    private final CandidateRanker<String,String> ranker;

    /**
     * @param generator
     * @param ranker
     */
    public EntityLinkingAnnotator(CandidateGenerator<String,String> generator, CandidateRanker<String,String> ranker) {
        this.generator = checkNotNull(generator, "generator");
        this.ranker = checkNotNull(ranker, "ranker");
    }

    /**
     * @return
     */
    public Set<Class<? extends CoreAnnotation<?>>> getRequiredAnnotations() {
        final Set<Class<? extends CoreAnnotation<?>>> requirements = Sets.newHashSet();
        requirements.add(TokensAnnotation.class);
        requirements.add(NamedEntityTagAnnotation.class);
        return requirements;
    }

    /**
     * Get a collection of all the annotation types that are produced by this annotator.
     * <p/>
     *
     * @return annotations produced by the annotator
     */
    @Nonnull
    public Set<Class<? extends CoreAnnotation<?>>> getSuppliedAnnotations() {
        return Collections.<Class<? extends CoreAnnotation<?>>>singleton(
                EntityKbIdAnnotation.class);
    }

    /**
     * @param document
     */
    @Override
    public void annotate(@Nonnull final Annotation document) {
        checkNotNull(document, "annotation");

        // Find all the entity mentions in the document
        final List<List<CoreLabel>> mentions = findMentions(document);

        // For each mention, create a query strings as the undering character sequence
        // covered by the tokens
        final Map<String, List<CoreLabel>> query2labels = Maps.newHashMap();

        for (final List<CoreLabel> phrase : mentions) {

            final String text = getSurfaceForm(document, phrase);

            // if map already contains the text then append the CoreLabels, otherwise create a 
            // new list there (don't use existing one since it's just a view
            if (query2labels.containsKey(text)) {
                query2labels.get(text).addAll(phrase);
            } else {
                query2labels.put(text, Lists.newArrayList(phrase));
            }
        }

        // Search the knowledge base with all of the unique query strings
        final Map<String, Set<String>> results;
        try {
            results = generator.batchFindCandidates(query2labels.keySet());


            // Now add the kb id as an annotation to each mention token
            for (final String query : query2labels.keySet()) {
                if (!results.containsKey(query)) {
                    LOG.warn("Failed to find search result for: " + query);
                    continue;
                }

                Set<String> unrankedCandidateIds = results.get(query);


                List<String> candidateIds = ranker.rankCandidates(query, unrankedCandidateIds);


                // TODO: Implement a more sensible method of handling NILs
                final String id = candidateIds.isEmpty() ? "/NIL" : candidateIds.get(0);

                for (final CoreLabel token : query2labels.get(query)) {
                    token.set(EntityKbIdAnnotation.class, id);
                }

            }

        } catch (IOException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
            throw new RuntimeException(ex);
        } catch (ExecutionException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
            throw new RuntimeException(ex);
        }

    }

    /**
     * Find all entity mentions in a document, and return them as a list. Each mentions consist of
     * one or more CoreLabel tokens, so a list of list is produced.
     *
     * @param document Annotated document to find mentions in
     * @return mentions
     */
    private List<List<CoreLabel>> findMentions(@Nonnull final Annotation document) {

        final List<List<CoreLabel>> mentions = Lists.newArrayList();
        for (final CoreMap sentence : document.get(SentencesAnnotation.class)) {
            final List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);

            // Search through the sentence finding contiguous sequences of tokens with the same
            // entity type label. If the label is not "O" (other) then add the sequence to the
            // mentions
            int first = 0;
            while (first < tokens.size()) {
                final String currentLabel = tokens.get(first).get(NamedEntityTagAnnotation.class);
                int last = first + 1;

                while (last < tokens.size() && tokens.get(last).get(NamedEntityTagAnnotation.class).equals(currentLabel)) {
                    last++;
                }

                if (!currentLabel.equals("O")) {
                    mentions.add(tokens.subList(first, last));
                }

                first = last;
            }
        }
        return mentions;
    }

    /**
     * Find the surface text covered by the mention phrase.
     *
     * @param document
     * @param mention
     * @return
     */
    private String getSurfaceForm(@Nonnull Annotation document, @Nonnull List<CoreLabel> mention) {
        checkNotNull(mention, "mention");

        final String documentText = document.get(CoreAnnotations.TextAnnotation.class);

        try {

            return documentText.substring(
                    mention.get(0).beginPosition(),
                    mention.get(mention.size() - 1).endPosition() + 1);

        } catch (IndexOutOfBoundsException ex) {
            // It's possible for the document text to be truncted, even though the annotation
            // is present. (I have no idea why.) In this case the above code through an exception,
            // so here we shall fall back on simply concatonating the mention text
            StringBuilder builder = new StringBuilder();
            boolean first = true;
            for (CoreLabel label : mention) {
                if (!first) {
                    builder.append(' ');
                }
                builder.append(label.get(CoreAnnotations.TextAnnotation.class));
                first = false;
            }

            return builder.toString();
        }
    }

}
