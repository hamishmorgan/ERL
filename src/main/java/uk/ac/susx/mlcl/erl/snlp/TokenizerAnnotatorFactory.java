/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.snlp;

import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.PTBTokenizerAnnotator;
import edu.stanford.nlp.pipeline.WhitespaceTokenizerAnnotator;

import java.io.Serializable;
import java.util.Properties;

/**
 * @author hamish
 */
public class TokenizerAnnotatorFactory extends AbstractAnnotatorFactory implements Serializable {

    public static final String NEWLINE_SPLITTER_PROPERTY = "ssplit.eolonly";

    public TokenizerAnnotatorFactory(Properties props) {
        super(props);
    }

    private static final long serialVersionUID = 1L;

    public Annotator create() {
        if (Boolean.valueOf(props.getProperty("tokenize.whitespace", "false"))) {
            return new WhitespaceTokenizerAnnotator(props);
        } else {
            String options =
                    props.getProperty("tokenize.options",
                            PTBTokenizerAnnotator.DEFAULT_OPTIONS);
            boolean keepNewline =
                    Boolean.valueOf(props.getProperty(NEWLINE_SPLITTER_PROPERTY,
                            "false"));
            // If the user specifies "tokenizeNLs=false" in tokenize.options, then this default will
            // be overridden.
            if (keepNewline) {
                options = "tokenizeNLs," + options;
            }
            return new PTBTokenizerAnnotator(false, options);
        }
    }

}
