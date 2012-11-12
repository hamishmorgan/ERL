/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package eu.ac.susx.mlcl.xom;

import static com.google.common.base.Preconditions.*;
import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.util.List;
import javax.annotation.Nonnull;
import nu.xom.Attribute;
import nu.xom.DocType;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Namespace;
import nu.xom.Node;
import nu.xom.NodeFactory;
import nu.xom.Nodes;

/**
 * XomB (Xml Object Model Builder, pronounced xombie) is construction tools for
 * XOM XML documents and elements.
 * <p/>
 * The motivation for this tools was a number of perceived problems with the
 * standard method of documents construction using the XOM library. These
 * include:
 *
 * <ul>
 *
 * <li>The factory methods provided by {@link NodeFactory} include parameters
 * that rarely used, resulting in large amounts unnecessary boiler plate code.
 * For example consider {@link NodeFactory#makeAttribute(java.lang.String,
 * java.lang.String, java.lang.String, nu.xom.Attribute.Type) }, the
 * namespaceURI parameter is usually
 * <code>null</code>, and the type parameter is usually
 * <code>CDATA</code>. The
 * <code>XomB</code> mitigates this problem by implemented highly ubiquitous
 * alternatives to constructors.</li>
 *
 * <li>A number of factory methods return type {@link Nodes}. This is because
 * the factory is permitted to replace expected node types with any number of
 * other types. For example a
 * <code>makeAttribute</code> could return elements instead. The
 * <code>Nodes</code> object is a collection without any of normal niceties
 * (such as implementing
 * <code>Iterable</code>); consequently it introduces yet more pointless boiler
 * plate code.
 * <code>XomB</code> handles
 * <code>Nodes</code> internally so the application is simplified
 * considerably.</li>
 *
 * <li>XOM library makes frequent use of
 * <code>null</code>s to indicate the absence of a property, or that the
 * property should have some default value. This is generally bad design and
 * results in errors not being detected until much later, so this builder
 * interface is non-nullable. (For previously nullable strings use the empty
 * string instead.)</li>
 *
 * <li>XOM is unnecessarily weakly typed, with frequent use of String names in
 * place of proper typed instances. For example URIs and charsets where encoded
 * as Strings rather than the classes Java provides. This practice increases the
 * likelihood of hard to diagnose run time errors. This problem has been reduces
 * by demanding the proper classes where appropriate.</li>
 *
 *
 * </ul>
 *
 *
 * Todo:
 *
 *
 * <ul>
 *
 * <li>Addition build() methods, such as build to string, and build DTD.</li>
 *
 * <li>Processing instructions can go anywhere.</li>
 *
 * </ul>
 *
 * @author hamish
 */
@Nonnull
public class XomB {

    public static final URI NULL_URI = URI.create("");

    /**
     * factory used to instantiate all XOM nodes.
     */
    private final NodeFactory factory;

    /**
     * Construct a new XomB instance that will use the given NodeFactory
     * instance to create all XOM nodes.
     * <p/>
     * @param factory used to create nodes
     * @throws NullPointerException if factory is null
     */
    public XomB(NodeFactory factory) {
        checkNotNull(factory, "factory");

        this.factory = factory;
    }

    /**
     * Construct a new XomB instance that will use the default NodeFactory.
     */
    public XomB() {
        this(new NodeFactory());
    }

    /**
     * Get the node factory used by this builder.
     * <p/>
     * @return node factory
     */
    public NodeFactory getFactory() {
        return factory;
    }

    public DocumentBuilder newDocument() {
        return new DocumentBuilder();
    }

    public ElementBuilder newRoot(String name, URI namespace) {
        checkNotNull(name, "name");
        checkNotNull(namespace, "namespace");
        checkArgument(!name.isEmpty(), "argument name is empty");

        return new ElementBuilder(name, namespace, true);
    }

    public ElementBuilder newRoot(String name) {
        return newRoot(name, NULL_URI);
    }

    public ElementBuilder newElement(String name, URI namespace) {
        checkNotNull(name, "name");
        checkNotNull(namespace, "namespace");
        checkArgument(!name.isEmpty(), "argument name is empty");

        return new ElementBuilder(name, namespace, false);
    }

    public ElementBuilder newElement(String name) {
        return newElement(name, NULL_URI);
    }

    /**
     *
     */
    abstract class ParentNodeBuilder<P extends Node> {


        /*
         * ===============================================================
         */
        private URI baseURI = NULL_URI;

        private final ImmutableList.Builder<Node> children;

        ParentNodeBuilder() {
            this.children = ImmutableList.builder();
        }

        public ParentNodeBuilder<P> setBaseURI(URI baseURI) {
            checkNotNull(baseURI, "baseURI");

            this.baseURI = baseURI;
            return this;
        }

        public ParentNodeBuilder<P> clearBaseURI() {
            baseURI = NULL_URI;
            return this;
        }

        URI getBaseURI() {
            return baseURI;
        }

        void addChildren(Nodes nodes) {
            for (int i = 0; i < nodes.size(); i++)
                addChild(nodes.get(i));
        }

        void addChild(Node node) {
            children.add(node);
        }

        List<Node> getChildren() {
            return children.build();
        }

        public abstract P build();
    }

    /**
     *
     */
    public class DocumentBuilder extends ParentNodeBuilder<Document> {

        private boolean docTypeSet;

        private boolean rootElementSet;

        DocumentBuilder() {
            docTypeSet = false;
            rootElementSet = false;
        }

        public DocumentBuilder appendPI(
                final String target, final String data) {
            checkNotNull(target, "target");
            checkNotNull(data, "data");
            checkArgument(!target.isEmpty(), "argument target is empty");
            checkArgument(!data.isEmpty(), "argument data is empty");

            Nodes nodes = factory.makeProcessingInstruction(target, data);
            addChildren(nodes);
            return this;
        }

