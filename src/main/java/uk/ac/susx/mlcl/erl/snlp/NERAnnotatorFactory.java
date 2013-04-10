/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.snlp;

import edu.stanford.nlp.ie.NERClassifierCombiner;
import edu.stanford.nlp.ie.regexp.NumberSequenceClassifier;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.DefaultPaths;
import edu.stanford.nlp.pipeline.NERCombinerAnnotator;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.PropertiesUtils;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author hamish
 */
public class NERAnnotatorFactory extends AbstractAnnotatorFactory implements Serializable {

    private static final long serialVersionUID = 1L;

    public NERAnnotatorFactory(Properties props) {
        super(props);
    }

    public Annotator create() {
        List<String> models = new ArrayList<String>();
        List<Pair<String, String>> modelNames =
                new ArrayList<Pair<String, String>>();
        modelNames.add(new Pair<String, String>("ner.model", null));
        modelNames.add(new Pair<String, String>("ner.model.3class",
                DefaultPaths.DEFAULT_NER_THREECLASS_MODEL));
        modelNames.add(new Pair<String, String>("ner.model.7class",
                DefaultPaths.DEFAULT_NER_MUC_MODEL));
        modelNames.add(new Pair<String, String>("ner.model.MISCclass",
                DefaultPaths.DEFAULT_NER_CONLL_MODEL));
        for (Pair<String, String> name : modelNames) {
            String model = props.getProperty(name.first, name.second);
            if (model != null && model.length() > 0) {
                models.addAll(Arrays.asList(model.split(",")));
            }
        }
        if (models.isEmpty()) {
            throw new RuntimeException("no NER models specified");
        }
        NERClassifierCombiner nerCombiner;
        try {
            boolean applyNumericClassifiers =
                    PropertiesUtils.getBool(props,
                            NERClassifierCombiner.APPLY_NUMERIC_CLASSIFIERS_PROPERTY,
                            NERClassifierCombiner.APPLY_NUMERIC_CLASSIFIERS_DEFAULT);
            boolean useSUTime =
                    PropertiesUtils.getBool(props,
                            NumberSequenceClassifier.USE_SUTIME_PROPERTY,
                            NumberSequenceClassifier.USE_SUTIME_DEFAULT);
            nerCombiner =
                    new NERClassifierCombiner(applyNumericClassifiers, useSUTime,
                            props,
                            models.toArray(new String[models.size()]));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        // ms 2009, no longer needed: the functionality of all these annotators is now included in NERClassifierCombiner
    /*
	 * AnnotationPipeline pipeline = new AnnotationPipeline(); pipeline.addAnnotator(new
	 * NERCombinerAnnotator(nerCombiner, false)); pipeline.addAnnotator(new
	 * NumberAnnotator(false)); pipeline.addAnnotator(new TimeWordAnnotator(false));
	 * pipeline.addAnnotator(new QuantifiableEntityNormalizingAnnotator(false, false));
	 * return pipeline;
	 */
        return new NERCombinerAnnotator(nerCombiner, false);
    }

}
