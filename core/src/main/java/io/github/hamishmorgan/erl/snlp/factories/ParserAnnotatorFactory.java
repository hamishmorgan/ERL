/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package io.github.hamishmorgan.erl.snlp.factories;

import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CharniakParserAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Properties;


public class ParserAnnotatorFactory extends AbstractAnnotatorFactory implements Serializable {

    private static final long serialVersionUID = 1L;

    public ParserAnnotatorFactory(Properties props) {
        super(props);
    }

    @Nonnull
    public Annotator create() {
        String parserType = props.getProperty("parser.type", "stanford");
        String maxLenStr = props.getProperty("parser.maxlen");
        if (parserType.equalsIgnoreCase("stanford")) {
            return new ParserAnnotator("parser", props);
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
            return new CharniakParserAnnotator(model, parserExecutable, false, maxLen);
        } else {
            throw new RuntimeException("Unknown parser type: " + parserType +
                    " (currently supported: stanford and charniak)");
        }
    }

}
