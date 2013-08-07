/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.snlp;

import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import uk.ac.susx.mlcl.erl.xml.AnnotationToXML;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Properties;
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
public class CleanXmlAnnotator2Test  extends AbstractTest {
    
    public CleanXmlAnnotator2Test() {
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
    
    
    public static AnnotationToXML newAnnotationToXML() {
	try {
	    AnnotationToXML.Builder builder = AnnotationToXML.builder();
            builder.configure(new PropertiesConfiguration(
		    "src/main/resources/sussexXml.properties"));
//            builder.addAnnotationToIgnore(
//                    CoreAnnotations.XmlContextAnnotation.class);
//            builder.addSimplifiedName(
//                    EntityLinkingAnnotator.EntityKbIdAnnotation.class, "link");
	    return builder.build();
	} catch (Throwable ex) {
	    Throwables.propagateIfPossible(ex);
	    throw new RuntimeException(ex);
	}
    }
    
    
    @Test
    public void testCleanXML() throws IOException, InstantiationException {
        Properties props = new Properties();
        props.put("annotators",
                  StanfordCoreNLP.STANFORD_TOKENIZE
                + "," + StanfordCoreNLP.STANFORD_CLEAN_XML
                + "," + StanfordCoreNLP.STANFORD_SSPLIT);

        props.put("clean.xmltags", ".*");
        props.put("clean.sentenceendingtags", "p|div|br");
        props.put("clean.allowflawedxml", CleanXmlAnnotator2.DEFAULT_ALLOW_FLAWS);
        props.put("clean.datetags", CleanXmlAnnotator2.DEFAULT_DATE_TAGS);

	
	AnnotationPipeline pipeline = new AnnotationPipeline();
	pipeline.addAnnotator(new TokenizerAnnotatorFactory(props).create());;
	pipeline.addAnnotator(new CleanXmlAnnotator2.Factory(props).create());

	String text = readTestData("bbc.html");
	
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

	
	AnnotationToXML xmler = newAnnotationToXML();
	
	
	new File("target/testout").mkdirs();
	
	Writer writer = new FileWriter(new File(
		"target/testout/bbc.html.annotations.xml"));
	xmler.xmlPrint(document, writer);
	Closeables.close(writer, true);
	
    }

//
//    /**
//     * Test of annotate method, of class CleanXmlAnnotator2.
//     */
//    @Test
//    public void testAnnotate() {
//	System.out.println("annotate");
//	Annotation annotation = null;
//	CleanXmlAnnotator2 instance = new CleanXmlAnnotator2();
//	instance.annotate(annotation);
//	// TODO review the generated test code and remove the default call to fail.
//	fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of process method, of class CleanXmlAnnotator2.
//     */
//    @Test
//    public void testProcess_List() {
//	System.out.println("process");
//	List<CoreLabel> tokens = null;
//	CleanXmlAnnotator2 instance = new CleanXmlAnnotator2();
//	List expResult = null;
//	List result = instance.process(tokens);
//	assertEquals(expResult, result);
//	// TODO review the generated test code and remove the default call to fail.
//	fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of process method, of class CleanXmlAnnotator2.
//     */
//    @Test
//    public void testProcess_List_List() {
//	System.out.println("process");
//	List<CoreLabel> tokens = null;
//	List<CoreLabel> dateTokens = null;
//	CleanXmlAnnotator2 instance = new CleanXmlAnnotator2();
//	List expResult = null;
//	List result = instance.process(tokens, dateTokens);
//	assertEquals(expResult, result);
//	// TODO review the generated test code and remove the default call to fail.
//	fail("The test case is a prototype.");
//    }
}
