/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.snlp;

import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.DefaultPaths;
import edu.stanford.nlp.pipeline.TrueCaseAnnotator;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Properties;

/**
 * @author hamish
 */
public class TrueCaseAnnotatorFactory extends AbstractAnnotatorFactory implements Serializable {

    private static final long serialVersionUID = 1L;

    public TrueCaseAnnotatorFactory(Properties props) {
        super(props);
    }

    @Nonnull
    public Annotator create() {
        String model =
                props.getProperty("truecase.model",
                        DefaultPaths.DEFAULT_TRUECASE_MODEL);
        String bias =
                props.getProperty("truecase.bias",
                        TrueCaseAnnotator.DEFAULT_MODEL_BIAS);
        String mixed =
                props.getProperty("truecase.mixedcasefile",
                        DefaultPaths.DEFAULT_TRUECASE_DISAMBIGUATION_LIST);
        return new TrueCaseAnnotator(model, bias, mixed, false);
    }

}
