/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package io.github.hamishmorgan.erl.snlp.annotators;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.WhitespaceTokenizerAnnotator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import io.github.hamishmorgan.erl.snlp.annotations.CharacterShapeAnnotation;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.susx.mlcl.erl.test.AbstractTest;

/**
 *
 * @author hamish
 */
public class CharacterShapeAnnotatorTest extends AbstractTest {

    @Test
    public void givenNewInstance_whenIsLemmaUsed_thenResultIsDefault() {
        CharacterShapeAnnotator instance = new CharacterShapeAnnotator();
        Assert.assertEquals(CharacterShapeAnnotator.LEMMA_USED_DEFAULT_VALUE,
                instance.isLemmaUsed());
    }

    @Test
    public void givenEmptyConfiguration_whenIsLemmaUsed_thenResultIsDefault() {

        CharacterShapeAnnotator instance = new CharacterShapeAnnotator();

        Properties props = new Properties();
        instance.configure(props);

        Assert.assertEquals(CharacterShapeAnnotator.LEMMA_USED_DEFAULT_VALUE,
                instance.isLemmaUsed());


    }

    @Test
    public void testConfigurex() {

        CharacterShapeAnnotator instance = new CharacterShapeAnnotator();

        Properties props = new Properties();
        instance.configure(props);

        props.setProperty(CharacterShapeAnnotator.LEMMA_USED_KEY, "falSE");
        instance.configure(props);

        Assert.assertEquals(false, instance.isLemmaUsed());

        props.setProperty(CharacterShapeAnnotator.LEMMA_USED_KEY, "tRue");
        instance.configure(props);

        Assert.assertEquals(true, instance.isLemmaUsed());

    }

    /**
     * Test of getRequiredAnnotations method, of class CharacterShapeAnnotator.
     */
    @Test
    public void testGetRequiredAnnotations() {

        CharacterShapeAnnotator instance = new CharacterShapeAnnotator();

        {
            instance.setLemmaUsed(false);

            Set<Class<?>> expResult = new HashSet<Class<?>>();
            expResult.add(CoreAnnotations.TokensAnnotation.class);

            Set<?> result = instance.getRequiredAnnotations();

            Assert.assertEquals(expResult, result);
        }

        {
            instance.setLemmaUsed(true);

            Set<Class<?>> expResult = new HashSet<Class<?>>();
            expResult.add(CoreAnnotations.TokensAnnotation.class);
            expResult.add(CoreAnnotations.LemmaAnnotation.class);

            Set<?> result = instance.getRequiredAnnotations();

            Assert.assertEquals(expResult, result);
        }
    }

    /**
     * Test of annotate method, of class CharacterShapeAnnotator.
     */
    @Test
    public void testAnnotate() {

        CharacterShapeAnnotator instance = new CharacterShapeAnnotator();
        instance.setLemmaUsed(false);

        String text = "Brighton €30 4.34 :-) 20% BN2 3LE åßƒ ๐๑๒ A\u0078\u20D7Aá";
        List<String> expectedTokens = Arrays.asList("Brighton", "€30", "4.34", ":-)", "20%", "BN2",
                                                    "3LE", "åßƒ", "๐๑๒", "A\u0078\u20D7Aá");
        List<String> expectedShape = Arrays.asList("Aaaaaaaa", "$##", "#.##", "...", "##.", "AA#",
                                                   "#AA", "aaa", "###", "Aa.Aa");

        Annotation document = new Annotation(text);
        WhitespaceTokenizerAnnotator tokeniser =
                new WhitespaceTokenizerAnnotator(new Properties());
        tokeniser.annotate(document);
        
        instance.annotate(document);

        List<CoreLabel> tokenList = document.get(CoreAnnotations.TokensAnnotation.class);
        for (int i = 0; i < tokenList.size(); i++) {
            CoreLabel token = tokenList.get(i);

            String actualWord = token.get(CoreAnnotations.TextAnnotation.class);
            String actualShape = token.get(CharacterShapeAnnotation.class);

            Assert.assertEquals("token missmatch:",
                                expectedTokens.get(i), actualWord);
            Assert.assertNotNull("shape is null", actualShape);
            Assert.assertEquals("shape missmatch (for token " + actualWord + "):",
                                expectedShape.get(i), actualShape);
            
        }
    }
}
