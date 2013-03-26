/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.snlp;

import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.DefaultPaths;
import edu.stanford.nlp.pipeline.GenderAnnotator;

import java.io.Serializable;
import java.util.Properties;

/**
 *
 * @author hamish
 */
public class GenderAnnotatorFactory extends AbstractAnnotatorFactory implements Serializable {

    private static final long serialVersionUID = 1L;

    public GenderAnnotatorFactory(Properties props) {
	super(props);
    }

    public Annotator create() {
	return new GenderAnnotator(false,
				   props.getProperty("gender.firstnames",
						     DefaultPaths.DEFAULT_GENDER_FIRST_NAMES));
    }
    
}
