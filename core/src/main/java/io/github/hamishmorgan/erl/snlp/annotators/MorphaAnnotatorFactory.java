/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package io.github.hamishmorgan.erl.snlp.annotators;

import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.MorphaAnnotator;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Properties;


public class MorphaAnnotatorFactory extends AbstractAnnotatorFactory implements Serializable {

    private static final long serialVersionUID = 1L;

    public MorphaAnnotatorFactory(Properties props) {
        super(props);
    }

    @Nonnull
    public Annotator create() {
        return new MorphaAnnotator(false);
    }

}
