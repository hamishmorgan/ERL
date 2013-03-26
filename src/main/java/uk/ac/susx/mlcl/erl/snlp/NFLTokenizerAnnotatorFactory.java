/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.snlp;

import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ReflectionLoading;

import java.io.Serializable;
import java.util.Properties;

/**
 *
 * @author hamish
 */
public class NFLTokenizerAnnotatorFactory extends AbstractAnnotatorFactory implements Serializable {

    //
    private static final long serialVersionUID = 1L;

    public NFLTokenizerAnnotatorFactory(Properties props) {
	super(props);
    }

    public Annotator create() {
	final String className =
		"edu.stanford.nlp.pipeline.NFLTokenizerAnnotator";
	return ReflectionLoading.loadByReflection(className);
    }
    
}
//
