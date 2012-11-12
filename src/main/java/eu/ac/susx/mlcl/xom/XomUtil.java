/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package eu.ac.susx.mlcl.xom;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import javax.annotation.Nonnull;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.NodeFactory;
import nu.xom.Nodes;
import nu.xom.ParentNode;
import nu.xom.Serializer;

/**
 *
 * @author hamish
 */
@Nonnull
public class XomUtil {

    public static String toString(Document document) {
        return toString(document, Charset.defaultCharset());
    }

    public static String toString(Document document, Charset charset) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writeDocument(document, out, charset);
            return out.toString(charset.name());
        } catch (IOException ex) {
            throw new AssertionError(ex);
        }
    }

    public static void writeDocument(
            Document document, OutputStream outputStream, Charset encoding)
            throws IOException {

        Preconditions.checkNotNull(document, "xmlDoc");
        Preconditions.checkNotNull(outputStream, "outputStream");
        Preconditions.checkNotNull(encoding, "encoding");

        Serializer ser = new Serializer(outputStream, encoding.name());
        ser.setIndent(2);
        ser.setMaxLength(0);
//        ser.setLineSeparator("\n");
        
        // XXX all elements inherit root elements base uri unless explicitly set (even if an 
        // ancestor overrides that base.) Not sure this is correct.
        ser.setPreserveBaseURI(true);
        ser.write(document);
        ser.flush();
    }

    public static void appendChildren(
            final ParentNode parent,
            final Nodes first,
            final Nodes... remainder) {

        Preconditions.checkNotNull(parent, "parent");
        Preconditions.checkNotNull(first, "first");
        Preconditions.checkNotNull(remainder, "remainder");

        for (int i = 0; i < first.size(); i++)
            parent.appendChild(first.get(i));
        for (Nodes nodes : remainder)
            appendChildren(parent, nodes);

    }

    public static void appendAttributes(
            final Element parent,
            final Nodes first,
            final Nodes... remainder) {

        Preconditions.checkNotNull(parent, "parent");
        Preconditions.checkNotNull(first, "first");
        Preconditions.checkNotNull(remainder, "remainder");

        for (int i = 0; i < first.size(); i++) {
            Preconditions.checkArgument(first.get(i) instanceof Attribute,
                                        "Node contains a node which is not an attribute.");
            parent.addAttribute((Attribute) first.get(i));
        }
        for (Nodes nodes : remainder)
            appendAttributes(parent, nodes);

    }

    public static void moveAttributes(Element from, Element to) {
        Preconditions.checkNotNull(from, "form");
        Preconditions.checkNotNull(to, "to");
        Preconditions.checkArgument(from != to, "form == to");


        for (int i = from.getAttributeCount() - 1; i >= 0; i--) {
            final Attribute a = from.getAttribute(i);
            from.removeAttribute(a);
            to.addAttribute(a);
        }
    }

    public static void moveChildren(ParentNode from, ParentNode to) {
        Preconditions.checkNotNull(from, "form");
        Preconditions.checkNotNull(to, "to");
        Preconditions.checkArgument(from != to, "form == to");

        for (int i = from.getChildCount() - 1; i >= 0; i--)
            to.appendChild(from.removeChild(i));
    }

    public static void move(Nodes from, Nodes to) {
        Preconditions.checkNotNull(from, "form");
        Preconditions.checkNotNull(to, "to");
        Preconditions.checkArgument(from != to, "form == to");

        for (int i = from.size() - 1; i >= 0; i--) {
            to.append(from.remove(i));
        }
    }

    public static void detachChildren(final Nodes first,
                                      final Nodes... remainder) {
        Preconditions.checkNotNull(first, "first");
        Preconditions.checkNotNull(remainder, "remainder");

        for (int i = 0; i < first.size(); i++) {
            Node child = first.get(i);
            ParentNode parent = child.getParent();
            if (parent == null)
                continue;
            parent.removeChild(child);
        }
        for (Nodes nodes : remainder) {
            detachChildren(nodes);
        }
    }

}
