package uk.ac.susx.mlcl.erl;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.CharMatcher;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.susx.mlcl.erl.linker.EntityLinkingAnnotator;
import uk.ac.susx.mlcl.erl.test.AbstractTest;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 10/04/2013
 * Time: 13:27
 * To change this template use File | Settings | File Templates.
 */
public class AnnotationServiceTest extends AbstractTest {

    @Test
    public void testAnnotationToJson() throws ClassNotFoundException, InstantiationException, ConfigurationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final String expected = "[ " +
                "{\n" +
                "  \"text\" : \"North Korea\",\n" +
                "  \"id\" : \"/nil\",\n" +
                "  \"url\" : \"http://www.freebase.com/view/nil\",\n" +
                "  \"type\" : \"Misc\"\n" +
                "}," +
                " {\n" +
                "  \"text\" : \" \"\n" +
                "}," +
                " {\n" +
                "  \"text\" : \"is planning to pull its ambassador out of the\",\n" +
                "  \"type\" : \"O\"\n" +
                "}, {\n" +
                "  \"text\" : \" \"\n" +
                "}, {\n" +
                "  \"text\" : \"UK\",\n" +
                "  \"id\" : \"/nil\",\n" +
                "  \"url\" : \"http://www.freebase.com/view/nil\",\n" +
                "  \"type\" : \"Misc\"\n" +
                "} " +
                "]";
        final String text = "North Korea is planning to pull its ambassador out of the UK";
        doAnnotationToJsonTest(text, expected);
    }

    @Test
    public void testAnnotationToJson_TwoWords() throws ClassNotFoundException, InstantiationException, ConfigurationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final String expected = "[ {\n" +
                "  \"text\" : \"North Korea\",\n" +
                "  \"id\" : \"/nil\",\n" +
                "  \"url\" : \"http://www.freebase.com/view/nil\",\n" +
                "  \"type\" : \"Misc\"\n" +
                "} ]";
        String text = "North Korea";
        doAnnotationToJsonTest(text, expected);
    }

    @Test
    public void testAnnotationToJson_OneWord() throws ClassNotFoundException, InstantiationException, ConfigurationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final String expected = "[ {\n" +
                "  \"text\" : \"North\",\n" +
                "  \"id\" : \"/nil\",\n" +
                "  \"url\" : \"http://www.freebase.com/view/nil\",\n" +
                "  \"type\" : \"Misc\"\n" +
                "} ]";
        String text = "North";
        doAnnotationToJsonTest(text, expected);
    }

    @Test
    public void testAnnotationToJson_OneChar() throws ClassNotFoundException, InstantiationException, ConfigurationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final String expected = "[ {\n" +
                "  \"text\" : \"x\",\n" +
                "  \"type\" : \"O\"\n" +
                "} ]";
        String text = "x";
        doAnnotationToJsonTest(text, expected);
    }

    @Test
    public void testAnnotationToJson_OneSpace() throws ClassNotFoundException, InstantiationException, ConfigurationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final String expected = "[ {\n" +
                "  \"text\" : \" \"\n" +
                "} ]";
        String text = " ";
        doAnnotationToJsonTest(text, expected);
    }

    @Test
    public void testAnnotationToJson_Empty() throws ClassNotFoundException, InstantiationException, ConfigurationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final String expected = "[ ]";
        String text = "";
        doAnnotationToJsonTest(text, expected);
    }

    private void doAnnotationToJsonTest(String input, String expectedOutput) throws ClassNotFoundException, InstantiationException, ConfigurationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        try {
            Properties props = new Properties();
            AnnotationService instance = AnnotationService.newInstance(props);
            Annotation document = fakeAnnotator(input);
            StringWriter writer = new StringWriter();
            instance.annotationToJson(document, writer);
            String actual = writer.toString();
            Assert.assertEquals(expectedOutput, actual);
        } catch (IOException impossible) {
            throw new AssertionError(impossible);
        }
    }

    private Annotation fakeAnnotator(String text) {
        Annotation document = new Annotation(text);

        List<CoreLabel> tokens = Lists.newArrayList();

        int i = 0;
        while (i < text.length()) {
            while (i < text.length() && CharMatcher.WHITESPACE.matches(text.charAt(i)))
                ++i;
            final int beginOffset = i;
            while (i < text.length() && !CharMatcher.WHITESPACE.matches(text.charAt(i)))
                ++i;
            final int endOffset = i;
            if (beginOffset != endOffset) {
                CoreLabel token = new CoreLabel();
                token.set(CoreAnnotations.TextAnnotation.class, text.substring(beginOffset, endOffset));
                token.set(CoreAnnotations.CharacterOffsetBeginAnnotation.class, beginOffset);
                token.set(CoreAnnotations.CharacterOffsetEndAnnotation.class, endOffset);
                final boolean isEntity = Character.isUpperCase(text.charAt(beginOffset));
                token.set(CoreAnnotations.NamedEntityTagAnnotation.class, isEntity ? "Misc" : "O");
                if (isEntity)
                    token.set(EntityLinkingAnnotator.EntityKbIdAnnotation.class, "/nil");
                tokens.add(token);
            }
        }

        document.set(CoreAnnotations.TokensAnnotation.class, tokens);
        return document;
    }
}
