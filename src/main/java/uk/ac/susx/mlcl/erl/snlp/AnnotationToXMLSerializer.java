/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.snlp;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import edu.stanford.nlp.dcoref.CoNLL2011DocumentReader;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations;
import edu.stanford.nlp.dcoref.Mention;
import edu.stanford.nlp.ie.machinereading.structure.MachineReadingAnnotations;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.tokensregex.types.Tags;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.TimeExpression;
import edu.stanford.nlp.time.Timex;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.international.pennchinese.ChineseGrammaticalRelations;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Filter;
import edu.stanford.nlp.util.Filters;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.NodeFactory;
import nu.xom.Nodes;
import nu.xom.Serializer;

/**
 * Save annotations to XML
 * <p/>
 * Mostly Ripped form {@link StanfordCoreNLP}, but modified to support our own annotations.
 * <p/>
 * <
 * p/> TODO: Class ParserAnnotations can't be found for some reason, so it's not in the searchspace.
 * <p/>
 * @author hamish
 */
@Nonnull
public class AnnotationToXMLSerializer {

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
    private final Map<Class<? extends CoreAnnotation>, String> simpleNames;

    /**
     * Map from CoreAnnotation types to special custom serializes that will be called for all
     * annotations of that type. This allows special behavior for particular annotators such as the
     * CorefChainAnnotation.
     */
    private final Map<Class<? extends CoreAnnotation<?>>, XmlSerializer<?>> customSerializers;

    private final Filter<Class<? extends CoreAnnotation>> annotationFilter;
//    private final Filter<Class<? extends CoreAnnotation>> annotationFilter;

    private final Filter<Node> nodeFilter;

    private final NodeFactory nodeFactory = new NodeFactory();

    /**
     * Dependence inject constructor. Use the associated builder {
     * @link AnnotationToXMLSerializer#builder()} < p/>
     * <p/>
     * @param namespaceURI      Namespace URI
     * @param stylesheetName    Name of the style sheet used from XML to XHTML transformation
     * @param simpleNames       Map from CoreAnnotation types to simplified elements names used in
     *                          the XML
     * @param customSerializers Map from CoreAnnotation types to special custom serializes that will
     *                          be called for all annotations of that type.
     * @throws NullPointerException if stylesheetName, simpleNames, or customSerializers is null
     */
    protected AnnotationToXMLSerializer(
            @Nullable String namespaceURI,
            String stylesheetName,
            Map<Class<? extends CoreAnnotation>, String> simpleNames,
            Map<Class<? extends CoreAnnotation<?>>, XmlSerializer<?>> customSerializers,
            Filter<Class<? extends CoreAnnotation>> annotationFilter,
            Filter<Node> nodeFilter) {
        Preconditions.checkNotNull(stylesheetName, "stylesheetName");
        Preconditions.checkNotNull(simpleNames, "simpleNames");
        Preconditions.checkNotNull(customSerializers, "customSerializers");



        this.namespaceURI = namespaceURI;
        this.stylesheetName = stylesheetName;
        this.simpleNames = simpleNames;
        this.customSerializers = customSerializers;
        this.annotationFilter = annotationFilter;
        this.nodeFilter = nodeFilter;
    }

