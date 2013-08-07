/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.xml;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.time.Timex;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Factory;
import edu.stanford.nlp.util.Filter;
import edu.stanford.nlp.util.Filters;
import nu.xom.*;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.erl.snlp.InstancePool;
import uk.ac.susx.mlcl.erl.xml.XomB.DocumentBuilder;
import uk.ac.susx.mlcl.erl.xml.XomB.ElementBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Save annotations to XML
 * <p/>
 * Mostly Ripped form {@link StanfordCoreNLP}, but modified to support our own annotations.
 * <p/>
 * <
 * p/> TODO: Class ParserAnnotations can't be found for some reason, so it's not in the searchspace.
 * <p/>
 *
 * @author hamish
 */
@Nonnull
@Immutable
public class AnnotationToXML {

    /**
     * Namespace URI (which may be null or the empty string)
     */
    @Nullable
    private final String namespaceURI;
    /**
     * Name of the style sheet used from XML to XHTML transformation
     */
    private final String stylesheetName;
    /**
     * Map from CoreAnnotation types to simplified elements names used in the XML. Without
     * simplified names the full canonical class name will used.
     */
    private final Map<Class<? extends CoreAnnotation<?>>, String> simpleNamesMap;
    /**
     * Map from CoreAnnotation types to special custom serializes that will be called for all
     * annotations of that type. This allows special behavior for particular annotators such as the
     * CorefChainAnnotation.
     */
//    private final Map<Class<? extends CoreAnnotation<?>>, Class<? extends CustomGenerator<?>>> customSerializers;
    private final InstancePool<Class<? extends CoreAnnotation<?>>, ? extends CustomGenerator<?>> customSerializerPool;
    //        private final TypeToInstanceMap<?> customSerializerPool;
    private final Filter<Class<? extends CoreAnnotation<?>>> annotationFilter;
    private final Set<String> nodeFilters;
    private final XomB x;

    /**
     * Dependence inject constructor. Use the associated builder {
     *
     * @param nodeFactory
     * @param namespaceURI      Namespace URI
     * @param stylesheetName    Name of the style sheet used from XML to XHTML transformation
     * @param simpleNames       Map from CoreAnnotation types to simplified elements names used in the XML
     * @param customSerializers Map from CoreAnnotation types to special custom serializes that will
     *                          be called for all annotations of that type.
     * @param nodeFilters
     * @param annotationFilter
     * @throws NullPointerException if stylesheetName, simpleNames, or customSerializers is null
     * @link AnnotationToXML#builder()} < p/>
     * <p/>
     */
    protected AnnotationToXML(
            NodeFactory nodeFactory,
            @Nullable String namespaceURI,
            String stylesheetName,
            Map<Class<? extends CoreAnnotation<?>>, String> simpleNames,
            InstancePool<Class<? extends CoreAnnotation<?>>, ? extends CustomGenerator<?>> customSerializers,
            //                    
            //            Map<Class<? extends CoreAnnotation<?>>, Class<? extends CustomGenerator<?>>> customSerializers,
            Filter<Class<? extends CoreAnnotation<?>>> annotationFilter,
            Set<String> nodeFilters) {
        Preconditions.checkNotNull(stylesheetName, "stylesheetName");
        Preconditions.checkNotNull(simpleNames, "simpleNames");
        Preconditions.checkNotNull(customSerializers, "customSerializers");

        this.x = new XomB(nodeFactory);
        this.namespaceURI = namespaceURI;
        this.stylesheetName = stylesheetName;
        this.simpleNamesMap = simpleNames;
//        this.customSerializers = customSerializers;
        this.annotationFilter = annotationFilter;
        this.nodeFilters = nodeFilters;
        this.customSerializerPool = customSerializers;
    }

    /**
     * @return
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Wrapper around xmlPrint(Annotation, OutputStream). Added for backward compatibility.
     * <p/>
     *
     * @param annotation
     * @param writer     The Writer to send the output to
     * @throws IOException
     */
    public void xmlPrint(Annotation annotation, Writer writer) throws IOException, InstantiationException {
        Preconditions.checkNotNull(annotation, "annotation");
        Preconditions.checkNotNull(writer, "writer");

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        xmlPrint(annotation, os);
        writer.write(new String(os.toByteArray(), "UTF-8"));
        writer.flush();
    }

