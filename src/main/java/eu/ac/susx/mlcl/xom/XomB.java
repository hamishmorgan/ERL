/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package eu.ac.susx.mlcl.xom;

import com.google.common.base.Optional;
import static com.google.common.base.Preconditions.*;
import com.google.common.collect.ImmutableList;
import java.io.OutputStream;
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
 * XomB (Xml Object Model Builder, pronounced xombie) is construction tools for XOM XML documents
 * and elements.
 * <p/>
 * The motivation for this tools was a number of perceived problems with the standard method of
 * documents construction using the XOM library. These include:
 *
 * <ul>
 *
 * <li>The factory methods provided by {@link NodeFactory} include parameters that rarely used,
 * resulting in large amounts unnecessary boiler plate code. For example consider {@link NodeFactory#makeAttribute(java.lang.String,
 * java.lang.String, java.lang.String, nu.xom.Attribute.Type) }, the namespaceURI parameter is
 * usually
 * <code>null</code>, and the type parameter is usually
 * <code>CDATA</code>. The
 * <code>XomB</code> mitigates this problem by implemented highly ubiquitous alternatives to
 * constructors.</li>
 *
 * <li>A number of factory methods return type {@link Nodes}. This is because the factory is
 * permitted to replace expected node types with any number of other types. For example a
 * <code>makeAttribute</code> could return elements instead. The
 * <code>Nodes</code> object is a collection without any of normal niceties (such as implementing
 * <code>Iterable</code>); consequently it introduces yet more pointless boiler plate code.
 * <code>XomB</code> handles
 * <code>Nodes</code> internally so the application is simplified considerably.</li>
 *
 * <li>XOM library makes frequent use of
 * <code>null</code>s to indicate the absence of a property, or that the property should have some
 * default value. This is generally bad design and results in errors not being detected until much
 * later, so this builder interface is non-nullable. (For previously nullable strings use the empty
 * string instead.)</li>
 *
 * <li>XOM is unnecessarily weakly typed, with frequent use of String names in place of proper typed
 * instances. For example URIs and charsets where encoded as Strings rather than the classes Java
 * provides. This practice increases the likelihood of hard to diagnose run time errors. This
 * problem has been reduces by demanding the proper classes where appropriate.</li>
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
 * <li>Not sure the semantics of the API are quite right yet.</li>
 *
 * </ul>
 *
 * @author hamish
 */
@Nonnull
public class XomB {

    /**
     * Rather than use null to represent unset URI values, use this special constant.
     */
    public static final URI NULL_URI = URI.create("");
    /**
     * factory used to instantiate all XOM nodes.
     */
    private final NodeFactory factory;

    /**
     * Construct a new XomB instance that will use the given NodeFactory instance to create all XOM
     * nodes.
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

    public DocumentBuilder buildDocument() {
        return new DocumentBuilder();
    }

    public ElementBuilder buildRoot(final String name, final URI namespace) {
        return buildRoot(name).setNamespace(namespace);
    }

    public ElementBuilder buildRoot(final String name) {
        return new ElementBuilder(name, true);
    }

    public ElementBuilder buildElement(final String name, final URI namespace) {
        return buildElement(name).setNamespace(namespace);
    }

    public ElementBuilder buildElement(final String name) {
        return new ElementBuilder(name, false);
    }

    /**
     * Abstract super class to the node build classes. </p> Handles a collection of child nodes, but
     * does not implement any public API because the constraints for subclasses are quite different.
     *
     * @param <P> product type (the object constructed by this builder)
     * @param <B> builder type (the subclass type of this builder)
     */
    abstract class ParentNodeBuilder<P, B extends ParentNodeBuilder<P, B>> {

        /**
         * Defines a names-space from which all URIs inside are considered to be relative to.
         * <p/>
         * For example, consider the following XHTML snippets, which are equivalent:
         * <pre>
         *      &lt;a href="http://example.org/test#foo"/&gt;
         *      &lt;a xml:base="http://example.org/test" href="#foo"/&gt;
         * </pre>
         */
        private Optional<URI> baseURI = Optional.absent();
        /**
         * Build an immutable list of children.
         */
        private final ImmutableList.Builder<Node> children;

        /**
         * Constructor
         */
        ParentNodeBuilder() {
            this.children = ImmutableList.builder();
        }

        /**
         *
         * @param baseURI
         * @return
         */
        public B setBaseURI(final URI baseURI) {
            this.baseURI = Optional.of(baseURI);
            return (B) this;
        }

        public B clearBaseURI() {
            this.baseURI = Optional.absent();
            return (B) this;
        }

        public B appendPI(final String target, final String data) {
            checkNotNull(target, "target");
            checkNotNull(data, "data");
            checkArgument(!target.isEmpty(), "argument target is empty");

            _addChildren(factory.makeProcessingInstruction(target, data));
            return (B) this;
        }

        Optional<URI> getBaseURI() {
            return baseURI;
        }

        /**
         *
         * @param nodes
         * @throws NullPointerException if nodes is null, or if any element of nodes is null.
         * @throws IllegalArgumentException if any node already has a parent
         */
        void _addChildren(final Nodes nodes) {
            for (int i = 0; i < nodes.size(); i++) {
                _addChild(nodes.get(i));
            }
        }