        public DocumentBuilder setDocType(final String rootElementName,
                                          final String publicID,
                                          final String systemID) {
            checkNotNull(rootElementName, "rootElementName");
            checkNotNull(publicID, "publicID");
            checkNotNull(systemID, "systemID");
            checkArgument(!rootElementName.isEmpty(),
                          "argument rootElementName is empty");
            checkState(!docTypeSet, "DocType has already been set.");

            Nodes nodes = factory.makeDocType(
                    rootElementName, publicID, systemID);
            addChildren(nodes);
            return this;
        }

        public DocumentBuilder setDocType(String rootElementName) {
            checkNotNull(rootElementName, "rootElementName");
            checkState(!docTypeSet, "DocType has already been set.");

            Nodes nodes = factory.makeDocType(rootElementName, null, null);
            addChildren(nodes);
            return this;
        }

        public DocumentBuilder setRoot(ElementBuilder rootElement) {
            checkNotNull(rootElement, "rootElement");

            setRoot(rootElement.build());
            return this;
        }

        public DocumentBuilder setRoot(Element rootElement) {
            checkNotNull(rootElement, "rootElement");
            checkState(!rootElementSet, "Root element has already been set.");

            addChild(rootElement);
            return this;
        }

        public Document build() {
            final Document document = factory.startMakingDocument();

            document.setBaseURI(getBaseURI().toString());

            List<Node> c = getChildren();

            int i = 0;
            while (i < c.size() && !(c.get(i) instanceof Element)) {
                document.insertChild(c.get(i), i);
                ++i;
            }

            if (i < c.size() && (c.get(i) instanceof Element)) {
                document.setRootElement((Element) c.get(i));
                ++i;
            }

            while (i < c.size()) {
                document.insertChild(c.get(i), document.getChildCount());
                ++i;
            }

            factory.finishMakingDocument(document);
            return document;
        }
    }

    /**
     *
     */
    public class ElementBuilder extends ParentNodeBuilder<Element> {

        private final boolean rootElement;

        private final String name;

        private final URI namespace;

        private final ImmutableList.Builder<Attribute> attributes;

        ElementBuilder(String name,
                       URI namespaceURI,
                       boolean rootElement) {
            checkNotNull(name, "name");
            checkArgument(!name.isEmpty(), "argument name is empty");
            checkNotNull(namespaceURI, "namespaceURI");

            this.name = name;
            this.namespace = namespaceURI;
            this.rootElement = rootElement;
            this.attributes = ImmutableList.builder();
        }

        public String getName() {
            return name;
        }

        public ElementBuilder append(final String data) {
            checkNotNull(data, "data");
            checkArgument(!data.isEmpty(), "argument data is empty");

            return append(factory.makeText(data));
        }

        public ElementBuilder append(final ElementBuilder elBuilder) {
            checkNotNull(elBuilder, "elBuilder");

            return append(elBuilder.build());
        }

        public ElementBuilder append(final Element element) {
            checkNotNull(element, "element");

            addChild(element);
            return this;
        }

        public ElementBuilder appendAttribute(Attribute attribute) {
            checkNotNull(attribute, "attribute");
            checkArgument(attribute.getParent() == null,
                          "Argument attribute already has a parent node.");

            attributes.add(attribute);
            return this;
        }

        public ElementBuilder appendAttribute(final String name,
                                              final String value) {
            return appendAttribute(name, NULL_URI, value, Attribute.Type.CDATA);
        }

        public ElementBuilder appendAttribute(
                final String name, final URI namespace,
                final String value, final Attribute.Type type) {
            checkNotNull(name, "name");
            checkArgument(!name.isEmpty(), "argument name is empty");
            checkNotNull(namespace, "namespaceURI");
            checkNotNull(value, "name");
            checkNotNull(type, "type");

            final Nodes nodes = factory.makeAttribute(
                    name, namespace.toString(), value, type);
            return append(nodes);
        }

        public ElementBuilder append(Nodes nodes) {
            checkNotNull(nodes, "nodes");

            for (int i = 0; i < nodes.size(); i++)
                append(nodes.get(i));
            return this;
        }

        public ElementBuilder append(Node node) {
            checkNotNull(node, "node");

            if (node instanceof Namespace) {
                throw new IllegalArgumentException(
                        "Namespace nodes can not be children of anything.");
            } else if (node instanceof DocType) {
                throw new IllegalArgumentException(
                        "DocType nodes can only be appended to document nodes.");
            } else if (node instanceof Document) {
                throw new IllegalArgumentException(
                        "Document can not have a parent.");
            } else if (node instanceof Attribute) {
                attributes.add((Attribute) node);
            } else {
                // Element, Comment, Text, ProcessingInstructiona
                addChild(node);
            }

            return this;
        }

        public ElementBuilder appendProcessingInstruction(
                final String target, final String data) {
            checkNotNull(target, "target");
            checkNotNull(data, "data");
            checkArgument(!target.isEmpty(), "argument target is empty");
            checkArgument(!data.isEmpty(), "argument data is empty");

            Nodes nodes = factory.makeProcessingInstruction(target, data);
            addChildren(nodes);
            return this;
        }

        @Override
        public Element build() {

            final Element element;
            element = rootElement
                    ? factory.makeRootElement(name,
                                              namespace.toString())
                    : factory.startMakingElement(name,
                                                 namespace.toString());

            for (Attribute attribute : attributes.build()) {
                element.addAttribute(attribute);
            }

            for (Node node : getChildren()) {
                element.appendChild(node);
            }

            if (!rootElement)
                factory.finishMakingElement(element);
            return element;
        }
    }
}
