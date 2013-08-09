/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.snlp;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.PTBTokenizerAnnotator;
import edu.stanford.nlp.pipeline.TokenizerAnnotator;
import edu.stanford.nlp.pipeline.WhitespaceTokenizerAnnotator;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import uk.ac.susx.mlcl.erl.test.AbstractTest;
import uk.ac.susx.mlcl.erl.test.Categories;

/**
 *
 * @author hamish
 */
public class CharacterShapeAnnotatorTest extends AbstractTest {

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
            String clazz = token.get(CharacterShapeAnnotator.Annotation.class);

            Assert.assertNotNull(word);
            Assert.assertNotNull(clazz);
            Assert.assertEquals(clazz.length(), word.length());

            System.out.printf("[%d,%d] %s => %s%n",
                              token.beginPosition(), token.endPosition(), word, clazz);
        }
    }

    /**
     * Test of configure method, of class CharacterShapeAnnotator.
     */
    @Test
    public void testConfigure() {

        CharacterShapeAnnotator instance = new CharacterShapeAnnotator();

        Assert.assertEquals(CharacterShapeAnnotator.LEMMA_USED_DEFAULT_VALUE,
                            instance.isLemmaUsed());

        Properties props = new Properties();
        instance.configure(props);

        Assert.assertEquals(CharacterShapeAnnotator.LEMMA_USED_DEFAULT_VALUE,
                            instance.isLemmaUsed());

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
            String actualShape = token.get(CharacterShapeAnnotator.Annotation.class);

            Assert.assertEquals("token missmatch:",
                                expectedTokens.get(i), actualWord);
            Assert.assertNotNull("shape is null", actualShape);
            Assert.assertEquals("shape missmatch (for token " + actualWord + "):",
                                expectedShape.get(i), actualShape);
            
        }
    }
}