    /**
     * Displays the output of all annotators in XML format.
     * <p/>
     *
     * @param annotation   Contains the output of all annotators
     * @param outputStream The output stream
     * @throws IOException
     */
    public void xmlPrint(Annotation annotation, OutputStream outputStream)
            throws IOException, InstantiationException {
        Preconditions.checkNotNull(annotation, "annotation");
        Preconditions.checkNotNull(outputStream, "outputStream");

        xmlPrint(toDocument(annotation), outputStream);
    }

    public void xmlPrint(Document xmlDoc, OutputStream outputStream) throws IOException {
        Preconditions.checkNotNull(xmlDoc, "xmlDoc");
        Preconditions.checkNotNull(outputStream, "outputStream");

        Serializer ser = new Serializer(outputStream, "UTF-8");
        ser.setIndent(2);
        ser.setMaxLength(0);
        ser.write(xmlDoc);
        ser.flush();
    }

    /**
     * Converts the given annotation to an XML document
     * <p/>
     *
     * @param annotation
     * @return
     * @throws InstantiationException
     */
    public Document toDocument(Annotation annotation)
            throws InstantiationException {
        Preconditions.checkNotNull(annotation, "annotation");


        DocumentBuilder xmlDoc = x.document();

        if (stylesheetName != null && !stylesheetName.isEmpty()) {
            xmlDoc.addPI("xml-stylesheet",
                    "href=\"" + stylesheetName + "\" type=\"text/xsl\"");
        }

        ElementBuilder root = x.root("root");


//        Element root = nodeFactory.makeRootElement("root", namespaceURI);

        ElementBuilder docElem = x.element("document");

        addCoreMap(docElem, annotation);

        root.add(docElem);

        xmlDoc.setRoot(root);

        Document doc = xmlDoc.build();
        for (String filter : nodeFilters) {
            filterDocument(doc, filter);
        }

        return doc;
    }

    static void filterDocument(Document doc, String xpathFilter) {

        final Nodes nodes = doc.query(xpathFilter);
        if (nodes != null) {
            XomUtil.detachChildren(nodes);
        }

    }

    /**
     * @param parent
     * @param value
     */
    private void addElementByValueType(ElementBuilder parent, Object value)
            throws InstantiationException {
        Preconditions.checkNotNull(parent, "parent");
        Preconditions.checkNotNull(value, "value");

        if (value instanceof CharSequence) {
            addString(parent, (CharSequence) value);
        } else if (value instanceof Number) {
            addNumber(parent, (Number) value);
        } else if (value instanceof Boolean) {
            addBoolean(parent, (Boolean) value);
        } else if (value instanceof List<?>) {
            addListElements(parent, (List<?>) value);
        } else if (value instanceof CoreMap) {
            addCoreMap(parent, (CoreMap) value);
        } else {
            System.err.println("Unknown value type: " + value.getClass());
            addString(parent, value.toString());
        }
    }

    private void addString(@Nonnull ElementBuilder parent,
                           @Nonnull CharSequence value) {
        parent.add(value.toString());
    }

    private void addNumber(@Nonnull ElementBuilder parent, @Nonnull Number value) {
        addString(parent, value.toString());
    }

    private void addBoolean(@Nonnull ElementBuilder parent,
                            @Nonnull Boolean value) {
        addString(parent, Boolean.toString(value));
    }

    private static final Morphology morph = new Morphology();

    private void addListElements(@Nonnull ElementBuilder parent,
                                 @Nonnull List<?> childValues) throws InstantiationException {

        // The list element itself will be named using the normal shortening system. Member 
        // elements of the list will attempt to use a singular variant of the list name if possible.
        String singularName;
        synchronized (morph) {
            singularName = morph.stem(parent.getLocalName());
        }
        if (singularName.equals(parent.getLocalName())) {
            singularName += "i";
        }

        int count = 0;
        for (Object value : childValues) {
            count++;

            ElementBuilder itemElement = x.element(singularName);

            itemElement.addAttribute("id", Integer.toString(count));

            if (value != null) {
                addElementByValueType(itemElement, value);
            }


            parent.add(itemElement);
        }
    }