        /**
         *
         * @param node
         * @throws NullPointerException if node is null
         * @throws IllegalArgumentException if node already has a parent
         */
        void _addChild(final Node node) {
            checkNotNull(node, "node");
            checkArgument(node.getParent() == null, "node argument already has a parent");

            children.add(node);
        }

        List<Node> getChildren() {
            return children.build();
        }

        public abstract P build();
    }

    public class DocumentBuilder extends ParentNodeBuilder<Document, DocumentBuilder> {

        private boolean docTypeSet;
        private boolean rootElementSet;

        /**
         * Constructor should not be called directly. Instead use {@link XomB#buildDocument() }
         * factory method.
         */
        DocumentBuilder() {
            docTypeSet = false;
            rootElementSet = false;
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
            _addChildren(nodes);
            return this;
        }

        public DocumentBuilder setDocType(String rootElementName) {
            checkNotNull(rootElementName, "rootElementName");
            checkState(!docTypeSet, "DocType has already been set.");

            Nodes nodes = factory.makeDocType(rootElementName, null, null);
            _addChildren(nodes);
            return this;
        }

        public DocumentBuilder setRoot(ElementBuilder rootElement) {
            checkNotNull(rootElement, "rootElement");

            Nodes nodes = rootElement.build();
            
            // TODO:
            //   Can contain any number of PIs and comments, but exactly 1 root node
            if (nodes.size() != 1) {
                throw new IllegalArgumentException(
                        "Document must contain exactly 1 Element node, but found " + nodes.size());
            }
            setRoot(nodes.get(1));
            return this;
        }

        public DocumentBuilder setRoot(Element rootElement) {
            checkNotNull(rootElement, "rootElement");
            checkState(!rootElementSet, "Root element has already been set.");

            _addChild(rootElement);
            return this;
        }

        public Document build() {
            final Document document = factory.startMakingDocument();

            if (getBaseURI().isPresent()) {
                document.setBaseURI(getBaseURI().get().toString());
            }

            /*
             * Rather than just appending all the child elements, we need to explicitly call the
             * setRootElement method on the document exactly once. Any nodes before or after must 
             * be inserted before and after the root element.
             */

            final List<Node> children = getChildren();

            int i = 0;
            while (i < children.size() && !(children.get(i) instanceof Element)) {
                if (children.get(i) instanceof DocType) {
                    document.setDocType((DocType) children.get(i));
                } else {
                    document.insertChild(children.get(i), i);
                }
                ++i;
            }

            if (i < children.size() && (children.get(i) instanceof Element)) {
                document.setRootElement((Element) children.get(i));
                ++i;
            }

            while (i < children.size()) {
                if (children.get(i) instanceof DocType) {
                    document.setDocType((DocType) children.get(i));
                } else {
                    document.insertChild(children.get(i), i);
                }
                ++i;
            }

            factory.finishMakingDocument(document);
            return document;
        }
    }

    /**
     *
     */
    public class ElementBuilder extends ParentNodeBuilder<Nodes, ElementBuilder> {

        /**
         * Whether or not the element being built is expected to be root element.
         *
         * Since the XOM NodeFactory provides separate methods for the construction of normal
         * elements vs. root elements, we need to insure the correct method is called. Most of the
         * time this will make no difference, but it still needs to happen.
         */
        private final boolean rootElement;
        /**
         * The elements unqualified name.
         */
        private String localName;
        /**
         * The namespace prefix for the given name.
         */
        private Optional<String> prefix = Optional.absent();
        /**
         * The elements name-space.
         */
        private Optional<URI> namespace;
        /**
         * Construct a list of the attributes of this element.
         */
        private final ImmutableList.Builder<Attribute> attributes;

        /**
         * Constructor should not be called directly. Instead use {@link XomB } factory methods:
         * null null null null null null null null null null null null null         {@link XomB#buildElement(java.lang.String) }
         * {@link XomB#buildElement(java.lang.String, java.net.URI) },
         * {@link XomB#buildRoot(java.lang.String) }, and
         * {@link XomB#buildRoot(java.lang.String, java.net.URI) }.
         *
         * @param name the qualified element name
         * @param rootElement whether or not this element is expected to be a root element.
         * @throws NullPointerException if name is null
         * @throws IllegalArgumentException if name is empty
         */
        ElementBuilder(final String name, final boolean rootElement) {
            checkNotNull(name, "name");
            checkArgument(!name.isEmpty(), "argument name is empty");

            final int colon = name.indexOf(':');
            if (colon > 0) {
                setPrefix(name.substring(0, colon));
                setLocalName(name.substring(colon + 1));
            } else {
                setLocalName(name);
            }

            this.rootElement = rootElement;
            this.attributes = ImmutableList.builder();
            this.namespace = Optional.absent();
        }

        /**
         *
         * I didn't want to make this (or indeed any accessors) public, but certain problems require
         * knowing the name of the parent element from that parents builder.
         *
         * @return
         */
        public String getLocalName() {
            return localName;
        }

