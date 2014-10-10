/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package io.github.hamishmorgan.erl.snlp.annotators;

import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.DefaultPaths;
import edu.stanford.nlp.pipeline.GenderAnnotator;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Properties;


public class GenderAnnotatorFactory extends AbstractAnnotatorFactory implements Serializable {

    private static final long serialVersionUID = 1L;

    public GenderAnnotatorFactory(Properties props) {
        super(props);
    }

    @Nonnull
    public Annotator create() {
        return new GenderAnnotator(false,
                props.getProperty("gender.firstnames",
                        DefaultPaths.DEFAULT_GENDER_FIRST_NAMES));
    }

}