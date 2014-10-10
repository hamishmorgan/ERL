/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package io.github.hamishmorgan.erl;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import edu.stanford.nlp.pipeline.AnnotatorPool;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

public class AnnotationServiceFactory {

    @Nonnull
    public AnnotationService get(Properties props)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        AnnotatorPool pool = Annotations.createPool(props);


        JsonFactory jsonFactory = new JacksonFactory();

        return new AnnotationServiceImpl(pool, jsonFactory);
    }

}