        /**
         *
         * @param namespace
         * @return ElementBuilder instance of method chaining
         * @throws NullPointerException if namespace is null
         */
        public ElementBuilder setNamespace(final URI namespace) {
            this.namespace = Optional.of(namespace);
            return this;
        }

        /**
         *
         * @return ElementBuilder instance of method chaining
         */
        public ElementBuilder clearNamespace() {
            this.namespace = Optional.absent();
            return this;
        }

        /**
         *
         * @param prefix
         * @return ElementBuilder instance of method chaining
         * @throws NullPointerException if prefix is null
         * @throws IllegalArgumentException if prefix is empty
         */
        public final ElementBuilder setPrefix(String prefix) {
            checkArgument(!prefix.isEmpty(), "prefix is empty");

            this.prefix = Optional.of(prefix);
            return this;
        }

        public final ElementBuilder clearPrefix() {
            this.prefix = Optional.absent();
            return this;
        }

        /**
         *
         * @param localName
         * @return ElementBuilder instance of method chaining
         * @throws NullPointerException if localName is null
         * @throws IllegalArgumentException if localName is empty
         */
        public final ElementBuilder setLocalName(String localName) {
            checkArgument(!localName.isEmpty(), "argument localName is empty");

            this.localName = localName;
            return this;
        }

        /**
         *
         * @param data
         * @return ElementBuilder instance of method chaining
         */
        public ElementBuilder append(final String data) {
            checkNotNull(data, "data");
            checkArgument(!data.isEmpty(), "argument data is empty");

            _addChildren(factory.makeText(data));
            return this;
        }

        /**
         *
         * @param elBuilder
         * @return ElementBuilder instance of method chaining
         */
        public ElementBuilder append(final ElementBuilder elBuilder) {
            checkNotNull(elBuilder, "elBuilder");

            _addChildren(elBuilder.build());
            return this;
        }

        /**
         *
         * @param element
         * @return ElementBuilder instance of method chaining
         */
        public ElementBuilder append(final Element element) {
            checkNotNull(element, "element");

            _addChild(element);
            return this;
        }

        /**
         *
         * @param attribute
         * @return ElementBuilder instance of method chaining
         */
        public ElementBuilder appendAttribute(Attribute attribute) {
            checkNotNull(attribute, "attribute");
            checkArgument(attribute.getParent() == null,
                          "Argument attribute already has a parent node.");

            attributes.add(attribute);
            return this;
        }

        /**
         *
         * @param name
         * @param value
         * @return ElementBuilder instance of method chaining
         */
        public ElementBuilder appendAttribute(final String name,
                                              final String value) {
            return appendAttribute(name, NULL_URI, value, Attribute.Type.CDATA);
        }

        /**
         *
         * @param name
         * @param namespace
         * @param value
         * @param type
         * @return ElementBuilder instance of method chaining
         */
        public ElementBuilder appendAttribute(
                final String name, final URI namespace,
                final String value, final Attribute.Type type) {
            checkNotNull(name, "name");
            checkArgument(!name.isEmpty(), "argument name is empty");
            checkNotNull(namespace, "namespaceURI");
            checkNotNull(value, "name");
            checkNotNull(type, "type");

            Nodes nodes = factory.makeAttribute(name, namespace.toString(), value, type);
            for (int i = 0; i < nodes.size(); i++) {
                append(nodes.get(i));
            }
            return this;
        }

        /**
         *
         * @param node
         * @return ElementBuilder instance of method chaining
         * @throws NullPointerException if node is null
         * @throws IllegalArgumentException if node is a Namespace, DocType or Document, or node
         * already has a parent.
         */
        public ElementBuilder append(final Node node) {
            checkNotNull(node, "node");
            if (node instanceof Namespace || node instanceof DocType || node instanceof Document) {
                throw new IllegalArgumentException(
                        "element node can not have child notes of type "
                        + node.getClass().getSimpleName());

            } else if (node instanceof Attribute) {
                attributes.add((Attribute) node);
            } else { // Element, Comment, Text, ProcessingInstructiona
                _addChild(node);
            }
            return this;
        }

        /**
         *
         * @return newly constructed Element instance
         */
        @Override
        public Nodes build() {

            final String qualifiedName = prefix.isPresent()
                    ? prefix.get().toString() + ":" + localName
                    : localName;

            final String namespaceStr = namespace.isPresent()
                    ? namespace.get().toString()
                    : NULL_URI.toString();

            System.out.println(qualifiedName + " ... " + namespaceStr);

            final Element element = rootElement
                    ? factory.makeRootElement(qualifiedName, namespaceStr)
                    : factory.startMakingElement(qualifiedName, namespaceStr);

            if (getBaseURI().isPresent()) {
                element.setBaseURI(getBaseURI().get().toString());
            }

            for (final Attribute attribute : attributes.build()) {
                element.addAttribute(attribute);
            }

            for (final Node node : getChildren()) {
                element.appendChild(node);
            }

            // XXX not sure if finishMarkingElement is supposed to be called
//            if (!rootElement) {
            return factory.finishMakingElement(element);
//            }
//            return element;
        }
    }
}
