/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package io.github.hamishmorgan.erl.snlp.annotators;

import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.DeterministicCorefAnnotator;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Properties;


public class CorefAnnotatorFactory extends AbstractAnnotatorFactory implements Serializable {

    private static final long serialVersionUID = 1L;

    public CorefAnnotatorFactory(Properties props) {
        super(props);
    }

    @Nonnull
    public Annotator create() {
        return new DeterministicCorefAnnotator(props);
    }

}
