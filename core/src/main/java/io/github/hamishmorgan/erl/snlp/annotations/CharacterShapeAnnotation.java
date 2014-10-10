package io.github.hamishmorgan.erl.snlp.annotations;

import edu.stanford.nlp.ling.CoreAnnotation;

import javax.annotation.Nonnull;

/**
 * The CoreMap key for getting the character shape strings contained by an annotation.
 * <p/>
 * This key is typically set only on token annotations.
 */
public final class CharacterShapeAnnotation implements CoreAnnotation<String> {

    @Nonnull
    public Class<String> getType() {
        return String.class;
    }
}
