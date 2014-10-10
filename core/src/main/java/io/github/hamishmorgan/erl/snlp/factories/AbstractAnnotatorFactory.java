/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package io.github.hamishmorgan.erl.snlp.factories;

import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.Factory;

import java.util.Properties;


public abstract class AbstractAnnotatorFactory implements Factory<Annotator> {

    protected final Properties props;

    public AbstractAnnotatorFactory(Properties props) {
        this.props = props;
    }

}