    private void addCoreMap(@Nonnull ElementBuilder parent,
                            @Nonnull CoreMap map) throws InstantiationException {
        for (Class<?> key : map.keySet()) {
            if (!CoreAnnotation.class.isAssignableFrom(key)) {
                throw new AssertionError(
                        "Key is not an instance of CoreAnnotation.");
            }

            @SuppressWarnings("unchecked")
            final Class<? extends CoreAnnotation<?>> castKey =
                    (Class<? extends CoreAnnotation<?>>) key;

            if (!annotationFilter.accept(castKey)) {
                continue;
            }


            final String name = (simpleNamesMap.containsKey(castKey))
                    ? simpleNamesMap.get(castKey)
                    : castKey.getCanonicalName();


            final ElementBuilder element = x.element(name);

            boolean found = false;
            for (Iterator<Class<? extends CoreAnnotation<?>>> it =
                         customSerializerPool.keySet().iterator();
                 it.hasNext(); ) {

                Class<? extends CoreAnnotation<?>> k = it.next();
                if (k.isAssignableFrom(key)) {

                    CustomGenerator serializer = customSerializerPool.getInstance(k);

                    final Object value = map.get((Class<? extends CoreAnnotation>) castKey);
                    serializer.generate(x, element, value);
                    found = true;
                    break;
                }
            }
            if (!found) {

                final Object value = map.get(
                        (Class<? extends CoreAnnotation>) castKey);
                if (value != null) {
                    addElementByValueType(element, value);
                }
            }

            parent.add(element);
        }
    }

    private void addMap(ElementBuilder parent, Map<String, ?> map) throws InstantiationException {
        for (String key : map.keySet()) {

            final ElementBuilder element = x.element(key);

            final Object value = map.get(key);
            if (value != null) {
                addElementByValueType(element, value);
            }

            parent.add(element);
        }
    }

    public interface CustomGenerator<T> {

        void generate(XomB builder, @Nonnull ElementBuilder element,
                      @Nonnull T value);
    }

    public static class TimexGenerator implements CustomGenerator<Timex> {

        private boolean useTimexXml = false;

        public void generate(XomB x, @Nonnull ElementBuilder parent,
                             Timex value) {

            if (useTimexXml) {
                Element timexXml = value.toXmlElement();


                for (int i = timexXml.getAttributeCount() - 1; i >= 0; i--) {
                    final Attribute a = timexXml.getAttribute(i);
                    a.detach();
                    parent.addAttribute(a);
                }
                for (int i = timexXml.getChildCount() - 1; i >= 0; i--) {
                    parent.add(timexXml.removeChild(i));
                }

            } else {

                parent.addAttribute("tid", value.tid())
                        .addAttribute("type", value.timexType())
                        .add(value.value());
            }
        }

        public static class Factory
                implements edu.stanford.nlp.util.Factory<TimexGenerator> {

            private static final long serialVersionUID = 1L;

            public TimexGenerator create() {
                return new TimexGenerator();
            }
        }
    }

    public static class TreeGenerator implements CustomGenerator<Tree> {

        private static TreePrint constituentTreePrinter = new TreePrint("penn");

        public void generate(XomB x, @Nonnull ElementBuilder parent,
                             @Nonnull Tree tree) {
            StringWriter treeStrWriter = new StringWriter();
            constituentTreePrinter.printTree(
                    tree, new PrintWriter(treeStrWriter, true));
            parent.add(treeStrWriter.toString());
        }

        public static class Factory
                implements edu.stanford.nlp.util.Factory<TreeGenerator> {

            private static final long serialVersionUID = 1L;

            public TreeGenerator create() {
                return new TreeGenerator();
            }
        }
    }

    public static class SemanticGraphGenerator implements CustomGenerator<SemanticGraph> {

        public void generate(XomB x, @Nonnull ElementBuilder parent,
                             @Nonnull SemanticGraph graph) {


            for (SemanticGraphEdge edge : graph.edgeListSorted()) {
                String rel = edge.getRelation()
                        .toString().replaceAll("\\s+", "");
                String sourceIndex = Integer.toString(edge.getSource().index());
                String targetIndex = Integer.toString(edge.getTarget().index());
                String sourceString = edge.getSource().word();
                String targetString = edge.getTarget().word();

                parent.add(x.element("dep")
                        .addAttribute("type", rel)
                        .add(x.element("governor")
                                .addAttribute("idx", sourceIndex)
                                .add(sourceString))
                        .add(x.element("dependent")
                                .addAttribute("idx", targetIndex)
                                .add(targetString)));
            }

        }

