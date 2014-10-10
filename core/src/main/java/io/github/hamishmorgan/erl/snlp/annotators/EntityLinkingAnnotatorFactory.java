package io.github.hamishmorgan.erl.snlp.annotators;

import com.google.api.services.freebase.Freebase2;
import edu.stanford.nlp.pipeline.Annotator;
import io.github.hamishmorgan.erl.FreebaseInstanceSupplier;
import io.github.hamishmorgan.erl.linker.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;
import java.util.Random;

/**
* Created by hamish on 10/10/14.
*/
public class EntityLinkingAnnotatorFactory
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

    FreebaseInstanceSupplier freebaseInstanceSupplier = new FreebaseInstanceSupplier();

    public EntityLinkingAnnotatorFactory(Properties props) {
        super(props);
    }

    public EntityLinkingAnnotatorFactory() {
        super(new Properties());
    }

    @Nonnull
    @Override
    public Annotator create() {

        CandidateGenerator generator;

        final String gen = props.getProperty(GENERATOR_KEY, GENERATOR_DEFAULT).toLowerCase().trim();
        if (gen.equals(GENERATOR_VALUE_FREEBASE_SEARCH)) {
            Freebase2 fb;

            fb = freebaseInstanceSupplier.get();
            generator = new FreebaseSearchGenerator(fb);

        } else {
            throw new RuntimeException("Unknown generator type: "
                    + props.getProperty(GENERATOR_KEY, GENERATOR_DEFAULT));
        }

        if (Boolean.valueOf(props.getProperty(GENERATOR_CACHED_KEY, GENERATOR_CACHED_DEFAULT))) {
            generator = CachedCandidateGenerator.wrap(generator);
        }

        final CandidateRanker ranker;
        final String rnkr = props.getProperty(RANKER_KEY, RANKER_DEFAULT).toLowerCase().trim();
        if (rnkr.equals(RANKER_VALUE_NULL)) {
            ranker = new NullRanker();
        } else if (rnkr.equals(RANKER_VALUE_RANDOM)) {
            if (props.containsKey(RANKER_SEED_KEY)) {
                final long seed = Long.parseLong(props.getProperty(RANKER_SEED_KEY));
                ranker = new RandomRanker(new Random(seed));
            } else {
                ranker = new RandomRanker();
            }

        } else {
            throw new RuntimeException("Unknown ranker type: "
                    + props.getProperty(RANKER_KEY, RANKER_DEFAULT));
        }

        return new EntityLinkingAnnotator(generator, ranker);
    }
}
