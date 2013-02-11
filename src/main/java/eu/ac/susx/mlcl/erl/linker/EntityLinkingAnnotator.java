/*
 * Copyright (c) 2012-2013, Hamish Morgan.
 * All Rights Reserved.
 */
package eu.ac.susx.mlcl.erl.linker;

import com.google.api.services.freebase.Freebase2;
import static com.google.common.base.Preconditions.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.CoreMap;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.slf4j.LoggerFactory;
import uk.ac.susx.mlcl.erl.MiscUtil;
import uk.ac.susx.mlcl.erl.snlp.AbstractAnnotatorFactory;

/**
 *
 * @author Hamish Morgan
 */
public class EntityLinkingAnnotator implements Annotator {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Freebase2.class);
    private final CandidateGenerator generator;
    private final CandidateRanker ranker;

    /**
     * 
     * @param generator
     * @param ranker 
     */
    public EntityLinkingAnnotator(CandidateGenerator generator, CandidateRanker ranker) {
        this.generator = checkNotNull(generator, "generator");
        this.ranker = checkNotNull(ranker, "ranker");
    }

    /**
     *
     * @return
     */
    public Set<Class<? extends CoreAnnotation<?>>> getRequiredAnnotations() {
        final Set<Class<? extends CoreAnnotation<?>>> requirements = new HashSet<>();
        requirements.add(TokensAnnotation.class);
        requirements.add(NamedEntityTagAnnotation.class);
        return requirements;
    }

    /**
     * Get a collection of all the annotation types that are produced by this annotator.
     * <p/>
     * @return annotations produced by the annotator
     */
    public Set<Class<? extends CoreAnnotation<?>>> getSuppliedAnnotations() {
        return Collections.<Class<? extends CoreAnnotation<?>>>singleton(
                EntityKbIdAnnotation.class);
    }

    /**
     *
     * @param annotation
     */
    @Override
    public void annotate(final Annotation document) {
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
        final Map<String, List<String>> results;
        try {
            results = generator.batchFindCandidates(query2labels.keySet());


            // Now add the kb id as an annotation to each mention token
            for (final String query : query2labels.keySet()) {
                if (!results.containsKey(query)) {
                    LOG.warn("Failed to find search result for: " + query);
                    continue;
                }

                List<String> candidateIds = results.get(query);


                candidateIds = ranker.ranked(candidateIds);


                // TODO: Implement a more sensible method of handling NILs
                final String id = candidateIds.isEmpty() ? "/NIL" : candidateIds.get(0);

                for (final CoreLabel token : query2labels.get(query)) {
                    token.set(EntityKbIdAnnotation.class, id);
                }

            }

        } catch (IOException | ExecutionException ex) {
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
    private List<List<CoreLabel>> findMentions(final Annotation document) {
        
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
    private String getSurfaceForm(Annotation document, List<CoreLabel> mention) {
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

    public static final class EntityKbIdAnnotation implements CoreAnnotation<String> {

        @Override
        public Class<String> getType() {
            return String.class;
        }
    }
    
    public static class Factory
            extends AbstractAnnotatorFactory
            implements edu.stanford.nlp.util.Factory<Annotator>, Serializable {

        private static final String PROPERTY_PREFIX = "nel.";
        private static final String GENERATOR_KEY = PROPERTY_PREFIX + "generator";
        private static final String GENERATOR_VALUE_FREEBASE_SEARCH = "freebase_search";
        private static final String GENERATOR_DEFAULT = GENERATOR_VALUE_FREEBASE_SEARCH;
        private static final String GENERATOR_CACHED_KEY = PROPERTY_PREFIX + "generator.cached";
        private static final String GENERATOR_CACHED_DEFAULT = "true";
        private static final String RANKER_KEY = PROPERTY_PREFIX + "ranker";
        private static final String RANKER_VALUE_NULL = "null";
        private static final String RANKER_VALUE_RANDOM = "random";
        private static final String RANKER_DEFAULT = RANKER_VALUE_NULL;
        private static final String RANKER_SEED_KEY = PROPERTY_PREFIX + RANKER_KEY + ".seed";
        private static final long serialVersionUID = 1L;

        public Factory(Properties props) {
            super(props);
        }

        public Factory() {
            super(new Properties());
        }

        @Override
        public Annotator create() {

            CandidateGenerator generator;
            switch (props.getProperty(GENERATOR_KEY, GENERATOR_DEFAULT).toLowerCase().trim()) {
                case GENERATOR_VALUE_FREEBASE_SEARCH:
                    Freebase2 fb;
                    try {
                        fb = MiscUtil.newFreebaseInstance();
                        generator = new FreebaseSearchGenerator(fb);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    break;
                default:
                    throw new RuntimeException("Unknown generator type: "
                            + props.getProperty(GENERATOR_KEY, GENERATOR_DEFAULT));
            }

            if (Boolean.valueOf(props.getProperty(GENERATOR_CACHED_KEY, GENERATOR_CACHED_DEFAULT))) {
                generator = CachedCandidateGenerator.wrap(generator);
            }

            final CandidateRanker ranker;
            switch (props.getProperty(RANKER_KEY, RANKER_DEFAULT).toLowerCase().trim()) {
                case RANKER_VALUE_NULL:
                    ranker = new NullRanker();
                    break;
                case RANKER_VALUE_RANDOM:
                    if (props.containsKey(RANKER_SEED_KEY)) {
                        final long seed = Long.parseLong(props.getProperty(RANKER_SEED_KEY));
                        ranker = new RandomRanker(new Random(seed));
                    } else {
                        ranker = new RandomRanker();
                    }

                    break;
                default:
                    throw new RuntimeException("Unknown ranker type: "
                            + props.getProperty(RANKER_KEY, RANKER_DEFAULT));
            }

            return new EntityLinkingAnnotator(generator, ranker);
        }
    }
}
