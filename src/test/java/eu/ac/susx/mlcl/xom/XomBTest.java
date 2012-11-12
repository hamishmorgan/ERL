/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package eu.ac.susx.mlcl.xom;

import eu.ac.susx.mlcl.xom.XomB.DocumentBuilder;
import eu.ac.susx.mlcl.xom.XomB.ElementBuilder;
import java.net.URI;
import java.nio.charset.Charset;
import nu.xom.Document;
import nu.xom.NodeFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hamish
 */
public class XomBTest {

    public XomBTest() {
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

    @Test
    public void testHTMLGen() {

//        XomUtil.DocumentBuilder builder = XomUtil.documentBuilder().setDocType("html");
//
//        ElementBuilder root = builder.createRootElement("html");
//
//        root.createChildElement("head")
//                .createChildElement("title").append("404 Not Found");
//
//
//        ElementBuilder body = root.createChildElement("body");
//        body.appendAttribute("id", "mc body");
//        
//        body.createChildElement("h1").append("Not Found");
//        body.createChildElement("p").append("Abject failure and stuff.");
//        body.createChildElement("hr");
//        body.createChildElement("address").append("Unicorn powered magic webserver.");
//
//
//        Document doc = builder.build();
//
//        


	XomB x = new XomB(new NodeFactory());

	Document doc = x.newDocument()
		.setDocType("html")
		.setRoot(
		x.newRoot("html")
		.append(
		x.newElement("head").append(
		x.newElement("title").append("404 Not Found")))
		.append(
		x.newElement("body")
		.appendAttribute("id", "mc body")
		//                            .append(x.newElement("id", "mc body"))
		.append(x.newElement("h1")
		.append("Not Found"))
		.append(x.newElement("p")
		.append("Abject failure."))
		.append(x.newElement("hr"))
		.append(x.newElement("address")
		.append("Unicorn powered.")))).appendPI("php",
							"run_finalizer();")
		.build();
//
//       
//        Document doc = docBuilder.build();


	System.out.println(XomUtil.toString(doc, Charset.forName("ASCII")));
//
//        
//        


//        db.


    }
//    
//                "<!DOCTYPE html>\n"
//            + "<html>"
//            + "  <head>\n"
//            + "    <title>{0,number,integer} {1}</title>\n"
//            + "  </head>"
//            + "  <body>\n"
//            + "    <h1>{1}</h1>\n"
//            + "    <p>{2}</p>\n"
//            + "    <hr>\n"
//            + "    <address>Unicorn powered magic webserver.</address>\n"
//            + "  </body>"
//            + "</html>");
//    /**
//     * Test of getFactory method, of class XomB.
//     */
//    @Test
//    public void testGetFactory() {
//	System.out.println("getFactory");
//	XomB instance = new XomB();
//	NodeFactory expResult = null;
//	NodeFactory result = instance.getFactory();
//	assertEquals(expResult, result);
//	// TODO review the generated test code and remove the default call to fail.
//	fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of newDocument method, of class XomB.
//     */
//    @Test
//    public void testNewDocument() {
//	System.out.println("newDocument");
//	XomB instance = new XomB();
//	DocumentBuilder expResult = null;
//	DocumentBuilder result = instance.newDocument();
//	assertEquals(expResult, result);
//	// TODO review the generated test code and remove the default call to fail.
//	fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of newRoot method, of class XomB.
//     */
//    @Test
//    public void testNewRoot_String_URI() {
//	System.out.println("newRoot");
//	String name = "";
//	URI namespace = null;
//	XomB instance = new XomB();
//	ElementBuilder expResult = null;
//	ElementBuilder result = instance.newRoot(name, namespace);
//	assertEquals(expResult, result);
//	// TODO review the generated test code and remove the default call to fail.
//	fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of newRoot method, of class XomB.
//     */
//    @Test
//    public void testNewRoot_String() {
//	System.out.println("newRoot");
//	String name = "";
//	XomB instance = new XomB();
//	ElementBuilder expResult = null;
//	ElementBuilder result = instance.newRoot(name);
//	assertEquals(expResult, result);
//	// TODO review the generated test code and remove the default call to fail.
//	fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of newElement method, of class XomB.
//     */
//    @Test
//    public void testNewElement_String_URI() {
//	System.out.println("newElement");
//	String name = "";
//	URI namespace = null;
//	XomB instance = new XomB();
//	ElementBuilder expResult = null;
//	ElementBuilder result = instance.newElement(name, namespace);
//	assertEquals(expResult, result);
//	// TODO review the generated test code and remove the default call to fail.
//	fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of newElement method, of class XomB.
//     */
//    @Test
//    public void testNewElement_String() {
//	System.out.println("newElement");
//	String name = "";
//	XomB instance = new XomB();
//	ElementBuilder expResult = null;
//	ElementBuilder result = instance.newElement(name);
//	assertEquals(expResult, result);
//	// TODO review the generated test code and remove the default call to fail.
//	fail("The test case is a prototype.");
//    }
}
