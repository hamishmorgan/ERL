package io.github.hamishmorgan.erl;

import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.AnnotatorPool;
import edu.stanford.nlp.util.Factory;
import io.github.hamishmorgan.erl.snlp.factories.*;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
public enum Annotations {

    TOKENIZE(TokenizerAnnotatorFactory.class, 0),
    CLEAN_XML(CleanXmlAnnotator2Factory.class, 1),
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
    public static final Comparator<Annotations> ORDER = new Comparator<Annotations>() {
        @Override
        public int compare(@Nonnull Annotations o1, @Nonnull Annotations o2) {
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

    private Annotations(final Class<? extends Factory<Annotator>> factoryClass, final int order) {
        this.factoryClass = checkNotNull(factoryClass);
        this.order = order;

    }

    @Nonnull
    public static AnnotatorPool createPool(final Properties props) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        final AnnotatorPool pool = new AnnotatorPool();
        for (Annotations annotator : Annotations.values())
            pool.register(annotator.name(), annotator.newFactory(props));
        return pool;
    }

    public Factory<Annotator> newFactory(final Properties props) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {
        return factoryClass.getConstructor(Properties.class).newInstance(props);
    }
}
