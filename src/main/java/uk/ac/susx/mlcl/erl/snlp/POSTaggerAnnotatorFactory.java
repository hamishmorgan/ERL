/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.snlp;

import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.DefaultPaths;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Properties;

//        //

//        // POS tagger
public class POSTaggerAnnotatorFactory extends AbstractAnnotatorFactory implements Serializable {

    private static final long serialVersionUID = 1L;

    public POSTaggerAnnotatorFactory(Properties props) {
        super(props);
    }

    @Nonnull
    public Annotator create() {
        try {
            String maxLenStr = props.getProperty("pos.maxlen");
            int maxLen = Integer.MAX_VALUE;
            if (maxLenStr != null) {
                maxLen = Integer.parseInt(maxLenStr);
            }
            return new POSTaggerAnnotator(props.getProperty("pos.model",
                    DefaultPaths.DEFAULT_POS_MODEL),
                    false, maxLen);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
