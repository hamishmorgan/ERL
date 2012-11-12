/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package eu.ac.susx.mlcl.xom;

import java.nio.charset.Charset;
import nu.xom.Document;
import nu.xom.NodeFactory;
import nu.xom.Attribute;
import nu.xom.Nodes;

/**
 *
 * @author hamish
 */
public class TestXOMBuilder {

    public static void main(String[] args) {

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
        
        Document doc = x.buildDocument()
            .setDocType("html")
            .setRoot(
                x.buildRoot("html")
                    .append(
                        x.buildElement("head").append(
                            x.buildElement("title").append("404 Not Found")
                        )
                    )
                    .append(
                        x.buildElement("body")
                            .appendAttribute("id", "mc body")
//                            .append(x.newElement("id", "mc body"))
                            .append(x.buildElement("h1")
                                                .append("Not Found"))
                            .append(x.buildElement("p")
                                                .append("Abject failure."))
                            .append(x.buildElement("hr"))
                            .append(x.buildElement("address")
                                                .append("Unicorn powered."))
                    )
        ).appendPI("php", "run_finalizer();")
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
}