        public static class Factory
                implements edu.stanford.nlp.util.Factory<SemanticGraphGenerator> {

            private static final long serialVersionUID = 1L;

            public SemanticGraphGenerator create() {
                return new SemanticGraphGenerator();
            }
        }
    }

    public static class CorefGenerator implements CustomGenerator<Map<Integer, CorefChain>> {

        /**
         * Generates the XML content for the coreference chain object
         * <p/>
         *
         * @param x
         * @param parent
         * @param corefChains
         */
        public void generate(XomB x, @Nonnull ElementBuilder parent,
                             @Nonnull Map<Integer, CorefChain> corefChains) {
//            String curNS = corefInfo.getNamespaceURI();
            boolean foundCoref = false;
            for (CorefChain chain : corefChains.values()) {

                if (chain.getCorefMentions().size() <= 1) {
                    continue;
                }
                foundCoref = true;

                ElementBuilder chainElem = x.element("coreference");

                CorefChain.CorefMention source = chain
                        .getRepresentativeMention();

                addCorefMention(x, chainElem, source, true);

                for (CorefChain.CorefMention mention : chain.getCorefMentions()) {
                    if (mention == source) {
                        continue;
                    }
                    addCorefMention(x, chainElem, mention, false);
                }

                parent.add(chainElem);
            }
//            return foundCoref;
        }

        private void addCorefMention(XomB x, @Nonnull ElementBuilder chainElem,
                                     @Nonnull CorefChain.CorefMention mention,
                                     boolean representative) {

            ElementBuilder mentionElem = x.element("mention")
                    .add(x.element("sentence")
                            .add(Integer.toString(mention.sentNum)))
                    .add(x.element("start")
                            .add(Integer.toString(mention.startIndex)))
                    .add(x.element("end")
                            .add(Integer.toString(mention.endIndex)))
                    .add(x.element("head")
                            .add(Integer.toString(mention.headIndex)));

            if (representative) {
                mentionElem.addAttribute("representative", "true");
            }

            chainElem.add(mentionElem);
        }

        public static class Factory
                implements edu.stanford.nlp.util.Factory<CorefGenerator> {

            private static final long serialVersionUID = 1L;

            public CorefGenerator create() {
                return new CorefGenerator();
            }
        }
    }

    /**
     *
     */
    public static class Builder {

        public static final String NO_STYLESHEET = "";
        private static final Log LOG = LogFactory.getLog(Builder.class);
        private final Set<Class<?>> annotationRoots = Sets.newHashSet();
        private final Set<String> stripSuffixes = Sets.newHashSet();
        private final InstancePool.Builder<Class<? extends CoreAnnotation<?>>, CustomGenerator<?>> customSerializerPool;
        private final BiMap<Class<? extends CoreAnnotation<?>>, String> simpleNames;
        private String namespaceURI = null;
        private String stylesheetName = NO_STYLESHEET;
        private NodeFactory nodeFactory = null;
        private final ImmutableSet.Builder<Class<? extends CoreAnnotation<?>>> annoBlacklist;
        private final ImmutableSet.Builder<String> xpathNodeFilters;

        public Builder() {
            xpathNodeFilters = ImmutableSet.builder();
            annoBlacklist = ImmutableSet.builder();
            simpleNames = HashBiMap.create();
            customSerializerPool = InstancePool.builder();
            nodeFactory = new NodeFactory();
        }

        public AnnotationToXML build() {

            if (!annotationRoots.isEmpty()) {

                final Set<Class<? extends CoreAnnotation>> candidates =
                        findMembersOfType(annotationRoots, CoreAnnotation.class);
                for (Class<? extends CoreAnnotation> candidate : candidates) {
                    if (!simpleNames.containsKey(candidate)) {
                        String name = simplifiedName(candidate,
                                stripSuffixes, simpleNames
                                .values());
                        simpleNames.put(
                                (Class<? extends CoreAnnotation<?>>) candidate,
                                name);
                    }
                }

            }

            final Filter<Class<? extends CoreAnnotation<?>>> annotationBlacklistFilter =
                    Filters.collectionRejectFilter(annoBlacklist.build());


            return new AnnotationToXML(
                    nodeFactory,
                    namespaceURI,
                    stylesheetName,
                    simpleNames,
                    customSerializerPool.build(),
                    annotationBlacklistFilter,
                    xpathNodeFilters.build());
        }

