/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package io.github.hamishmorgan.erl.snlp.annotators;

import edu.stanford.nlp.ie.regexp.RegexNERSequenceClassifier;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.DefaultPaths;
import edu.stanford.nlp.pipeline.RegexNERAnnotator;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Properties;


public class RegexNERAnnotatorFactory extends AbstractAnnotatorFactory implements Serializable {

    private static final long serialVersionUID = 1L;

    public RegexNERAnnotatorFactory(Properties props) {
        super(props);
    }

    @Nonnull
    public Annotator create() {
        String mapping =
                props.getProperty("regexner.mapping",
                        DefaultPaths.DEFAULT_REGEXNER_RULES);
        String ignoreCase = props.getProperty("regexner.ignorecase", "false");
        String validPosPattern =
                props.getProperty("regexner.validpospattern",
                        RegexNERSequenceClassifier.DEFAULT_VALID_POS);
        return new RegexNERAnnotator(mapping, Boolean.valueOf(ignoreCase),
                validPosPattern);
    }

}
