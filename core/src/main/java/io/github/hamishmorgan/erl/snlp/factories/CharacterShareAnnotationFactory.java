package io.github.hamishmorgan.erl.snlp.factories;

import com.google.common.base.Preconditions;
import io.github.hamishmorgan.erl.snlp.annotators.CharacterShapeAnnotator;

import javax.annotation.Nonnull;
import java.util.Properties;

/**
 * Factory for {@code CharacterShapeAnnotator}, which is required by some components such as the
 * {@link edu.stanford.nlp.pipeline.AnnotatorPool}.
 */
public final class CharacterShareAnnotationFactory implements edu.stanford.nlp.util.Factory<CharacterShapeAnnotator> {

    private static final long serialVersionUID = 1L;

    private final Properties props;

    public CharacterShareAnnotationFactory() {
        this.props = new Properties();
    }

    public CharacterShareAnnotationFactory(Properties props) {
        Preconditions.checkNotNull(props, "props");
        this.props = props;
    }

    @Nonnull
    public CharacterShapeAnnotator create() {
        final CharacterShapeAnnotator csa = new CharacterShapeAnnotator();
        csa.configure(props);
        return csa;
    }
}
