/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package io.github.hamishmorgan.erl.snlp.annotators;

import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ReflectionLoading;

import java.io.Serializable;
import java.util.Properties;


public class NFLAnnotatorFactory extends AbstractAnnotatorFactory implements Serializable {

    private static final long serialVersionUID = 1L;

    public NFLAnnotatorFactory(Properties props) {
        super(props);
    }

    public Annotator create() {
        // these paths now extracted inside c'tor
        // String gazetteer = props.getProperty("nfl.gazetteer", DefaultPaths.DEFAULT_NFL_GAZETTEER);
        // String entityModel = props.getProperty("nfl.entity.model", DefaultPaths.DEFAULT_NFL_ENTITY_MODEL);
        // String relationModel = props.getProperty("nfl.relation.model", DefaultPaths.DEFAULT_NFL_RELATION_MODEL);
        final String className = "edu.stanford.nlp.pipeline.NFLAnnotator";
        return ReflectionLoading.loadByReflection(className, props);
    }

}
