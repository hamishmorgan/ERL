/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.snlp;

import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CleanXmlAnnotator;
import java.io.Serializable;
import java.util.Properties;

/**
 *
 * @author hamish
 */
public class CleanXmlAnnotatorFactory extends AbstractAnnotatorFactory implements Serializable {

    public static final String NEWLINE_SPLITTER_PROPERTY = "ssplit.eolonly";

    public CleanXmlAnnotatorFactory(Properties props) {
	super(props);
    }

    private static final long serialVersionUID = 1L;

    public Annotator create() {
	String xmlTags =
		props.getProperty("clean.xmltags",
				  CleanXmlAnnotator.DEFAULT_XML_TAGS);
	String sentenceEndingTags = props.getProperty("clean.sentenceendingtags",
						      CleanXmlAnnotator.DEFAULT_SENTENCE_ENDERS);
	String allowFlawedString = props.getProperty("clean.allowflawedxml");
	boolean allowFlawed = CleanXmlAnnotator.DEFAULT_ALLOW_FLAWS;
	if (allowFlawedString != null) {
	    allowFlawed = Boolean.valueOf(allowFlawedString);
	}
	String dateTags =
		props.getProperty("clean.datetags",
				  CleanXmlAnnotator.DEFAULT_DATE_TAGS);
	return new CleanXmlAnnotator(xmlTags, sentenceEndingTags, dateTags,
				     allowFlawed);
    }
    
}