        public Builder addAnnotationRoot(Class<?> annotationRoot) {
            annotationRoots.add(annotationRoot);
            return this;
        }

        public Builder addXPathNodeFilter(String xpathFilter) {
            xpathNodeFilters.add(xpathFilter);
            return this;
        }

        public Builder addAnnotationToIgnore(
                Class<? extends CoreAnnotation<?>> annotationClass) {
            annoBlacklist.add(annotationClass);
            return this;
        }

        public Builder addStripSuffix(String suffix) {
            stripSuffixes.add(suffix);
            return this;
        }

        public Builder addSimplifiedName(
                Class<? extends CoreAnnotation<?>> annotationType,
                String name) {
            simpleNames.put(annotationType, name);
            return this;
        }

        public void setNamespaceURI(String namespaceURI) {
            this.namespaceURI = namespaceURI;
        }

        public void setStylesheetName(String stylesheetName) {
            this.stylesheetName = stylesheetName;
        }

        public void setNodeFactory(NodeFactory nodeFactory) {
            this.nodeFactory = nodeFactory;
        }

        /**
         * Search (breadth first) the membership tree(s) of any classes that are subclasses of the
         * given {@code targetType}. The search is started at all of the given root nodes.
         * <p/>
         *
         * @param <T>
         * @param searchRoots
         * @param targetType
         * @return
         * @throws SecurityException    caused by {@link Class#getClasses() }
         * @throws NullPointerException if any parameter is null, or contains null
         */
        @SuppressWarnings("unchecked")
        private static <T> Set<Class<? extends T>> findMembersOfType(
                Set<Class<?>> searchRoots, Class<T> targetType)
                throws SecurityException, NullPointerException {
            Preconditions.checkNotNull(searchRoots, "searchRoots");
            Preconditions.checkNotNull(targetType, "targetType");

            final Set<Class<? extends T>> found =
                    new HashSet<Class<? extends T>>();

            // Set of classes that have already been visited. This is barely required, since
            // membership is a directed acyclic graph (tree), we generally won't encounter the same
            // class more than once. The only exception is when the starting node list contains
            // ancestors.
            final Set<Class<?>> done = new HashSet<Class<?>>();

            // Queue of classes that have yet to be processed.
            final Queue<Class<?>> todo = new ArrayDeque<Class<?>>(
                    Sets.newHashSet(searchRoots));

            while (!todo.isEmpty()) {

                final Class<?> clazz = todo.poll();

                if (targetType.isAssignableFrom(clazz)) {
                    found.add((Class<? extends T>) clazz);
                }

                done.add(clazz);

                Set<Class<?>> members = Sets.newHashSet(Arrays.asList(clazz
                        .getClasses()));
                members = Sets.difference(members, done);
                todo.addAll(members);
            }

            return found;
        }

        private static String simplifiedName(Class<?> clazz,
                                             Set<String> stripSuffixes,
                                             Set<String> existingNames) {
            Preconditions.checkNotNull(clazz, "clazz");
            Preconditions.checkNotNull(stripSuffixes, "stripSuffixes");
            Preconditions.checkNotNull(existingNames, "existingNames");
            Preconditions.checkArgument(!clazz.isPrimitive(), "Class is primitive.");
            Preconditions.checkArgument(!clazz.isArray(), "Class is primitive.");

            // XXX Move to static constant
            Pattern NS_DELIMITER = Pattern.compile("\\.");
            Pattern MEMBER_DELIMITER = Pattern.compile("\\$");

            String className = clazz.getCanonicalName();

            if (className == null) {
                className = MEMBER_DELIMITER.matcher(clazz.getName())
                        .replaceAll(".");
            }

            final String[] nameParts = NS_DELIMITER.split(className);

            final StringBuilder name = new StringBuilder();
            int i = nameParts.length - 1;
            do {

                String part = nameParts[i];

                // Strip suffix if it's present
                for (String stripSuffix : stripSuffixes) {
                    if (part.endsWith(stripSuffix)
                            && part.length() > stripSuffix.length()) {
                        part = part.substring(0, part.length() - stripSuffix
                                .length());
                    }
                }
                // Change first character to lower case
                if (Character.isUpperCase(part.charAt(0))) {
                    part = Character.toLowerCase(part.charAt(0))
                            + (part.length() > 1 ? part.substring(1) : "");
                }

                if (name.length() > 0) {
                    name.insert(0, '.');
                }
                name.insert(0, part);

                i--;
            } while (i >= 0 && existingNames.contains(name.toString()));

            if (i < 0) {
                // Somehow we have a direct namespace collusion after simplification
                // so just number the name

                int number = 2;
                name.append('.');
                final int mark = name.length();
                name.append(Integer.toString(number));
                while (existingNames.contains(name.toString())) {
                    name
                            .replace(mark, name.length(), Integer.toString(
                                    ++number));
                }
            }

            return name.toString();
        }

