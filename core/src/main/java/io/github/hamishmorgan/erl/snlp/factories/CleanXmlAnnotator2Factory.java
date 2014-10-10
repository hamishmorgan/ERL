package io.github.hamishmorgan.erl.snlp.factories;

import edu.stanford.nlp.pipeline.Annotator;
import io.github.hamishmorgan.erl.snlp.annotators.CleanXmlAnnotator2;
import io.github.hamishmorgan.erl.snlp.factories.AbstractAnnotatorFactory;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Properties;

/**
* Created by hamish on 10/10/14.
*/
public class CleanXmlAnnotator2Factory extends AbstractAnnotatorFactory implements Serializable {

    public static final String NEWLINE_SPLITTER_PROPERTY = "ssplit.eolonly";

    public CleanXmlAnnotator2Factory(Properties props) {
        super(props);
    }

    private static final long serialVersionUID = 1L;

    @Nonnull
    public Annotator create() {
        String xmlTags =
                props.getProperty("clean.xmltags",
                        CleanXmlAnnotator2.DEFAULT_XML_TAGS);
        String sentenceEndingTags = props.getProperty(
                "clean.sentenceendingtags",
                CleanXmlAnnotator2.DEFAULT_SENTENCE_ENDERS);
        String allowFlawedString = props.getProperty("clean.allowflawedxml");
        boolean allowFlawed = CleanXmlAnnotator2.DEFAULT_ALLOW_FLAWS;
        if (allowFlawedString != null) {
            allowFlawed = Boolean.valueOf(allowFlawedString);
        }
        String dateTags =
                props.getProperty("clean.datetags",
                        CleanXmlAnnotator2.DEFAULT_DATE_TAGS);
        return new CleanXmlAnnotator2(xmlTags, sentenceEndingTags, dateTags,
                allowFlawed);
    }
}
