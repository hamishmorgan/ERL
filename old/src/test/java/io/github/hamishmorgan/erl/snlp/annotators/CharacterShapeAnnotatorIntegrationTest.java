/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package io.github.hamishmorgan.erl.snlp.annotators;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import io.github.hamishmorgan.erl.snlp.annotations.CharacterShapeAnnotation;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import uk.ac.susx.mlcl.erl.test.AbstractTest;
import uk.ac.susx.mlcl.erl.test.Categories;

import java.io.IOException;

/**
 *
 * @author hamish
 */
public class CharacterShapeAnnotatorIntegrationTest extends AbstractTest {

    @Test
    @Category(Categories.IntegrationTests.class)
    public void testCustomAnnotator() throws IOException {

        final boolean verbose = false;

        AnnotationPipeline pipeline = new AnnotationPipeline();

        TokenizerAnnotator tokeniser = new PTBTokenizerAnnotator(verbose);
        pipeline.addAnnotator(tokeniser);

        final CharacterShapeAnnotator characterClassC18N = new CharacterShapeAnnotator();
        pipeline.addAnnotator(characterClassC18N);

        String text = readTestData("freebase_brighton.txt");

        Annotation document = new Annotation(text);

        pipeline.annotate(document);

        for (CoreLabel token : document.get(CoreAnnotations.TokensAnnotation.class)) {
            String word = token.get(CoreAnnotations.TextAnnotation.class);
            String clazz = token.get(CharacterShapeAnnotation.class);

            Assert.assertNotNull(word);
            Assert.assertNotNull(clazz);
            Assert.assertEquals(clazz.length(), word.length());

            System.out.printf("[%d,%d] %s => %s%n",
                              token.beginPosition(), token.endPosition(), word, clazz);
        }
    }

}
