/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.lib.xml;

import com.google.common.base.Preconditions;
import nu.xom.*;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * @author hamish
 */
@Nonnull
public class XomUtil {

    public static String toString(@Nonnull Element element) {
        detachChildren(element);
        return toString(new XomB().document().setRoot(element).build());
    }

    public static String toString(@Nonnull Element element, @Nonnull Charset charset) {
        detachChildren(element);
        return toString(new XomB().document().setRoot(element).build(), charset);
    }

    public static String toString(Document document) {
        return toString(document, Charset.defaultCharset());
    }

    public static String toString(Document document, @Nonnull Charset charset) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writeDocument(document, out, charset);
            return out.toString(charset.name());
        } catch (IOException ex) {
            throw new AssertionError(ex);
        }
    }

    public static void writeDocument(
            Document document, OutputStream outputStream, @Nonnull Charset encoding)
            throws IOException {

        Preconditions.checkNotNull(document, "xmlDoc");
        Preconditions.checkNotNull(outputStream, "outputStream");
        Preconditions.checkNotNull(encoding, "encoding");

        Serializer ser = new Serializer(outputStream, encoding.name());
        ser.setIndent(2);
        ser.setMaxLength(0);
        ser.setLineSeparator("\n");

        // XXX all elements inherit root elements base uri unless explicitly set (even if an
        // ancestor overrides that base.) Not sure this is correct.
        ser.setPreserveBaseURI(true);
        ser.write(document);
        ser.flush();
    }

    public static void appendChildren(
            @Nonnull final ParentNode parent,
            @Nonnull final Nodes first,
            @Nonnull final Nodes... remainder) {

        Preconditions.checkNotNull(parent, "parent");
        Preconditions.checkNotNull(first, "first");
        Preconditions.checkNotNull(remainder, "remainder");

        for (int i = 0; i < first.size(); i++)
            parent.appendChild(first.get(i));
        for (Nodes nodes : remainder)
            appendChildren(parent, nodes);

    }

    public static void appendAttributes(
            @Nonnull final Element parent,
            @Nonnull final Nodes first,
            @Nonnull final Nodes... remainder) {

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

    public static void moveAttributes(@Nonnull Element from, @Nonnull Element to) {
        Preconditions.checkNotNull(from, "form");
        Preconditions.checkNotNull(to, "to");
        Preconditions.checkArgument(from != to, "form == to");


        for (int i = from.getAttributeCount() - 1; i >= 0; i--) {
            final Attribute a = from.getAttribute(i);
            from.removeAttribute(a);
            to.addAttribute(a);
        }
    }

    public static void moveChildren(@Nonnull ParentNode from, @Nonnull ParentNode to) {
        Preconditions.checkNotNull(from, "form");
        Preconditions.checkNotNull(to, "to");
        Preconditions.checkArgument(from != to, "form == to");

        for (int i = from.getChildCount() - 1; i >= 0; i--)
            to.appendChild(from.removeChild(i));
    }

    public static void move(@Nonnull Nodes from, @Nonnull Nodes to) {
        Preconditions.checkNotNull(from, "form");
        Preconditions.checkNotNull(to, "to");
        Preconditions.checkArgument(from != to, "form == to");

        for (int i = from.size() - 1; i >= 0; i--) {
            to.append(from.remove(i));
        }
    }

    public static void detachChildren(@Nonnull final Nodes first,
                                      @Nonnull final Nodes... remainder) {
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

    public static void detachChildren(@Nonnull final Node firstChild,
                                      @Nonnull final Node... remainder) {
        Preconditions.checkNotNull(firstChild, "first");
        Preconditions.checkNotNull(remainder, "remainder");

        ParentNode parent = firstChild.getParent();
        if (parent != null)
            parent.removeChild(firstChild);

        if (remainder.length > 0)
            for (Node node : remainder) {
                detachChildren(node);
            }
    }

    @Nonnull
    public static String getPrintableText(@Nonnull Node node) {
        final StringBuilder builder = new StringBuilder();
        getPrintableText(node, builder);
        return builder.toString();
    }

    public static void getPrintableText(@Nonnull Node node, @Nonnull StringBuilder builder) {
        if (node.getClass().equals(Text.class)) {
            builder.append(((Text) node).getValue());
        } else if (node.getClass().equals(Element.class)) {
            for (int i = 0; i < node.getChildCount(); i++)
                getPrintableText(node.getChild(i), builder);
        } else if (node.getClass().equals(Document.class)) {
            getPrintableText(((Document) node).getRootElement(), builder);
        } else {
            assert node.getClass().equals(Attribute.class)
                    || node.getClass().equals(Comment.class)
                    || node.getClass().equals(DocType.class)
                    || node.getClass().equals(Namespace.class)
                    || node.getClass().equals(ProcessingInstruction.class)
                    || node.getClass().equals(Attribute.class)
                    || node.getClass().equals(ParentNode.class);
        }
    }
}
