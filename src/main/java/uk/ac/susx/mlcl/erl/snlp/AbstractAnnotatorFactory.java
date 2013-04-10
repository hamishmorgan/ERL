/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.snlp;

import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.Factory;

import java.util.Properties;

/**
 * @author hamish
 */
public abstract class AbstractAnnotatorFactory implements Factory<Annotator> {

    protected Properties props;

    public AbstractAnnotatorFactory(Properties props) {
        this.props = props;
    }

}
