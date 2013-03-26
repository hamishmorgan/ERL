/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.snlp;

import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CharniakParserAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;

import java.io.Serializable;
import java.util.Properties;

/**
 *
 * @author hamish
 */
public class ParserAnnotatorFactory extends AbstractAnnotatorFactory implements Serializable {

    private static final long serialVersionUID = 1L;

    public ParserAnnotatorFactory(Properties props) {
	super(props);
    }

    public Annotator create() {
	String parserType = props.getProperty("parser.type", "stanford");
	String maxLenStr = props.getProperty("parser.maxlen");
	if (parserType.equalsIgnoreCase("stanford")) {
	    ParserAnnotator anno = new ParserAnnotator("parser", props);
	    return anno;
	} else if (parserType.equalsIgnoreCase("charniak")) {
	    String model = props.getProperty("parser.model");
	    String parserExecutable = props.getProperty("parser.executable");
	    if (model == null || parserExecutable == null) {
		throw new RuntimeException("Both parser.model and parser.executable properties must be specified if parser.type=charniak");
	    }
	    int maxLen = 399;
	    if (maxLenStr != null) {
		maxLen = Integer.parseInt(maxLenStr);
	    }
	    CharniakParserAnnotator anno =
		    new CharniakParserAnnotator(model, parserExecutable, false,
						maxLen);
	    return anno;
	} else {
	    throw new RuntimeException("Unknown parser type: " + parserType +
		    " (currently supported: stanford and charniak)");
	}
    }
    
}
