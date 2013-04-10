/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.snlp;

import uk.ac.susx.mlcl.erl.xml.AnnotationToXML;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.time.TimeAnnotator;
import java.io.IOException;
import java.util.Properties;
import nu.xom.Document;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.susx.mlcl.erl.test.AbstractTest;

/**
 *
 * @author hamish
 */
public class AnnotationToXMLSerializerTest extends AbstractTest {

    public AnnotationToXMLSerializerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Run just the English tokeniser, on some wikipedia description of "Brighton" topic.
     * <p/>
     * @throws IOException
     */
    @Test
    public void testTokeniSe() throws IOException, ConfigurationException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit");


        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        String text = readTestData("freebase_brighton.txt");
        Annotation annotation = new Annotation(text);

        TimeAnnotator x = new TimeAnnotator();
        pipeline.addAnnotator(x);
        
        pipeline.annotate(annotation);


        AnnotationToXML.Builder builder = AnnotationToXML.builder();
        builder.addSimplifiedName(CharacterOffsetBeginAnnotation.class, "from");
        builder.addSimplifiedName(CharacterOffsetEndAnnotation.class, "to");

        builder.addXPathNodeFilter("//document/word");
        builder.addXPathNodeFilter("//document/tokens");
        builder.addXPathNodeFilter("//sentence/word");


        PropertiesConfiguration config = new PropertiesConfiguration(
                "stanfordXml.properties");


        builder.configure(config);

        AnnotationToXML toXml = builder.build();


        Document doc = toXml.toDocument(annotation);
//
//        for (String filter : filters) {
//            final Nodes nodes = doc.query(filter);
//            if (nodes != null) {
//                for (int i = 0; i < nodes.size(); i++) {
//                    nodes.get(i).getParent().removeChild(nodes.get(i));
//                }
//            }
//        }


        toXml.xmlPrint(doc, System.out);

    }
//    /**
//     * Test of builder method, of class AnnotationToXML.
//     */
//    @Test
//    public void testBuilder() {
//        System.out.println("builder");
//        Builder expResult = null;
//        Builder result = AnnotationToXML.builder();
//        assertEquals(expResult, result);
//        // TODO review the generated simpleTest code and remove the default call to fail.
//        fail("The simpleTest case is a prototype.");
//    }
//
//    /**
//     * Test of xmlPrint method, of class AnnotationToXML.
//     */
//    @Test
//    public void testXmlPrint_Annotation_Writer() throws Exception {
//        System.out.println("xmlPrint");
//        Annotation annotation = null;
//        Writer writer = null;
//        AnnotationToXML instance = null;
//        instance.xmlPrint(annotation, writer);
//        // TODO review the generated simpleTest code and remove the default call to fail.
//        fail("The simpleTest case is a prototype.");
//    }
//
//    /**
//     * Test of xmlPrint method, of class AnnotationToXML.
//     */
//    @Test
//    public void testXmlPrint_Annotation_OutputStream() throws Exception {
//        System.out.println("xmlPrint");
//        Annotation annotation = null;
//        OutputStream outputStream = null;
//        AnnotationToXML instance = null;
//        instance.xmlPrint(annotation, outputStream);
//        // TODO review the generated simpleTest code and remove the default call to fail.
//        fail("The simpleTest case is a prototype.");
//    }
//
//    /**
//     * Test of toDocument method, of class AnnotationToXML.
//     */
//    @Test
//    public void testToDocument() {
//        System.out.println("toDocument");
//        Annotation annotation = null;
//        AnnotationToXML instance = null;
//        Document expResult = null;
//        Document result = instance.toDocument(annotation);
//        assertEquals(expResult, result);
//        // TODO review the generated simpleTest code and remove the default call to fail.
//        fail("The simpleTest case is a prototype.");
//    }
}
