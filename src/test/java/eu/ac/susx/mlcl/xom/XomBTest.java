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

        final XomB x = new XomB(new NodeFactory());

        Document doc = x.buildDocument()
                .setDocType("html")
                .setBaseURI(URI.create("http://localhost/"))
                .setRoot(x.buildRoot("html")
                .append(x.buildElement("head")
                /**/.append(x.buildElement("title")
                /**/.append("404 Not Found")))
                .append(x.buildElement("body")
                /**/.setBaseURI(URI.create("http://example.com/"))
                /**/.appendAttribute("id", "mc body")
                /**/.append(x.buildElement("h1")
                /*    */.append("Not Found"))
                /**/.append(x.buildElement("p")
                /*    */.append("Abject failure."))
                /**/.append(x.buildElement("hr"))
                /**/.append(x.buildElement("address")
                /*    */.append("Unicorn powered."))))
                .appendPI("php", "run_finalizer();")
                .build();


        System.out.println(XomUtil.toString(doc, Charset.forName("ASCII")));

    }

    @Test
    public void testNamespaces() {

        final XomB x = new XomB(new NodeFactory());

        URI h = URI.create("http://www.w3.org/TR/html4/");
        URI f = URI.create("http://www.w3schools.com/furniture");
        Document doc = x.buildDocument().setRoot(
                x.buildElement("root")
                    .append(x.buildElement("table")
                        .setNamespace(h).setPrefix("h")
                        .append(x.buildElement("tr")
                            .setNamespace(h).setPrefix("h")
                            .append(x.buildElement("td")
                                .setNamespace(h).setPrefix("h")
                                .append("Apples"))
                            .append(x.buildElement("td")
                                .setNamespace(h).setPrefix("h")
                                .append("Bananas"))))
                    .append(x.buildElement("table")
                        .setNamespace(f).setPrefix("f")
                        .append(x.buildElement("name")
                            .setNamespace(f).setPrefix("f")
                            .append("African Coffee Table"))
                        .append(x.buildElement("width")
                            .setNamespace(f).setPrefix("f")
                            .append("80"))
                        .append(x.buildElement("length")
                            .setNamespace(f).setPrefix("f")
                            .append("120")))
                ).build();

         System.out.println(XomUtil.toString(doc, Charset.forName("ASCII")));

         
//        <root>
//
//<h:table xmlns:h="http://www.w3.org/TR/html4/">
//  <h:tr>
//    <h:td>Apples</h:td>
//    <h:td>Bananas</h:td>
//  </h:tr>
//</h:table>
//
//<f:table xmlns:f="http://www.w3schools.com/furniture">
//  <f:name>African Coffee Table</f:name>
//  <f:width>80</f:width>
//  <f:length>120</f:length>
//</f:table>
//
//</root>
//        
//        

    }
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
