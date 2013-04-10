/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.xml;

import uk.ac.susx.mlcl.erl.xml.XomB.ElementBuilder;
import java.net.URI;
import java.nio.charset.Charset;
import nu.xom.Document;
import nu.xom.NodeFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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

        Document doc = x.document()
                .setDocType("html")
                .setBaseURI(URI.create("http://localhost/"))
                .setRoot(x.root("html")
		.addComment("Root comment!")
                .add(x.element("head")
                /**/.add(x.element("title")
                /**/.add("404 Not Found")))
                .add(x.element("body")
		/**/.addComment("Body comment!")
                /**/.setBaseURI(URI.create("http://example.com/"))
                /**/.addAttribute("id", "mc body")
                /**/.add(x.element("h1")
                /*    */.add("Not Found"))
                /**/.add(x.element("p")
                /*    */.add("Abject failure."))
                /**/.add(x.element("hr"))
                /**/.add(x.element("address")
                /*    */.add("Unicorn powered."))))
                .addPI("php", "run_finalizer();")
                .build();


        System.out.println(XomUtil.toString(doc, Charset.forName("ASCII")));

    }

    @Test
    public void testRepeatedBuild() {
	
	XomB x = new XomB();
	ElementBuilder p = x.element("p");
	
	Document doc = x.document().setRoot(
		    x.element("root")
			.add(p.add("A"))
			.add(p.addComment("win"))
			.add(p.add("B"))
			.add(p.add("C"))
			.add(p.add("D"))
		).build();
	
        System.out.println(XomUtil.toString(doc, Charset.forName("ASCII")));
    }
   
    
    @Test
    public void testNamespaces() {

        final XomB x = new XomB(new NodeFactory());

        URI h = URI.create("http://www.w3.org/TR/html4/");
        URI f = URI.create("http://www.w3schools.com/furniture");
        Document doc = x.document().setRoot(
                x.element("root")
                    .add(x.element("table")
                        .setNamespace(h).setPrefix("h")
                        .add(x.element("tr")
                            .setNamespace(h).setPrefix("h")
                            .add(x.element("td")
                                .setNamespace(h).setPrefix("h")
                                .add("Apples"))
                            .add(x.element("td")
                                .setNamespace(h).setPrefix("h")
                                .add("Bananas"))))
                    .add(x.element("table")
                        .setNamespace(f).setPrefix("f")
                        .add(x.element("name")
                            .setNamespace(f).setPrefix("f")
                            .add("African Coffee Table"))
                        .add(x.element("width")
                            .setNamespace(f).setPrefix("f")
                            .add("80"))
                        .add(x.element("length")
                            .setNamespace(f).setPrefix("f")
                            .add("120")))
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
    
//     @Test
//    public void testCommentEscaping() {
//
//        final XomB x = new XomB(new NodeFactory());
//
//        Document doc = x.document()
//                .setDocType("html")
//                .setRoot(x.root("html")
//		.addComment("Root  comment!")
//                .add(x.element("head")
//                /**/.add(x.element("title")
//                /**/.add("404 Not Found")))
//                .add(x.element("body")
//		/**/.addComment("Body comment!")
//                /**/.add(x.element("h1")
//                /*    */.add("Unicorn powered."))))
//                .build();
//
//
//        System.out.println(XomUtil.toString(doc, Charset.forName("ASCII")));
//
//    }
     
//        XomUtil.DocumentBuilder builder = XomUtil.documentBuilder().setDocType("html");
//
//        ElementBuilder root = builder.createRootElement("html");
//
//        root.createChildElement("head")
//                .createChildElement("title").add("404 Not Found");
//
//
//        ElementBuilder body = root.createChildElement("body");
//        body.addAttribute("id", "mc body");
//        
//        body.createChildElement("h1").add("Not Found");
//        body.createChildElement("p").add("Abject failure and stuff.");
//        body.createChildElement("hr");
//        body.createChildElement("address").add("Unicorn powered magic webserver.");
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
//	// TODO review the generated simpleTest code and remove the default call to fail.
//	fail("The simpleTest case is a prototype.");
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
//	// TODO review the generated simpleTest code and remove the default call to fail.
//	fail("The simpleTest case is a prototype.");
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
//	// TODO review the generated simpleTest code and remove the default call to fail.
//	fail("The simpleTest case is a prototype.");
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
//	// TODO review the generated simpleTest code and remove the default call to fail.
//	fail("The simpleTest case is a prototype.");
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
//	// TODO review the generated simpleTest code and remove the default call to fail.
//	fail("The simpleTest case is a prototype.");
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
//	// TODO review the generated simpleTest code and remove the default call to fail.
//	fail("The simpleTest case is a prototype.");
//    }
}
