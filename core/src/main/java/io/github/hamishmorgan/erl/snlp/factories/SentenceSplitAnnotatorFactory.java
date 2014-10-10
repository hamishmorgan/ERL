/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package io.github.hamishmorgan.erl.snlp.factories;

import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.process.PTBTokenizer;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;


public class SentenceSplitAnnotatorFactory extends AbstractAnnotatorFactory implements Serializable {

    public static final String NEWLINE_SPLITTER_PROPERTY = "ssplit.eolonly";

    public SentenceSplitAnnotatorFactory(Properties props) {
        super(props);
    }

    private static final long serialVersionUID = 1L;

    public Annotator create() {
        boolean nlSplitting =
                Boolean.valueOf(props.getProperty(NEWLINE_SPLITTER_PROPERTY,
                        "false"));
        if (nlSplitting) {
            boolean whitespaceTokenization = Boolean.valueOf(props.getProperty("tokenize.whitespace",
                    "false"));
            WordsToSentencesAnnotator wts;
            if (whitespaceTokenization) {
                if (System.getProperty("line.separator").equals("\n")) {
                    wts = WordsToSentencesAnnotator.newlineSplitter(false, "\n");
                } else {
                    // throw "\n" in just in case files use that instead of
                    // the system separator
                    wts =
                            WordsToSentencesAnnotator.newlineSplitter(false,
                                    System.getProperty("line.separator"),
                                    "\n");
                }
            } else {
                wts =
                        WordsToSentencesAnnotator.newlineSplitter(false,
                                PTBTokenizer.getNewlineToken());
            }
            return wts;
        } else {
            WordsToSentencesAnnotator wts = new WordsToSentencesAnnotator(false);
            // regular boundaries
            String bounds = props.getProperty("ssplit.boundariesToDiscard");
            if (bounds != null) {
                String[] toks = bounds.split(",");
                // for(int i = 0; i < toks.length; i ++)
                //   System.err.println("BOUNDARY: " + toks[i]);
                wts.setSentenceBoundaryToDiscard(new HashSet<String>(Arrays.asList(toks)));
            }
            // HTML boundaries
            bounds = props.getProperty("ssplit.htmlBoundariesToDiscard");
            if (bounds != null) {
                String[] toks = bounds.split(",");
                wts.addHtmlSentenceBoundaryToDiscard(new HashSet<String>(Arrays.asList(toks)));
            }
            // Treat as one sentence
            String isOneSentence = props.getProperty("ssplit.isOneSentence");
            if (isOneSentence != null) {
                wts.setOneSentence(Boolean.parseBoolean(isOneSentence));
            }
            return wts;
        }
    }

}