    /**
     *
     * @return
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Wrapper around xmlPrint(Annotation, OutputStream). Added for backward compatibility.
     * <p/>
     * @param annotation
     * @param writer     The Writer to send the output to
     * @throws IOException
     */
    public void xmlPrint(Annotation annotation, Writer writer) throws IOException {
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
     * @param annotation   Contains the output of all annotators
     * @param outputStream The output stream
     * @throws IOException
     */
    public void xmlPrint(Annotation annotation, OutputStream outputStream) throws IOException {
        Preconditions.checkNotNull(annotation, "annotation");
        Preconditions.checkNotNull(outputStream, "outputStream");

        Document xmlDoc = annotationToDoc(annotation);
        Serializer ser = new Serializer(outputStream, "UTF-8");
        ser.setIndent(2);
        ser.setMaxLength(0);
        ser.write(xmlDoc);
        ser.flush();
    }

    /**
     * Converts the given annotation to an XML document
     * <p/>
     * @param annotation
     * @return
     */
    public Document annotationToDoc(Annotation annotation) {
        Preconditions.checkNotNull(annotation, "annotation");


//        Element root = new Element("root", namespaceURI);

        Document xmlDoc = nodeFactory.startMakingDocument();

        Element root = nodeFactory.makeRootElement("root", namespaceURI);

        xmlDoc.setRootElement(root);



//        Document xmlDoc = new Document(root);

        Nodes pi = nodeFactory.makeProcessingInstruction(
                "xml-stylesheet", "href=\"" + stylesheetName + "\" type=\"text/xsl\"");

//        xmlDoc.

//        xmlDoc.
//        xmlDoc.insertChild(pi, 0);



        Element docElem = nodeFactory.startMakingElement("document", namespaceURI);
        addCoreMap(docElem, annotation);
        appendAllNodes(root, nodeFactory.finishMakingElement(docElem));

        nodeFactory.finishMakingDocument(xmlDoc);
        return xmlDoc;
    }

    /**
     *
     * @param parent
     * @param value
     */
    private void addElementByValueType(Element parent, Object value) {
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

    private void appendAllNodes(@Nonnull Element parent, @Nonnull Nodes nodes) {
        for (int i = 0; i < nodes.size(); i++)
            parent.appendChild(nodes.get(i));
    }

    private void addString(@Nonnull Element parent, @Nonnull CharSequence value) {
        Nodes nodes = nodeFactory.makeText(value.toString());
        appendAllNodes(parent, nodes);
    }

    private void addNumber(@Nonnull Element parent, @Nonnull Number value) {
        addString(parent, value.toString());
    }

    private void addBoolean(@Nonnull Element parent, @Nonnull Boolean value) {
        addString(parent, Boolean.toString(value));
    }

    private static final Morphology morph = new Morphology();

    private void addListElements(@Nonnull Element parentList, @Nonnull List<?> childValues) {

        // The list element itself will be named using the normal shortening system. Member 
        // elements of the list will attempt to use a singular variant of the list name if possible.
        String singularName = morph.stem(parentList.getLocalName());
        if (singularName.equals(parentList.getLocalName())) {
            singularName += "i";
        }

        int count = 0;
        for (Object value : childValues) {
            count++;

            Element itemElement = nodeFactory.startMakingElement(
                    singularName, parentList.getNamespaceURI());
            itemElement.addAttribute(new Attribute("id", Integer.toString(count)));

            if (value != null)
                addElementByValueType(itemElement, value);

            appendAllNodes(parentList, nodeFactory.finishMakingElement(itemElement));
        }
    }

    private void addCoreMap(@Nonnull Element parent,
                            @Nonnull CoreMap map) {
        for (Class<?> key : map.keySet()) {
            if (!CoreAnnotation.class.isAssignableFrom(key)) {
                throw new AssertionError("Key is not an instance of CoreAnnotation.");
            }

            final Class<? extends CoreAnnotation> castKey =
                    (Class<? extends CoreAnnotation>) key;

            if (!annotationFilter.accept(castKey))
                continue;


            final String name = (simpleNames.containsKey(castKey))
                    ? simpleNames.get(castKey)
                    : castKey.getCanonicalName();

            final Element element = nodeFactory.startMakingElement(name, parent.getNamespaceURI());


            boolean found = false;
            for (Class<? extends CoreAnnotation> x : customSerializers.keySet()) {

                if (x.isAssignableFrom(key)) {
                    XmlSerializer s = customSerializers.get(x);
                    final Object value = map.get(castKey);
                    s.add(nodeFactory, element, value);
                    found = true;
                    break;
                }
            }
            if (!found) {

                final Object value = map.get(castKey);
                if (value != null)
                    addElementByValueType(element, value);
            }

            appendAllNodes(parent, nodeFactory.finishMakingElement(element));
        }
    }

    private void addMap(Element parent, Map<String, ?> map) {
        for (String key : map.keySet()) {

            final Element element = nodeFactory.startMakingElement(
                    key, parent.getNamespaceURI());

            final Object value = map.get(key);
            if (value != null)
                addElementByValueType(element, value);

            appendAllNodes(parent, nodeFactory.finishMakingElement(element));
        }
    }

    public interface XmlSerializer<T> {

        void add(NodeFactory factory, @Nonnull Element element, @Nonnull T value);
    }

    public static class TimexXmlSerializer implements XmlSerializer<Timex> {

        private boolean useTimexXml = false;

        public void add(NodeFactory factory, @Nonnull Element element, @Nonnull Timex value) {
            if (useTimexXml) {
                Element timexXml = value.toXmlElement();
                for (int i = 0; i < timexXml.getAttributeCount(); i++) {
                    Attribute a = timexXml.getAttribute(i);
                    timexXml.removeAttribute(a);
                    element.addAttribute(a);
                }
            } else {

//                factory.makeAttribute("tid", element.getNamespaceURI(),
//                                      value.tid(), Attribute.Type.CDATA);
//                

                element.addAttribute(new Attribute("tid", value.tid()));
                element.addAttribute(new Attribute("type", value.timexType()));
                element.appendChild(value.value());
            }
        }
    }

    public static class TreeXmlSerializer implements XmlSerializer<Tree> {

        private static TreePrint constituentTreePrinter = new TreePrint("penn");

        public void add(NodeFactory factory, @Nonnull Element parent, @Nonnull Tree tree) {
            StringWriter treeStrWriter = new StringWriter();
            constituentTreePrinter.printTree(tree, new PrintWriter(treeStrWriter, true));
            String temp = treeStrWriter.toString();
            parent.appendChild(temp);
        }
    }

    public static class SemanticXmlGraphSerializer implements XmlSerializer<SemanticGraph> {

        public void add(NodeFactory factory, @Nonnull Element parent, @Nonnull SemanticGraph graph) {


            for (SemanticGraphEdge edge : graph.edgeListSorted()) {
                String rel = edge.getRelation().toString();
                rel = rel.replaceAll("\\s+", "");
                int source = edge.getSource().index();
                int target = edge.getTarget().index();


                String sourceString = edge.getSource().word();
                String targetString = edge.getTarget().word();

                Element depElem = new Element("dep", parent.getNamespaceURI());
                depElem.addAttribute(new Attribute("type", rel));

                Element govElem = new Element("governor", parent.getNamespaceURI());
                govElem.addAttribute(new Attribute("idx", Integer.toString(source)));
//            govElem.appendChild(tokens.get(source - 1).word());
                govElem.appendChild(sourceString);
                depElem.appendChild(govElem);

                Element dependElem = new Element("dependent", parent.getNamespaceURI());
                dependElem.addAttribute(new Attribute("idx", Integer.toString(target)));
//            dependElem.appendChild(tokens.get(target - 1).word());
                dependElem.appendChild(targetString);
                
                
                depElem.appendChild(dependElem);

                parent.appendChild(depElem);
            }

        }
    }

    public static class CorefXmlSerializer implements XmlSerializer<Map<Integer, CorefChain>> {

        /**
         * Generates the XML content for the coreference chain object
         */
        public void add(NodeFactory factory, @Nonnull Element corefInfo,
                        @Nonnull Map<Integer, CorefChain> corefChains) {
            String curNS = corefInfo.getNamespaceURI();
            boolean foundCoref = false;
            for (CorefChain chain : corefChains.values()) {

                if (chain.getCorefMentions().size() <= 1)
                    continue;
                foundCoref = true;
                Element chainElem = new Element("coreference", curNS);
                CorefChain.CorefMention source = chain.getRepresentativeMention();
                addCorefMention(factory, chainElem, curNS, source, true);
                for (CorefChain.CorefMention mention : chain.getCorefMentions()) {
                    if (mention == source)
                        continue;
                    addCorefMention(factory, chainElem, curNS, mention, false);
                }
                corefInfo.appendChild(chainElem);
            }
//            return foundCoref;
        }

        private void addCorefMention(NodeFactory factory, @Nonnull Element chainElem,
                                     @Nonnull String curNS,
                                     @Nonnull CorefChain.CorefMention mention,
                                     boolean representative) {
            Element mentionElem = new Element("mention", curNS);
            if (representative) {
                mentionElem.addAttribute(new Attribute("representative", "true"));
            }

            Element sentenceElem = new Element("sentence", curNS);
            sentenceElem.appendChild(Integer.toString(mention.sentNum));
            mentionElem.appendChild(sentenceElem);

            Element startElem = new Element("start", curNS);
            sentenceElem.appendChild(Integer.toString(mention.startIndex));
            mentionElem.appendChild(startElem);

            Element endElem = new Element("end", curNS);
            sentenceElem.appendChild(Integer.toString(mention.endIndex));
            mentionElem.appendChild(endElem);

            Element headElem = new Element("head", curNS);
            sentenceElem.appendChild(Integer.toString(mention.headIndex));
            mentionElem.appendChild(headElem);

            chainElem.appendChild(mentionElem);
        }
    }

    /**
     *
     */
    public static class Builder {

        private static final Class<?>[] DEFAULT_ANNOTATION_ROOTS = {
            CoreAnnotations.class,
            SemanticGraphCoreAnnotations.class,
            MachineReadingAnnotations.class,
            ChineseGrammaticalRelations.class,
            CoNLL2011DocumentReader.class,
            CorefCoreAnnotations.class,
            EnglishGrammaticalRelations.class,
            GrammaticalRelation.class,
            //                    ParserAnnotations.class,
            Tags.class,
            TimeAnnotations.class,
            TimeExpression.class,
            TreeCoreAnnotations.class,
            Mention.class
        };

        private static final String[] DEFAULT_STRIP_SUFFIXES = {
            "Annotations",
            "Annotation",
            "GrammaticalRelations"
        };

        private final Set<Class<?>> annotationRoots = new HashSet<Class<?>>();

        private final Set<String> stripSuffixes = new HashSet<String>();

        private final BiMap<Class<? extends CoreAnnotation>, String> simpleNames =
                HashBiMap.<Class<? extends CoreAnnotation>, String>create();

        private final Map<Class<? extends CoreAnnotation<?>>, XmlSerializer<?>> customSerializers =
                new HashMap<Class<? extends CoreAnnotation<?>>, XmlSerializer<?>>();

        private boolean useDefaultStripSuffixes = true;

        private boolean useDefaultAnnotationRoots = true;

        private boolean useDefaultSerializers = true;

        private String namespaceURI = null;

        private String stylesheetName = "CoreNLP-to-HTML.xsl";

        public Builder() {
        }

        public AnnotationToXMLSerializer build() throws XPathExpressionException {

            if (useDefaultSerializers) {
                addCustomeSerializer(
                        TimeAnnotations.TimexAnnotation.class,
                        new TimexXmlSerializer());
                addCustomeSerializer(
                        TreeCoreAnnotations.TreeAnnotation.class,
                        new TreeXmlSerializer());
                SemanticXmlGraphSerializer sxgs = new SemanticXmlGraphSerializer();
                addCustomeSerializer(
                        SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class,
                        sxgs);
                addCustomeSerializer(
                        SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class,
                        sxgs);
                addCustomeSerializer(
                        SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation.class,
                        sxgs);
                addCustomeSerializer(
                        CorefCoreAnnotations.CorefChainAnnotation.class,
                        new CorefXmlSerializer());
            }

            if (useDefaultStripSuffixes) {
                stripSuffixes.addAll(Arrays.asList(DEFAULT_STRIP_SUFFIXES));
            }


            if (useDefaultAnnotationRoots) {
                annotationRoots.addAll(Arrays.asList(DEFAULT_ANNOTATION_ROOTS));
            }

            simpleNames.put(CoreAnnotations.TextAnnotation.class, "word");
            simpleNames.put(CoreAnnotations.PartOfSpeechAnnotation.class, "POS");
            simpleNames.put(CoreAnnotations.NamedEntityTagAnnotation.class, "NER");
            simpleNames.put(CoreAnnotations.CharacterOffsetBeginAnnotation.class,
                            "CharacterOffsetBegin");
            simpleNames
                    .put(CoreAnnotations.CharacterOffsetEndAnnotation.class, "CharacterOffsetEnd");
            simpleNames.put(TreeCoreAnnotations.TreeAnnotation.class, "parse");
            simpleNames.put(CorefCoreAnnotations.CorefChainAnnotation.class, "coreference");
            simpleNames.put(
                    SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class,
                    "collapsed-ccprocessed-dependencies");
            simpleNames.put(SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation.class,
                            "collapsed-dependencies");
            simpleNames.put(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class,
                            "basic-dependencies");




            if (!annotationRoots.isEmpty()) {
                final Set<Class<? extends CoreAnnotation>> candidates =
                        findMembersOfType(annotationRoots,
                                          CoreAnnotation.class);
                for (Class<? extends CoreAnnotation> x : candidates) {
                    if (!simpleNames.containsKey(x)) {
                        String name = simplifiedName(
                                x, DEFAULT_STRIP_SUFFIXES, simpleNames.values());
                        simpleNames.put(x, name);
                    }
                }

            }

            Set<Class<? extends CoreAnnotation>> annotationBlacklist =
                    new HashSet<Class<? extends CoreAnnotation>>();
            annotationBlacklist.add(CoreAnnotations.ValueAnnotation.class);
            annotationBlacklist.add(CoreAnnotations.OriginalTextAnnotation.class);
            annotationBlacklist.add(CoreAnnotations.BeforeAnnotation.class);
            annotationBlacklist.add(CoreAnnotations.AfterAnnotation.class);
            annotationBlacklist.add(CoreAnnotations.TokenBeginAnnotation.class);
            annotationBlacklist.add(CoreAnnotations.TokenEndAnnotation.class);
            annotationBlacklist.add(CoreAnnotations.IndexAnnotation.class);
            annotationBlacklist.add(CoreAnnotations.UtteranceAnnotation.class);
            annotationBlacklist.add(CoreAnnotations.BeginIndexAnnotation.class);
            annotationBlacklist.add(CoreAnnotations.EndIndexAnnotation.class);
            annotationBlacklist.add(CoreAnnotations.ParagraphAnnotation.class);
            annotationBlacklist.add(CoreAnnotations.SpeakerAnnotation.class);
            annotationBlacklist.add(CorefCoreAnnotations.CorefClusterIdAnnotation.class);
            annotationBlacklist.add(CoreAnnotations.NumerizedTokensAnnotation.class);

            Filter<Class<? extends CoreAnnotation>> annotationBlacklistFilter =
                    Filters.collectionRejectFilter(annotationBlacklist);



//            final XPathFactory xpathFactory = XPathFactory.newInstance();
//            final XPath xpath = xpathFactory.newXPath();
//
//            Filter<Node> nodeFilter = new Filter<Node>() {
//                XPathExpression xpx = xpath.compile("/root/document/word");
//
//                public boolean accept(Node obj) {
//                    try {
//                        Object result = xpx.evaluate(obj, XPathConstants.BOOLEAN);
//                        assert result instanceof Boolean;
//                        return (Boolean) result;
//                    } catch (XPathExpressionException ex) {
//                        throw new AssertionError(ex);
//                    }
//                }
//            };
//            xpx.


//        xp.
//        

            Filter<Node> nodeFilter = Filters.acceptFilter();

            return new AnnotationToXMLSerializer(
                    namespaceURI,
                    stylesheetName,
                    simpleNames,
                    customSerializers,
                    annotationBlacklistFilter,
                    nodeFilter);
        }

        public Builder disableDefaultSerializers() {
            useDefaultSerializers = false;
            return this;
        }

        public Builder disableDefaultAnnotationRoots() {
            useDefaultAnnotationRoots = false;
            return this;
        }

        public Builder disableDefaultStripSuffixes() {
            useDefaultStripSuffixes = false;
            return this;
        }

        public <T> Builder addCustomeSerializer(
                Class<? extends CoreAnnotation<T>> annotationType,
                XmlSerializer<T> serializer) {
            customSerializers.put(annotationType, serializer);
            return this;
        }

        public Builder addSimplifiedName(
                Class<? extends CoreAnnotation> annotationType,
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

        /**
         * Search (breadth first) the membership tree(s) of any classes that are subclasses of the
         * given {@code targetType}. The search is started at all of the given root nodes.
         * <p/>
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

            final Set<Class<? extends T>> found = new HashSet<Class<? extends T>>();

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

                if (targetType.isAssignableFrom(clazz))
                    found.add((Class<? extends T>) clazz);

                done.add(clazz);

                Set<Class<?>> members = Sets.newHashSet(Arrays.asList(clazz.getClasses()));
                members = Sets.difference(members, done);
                todo.addAll(members);
            }

            return found;
        }

        private static String simplifiedName(Class<?> clazz,
                                             String[] stripSuffixes,
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
                className = MEMBER_DELIMITER.matcher(clazz.getName()).replaceAll(".");
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
                        part = part.substring(0, part.length() - stripSuffix.length());
                    }
                }
                // Change first character to lower case
                if (Character.isUpperCase(part.charAt(0))) {
                    part = Character.toLowerCase(part.charAt(0))
                            + (part.length() > 1 ? part.substring(1) : "");
                }

                if (name.length() > 0)
                    name.insert(0, '.');
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
                    name.replace(mark, name.length(), Integer.toString(++number));
                }
            }

            return name.toString();
        }
    }
}
