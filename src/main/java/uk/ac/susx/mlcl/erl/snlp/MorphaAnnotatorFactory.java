/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.snlp;

import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.MorphaAnnotator;
import java.io.Serializable;
import java.util.Properties;

/**
 *
 * @author hamish
 */
public class MorphaAnnotatorFactory extends AbstractAnnotatorFactory implements Serializable {

    private static final long serialVersionUID = 1L;

    public MorphaAnnotatorFactory(Properties props) {
	super(props);
    }

    public Annotator create() {
	return new MorphaAnnotator(false);
    }
    
}
