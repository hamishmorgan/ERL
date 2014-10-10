package io.github.hamishmorgan.erl.snlp.annotations;

import edu.stanford.nlp.ling.CoreAnnotation;

import javax.annotation.Nonnull;

/**
* Created by hamish on 10/10/14.
*/
public final class EntityKbIdAnnotation implements CoreAnnotation<String> {

    @Nonnull
    @Override
    public Class<String> getType() {
        return String.class;
    }
}