        public void configure(Configuration config)
                throws ClassNotFoundException, InstantiationException, IllegalAccessException {


            if (config.containsKey("annotationRoots")) {
                String[] roots = config.getStringArray("annotationRoots");
                for (String className : roots) {
                    try {

                        Class<?> clazz = Class.forName(className);
                        addAnnotationRoot(clazz);
                    } catch (ClassNotFoundException ex) {
                        LOG.warn("Failed to load class for name: " + className,
                                ex);
                    }
                }
            }

            if (config.containsKey("simplify")) {
                String[] items = config.getStringArray("simplify");

                for (String item : items) {
//                    System.out.println(item);
                    final int i = item.indexOf(':');
                    String className = item.substring(0, i).trim();
                    String simpleName = item.substring(i + 1).trim();
                    try {
                        Class<? extends CoreAnnotation<?>> clazz =
                                (Class<? extends CoreAnnotation<?>>) Class
                                        .forName(className);
                        addSimplifiedName(clazz, simpleName);
                    } catch (ClassCastException ex) {
                        LOG.warn("Class does not extend CoreAnnotation: "
                                + className, ex);
                    } catch (ClassNotFoundException ex) {
                        LOG.warn("Failed to load class for name: " + className,
                                ex);
                    }
                }

            }

            if (config.containsKey("stripSuffixes")) {
                String[] roots = config.getStringArray("stripSuffixes");
                for (String suffix : roots) {
                    addStripSuffix(suffix.trim());
                }
            }

            if (config.containsKey("nodeFilters")) {
                String[] items = config.getStringArray("nodeFilters");
                for (String filter : items) {
                    addXPathNodeFilter(filter);
                }
            }


            if (config.containsKey("annoBlacklist")) {
                String[] classNames = config.getStringArray("annoBlacklist");
                for (String className : classNames) {
                    try {
                        Class<? extends CoreAnnotation<?>> clazz =
                                (Class<? extends CoreAnnotation<?>>) Class
                                        .forName(className);
                        addAnnotationToIgnore(clazz);
                    } catch (ClassCastException ex) {
                        LOG.warn("Class does not extend CoreAnnotation: "
                                + className, ex);
                    } catch (ClassNotFoundException ex) {
                        LOG.warn("Failed to load class for name: " + className,
                                ex);
                    }
                }
            }


            if (config.containsKey("namespaceURI")) {
                setNamespaceURI(config.getString("namespaceURI"));
            }


            if (config.containsKey("stylesheetName")) {
                setStylesheetName(config.getString("stylesheetName"));
            }


            if (config.containsKey("customSerializers")) {
                String[] items = config.getStringArray("customSerializers");

                for (String item : items) {
//                    System.out.println(item);
                    String[] parts = item.split(":");
                    final int i = item.indexOf(':');
                    String annotationClassName = parts[0].trim();
                    String serializerClassName = parts[1].trim();
                    String serializerFactoryClassName = parts[2].trim();
                    try {

                        Class annotationClass = Class.forName(
                                annotationClassName);
                        Class serializerClass = Class.forName(
                                serializerClassName);
                        Factory factory = (Factory) Class.forName(
                                serializerFactoryClassName)
                                .newInstance();

                        customSerializerPool.addFactory(
                                annotationClass,
                                serializerClass,
                                factory);

                    } catch (ClassCastException ex) {
                        LOG.warn("Class does not extend CoreAnnotation: "
                                + annotationClassName, ex);
                    } catch (ClassNotFoundException ex) {
                        LOG.warn("Failed to load class for name: "
                                + annotationClassName, ex);
                    }
                }

            }

        }
    }
}
