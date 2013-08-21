/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.snlp;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ie.machinereading.structure.EntityMention;
import edu.stanford.nlp.ie.machinereading.structure.MachineReadingAnnotations;
import edu.stanford.nlp.ie.machinereading.structure.RelationMention;
import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.Timex;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation;
import edu.stanford.nlp.trees.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import nu.xom.*;
import javax.annotation.Nullable;
import uk.ac.susx.mlcl.erl.linker.EntityLinkingAnnotator;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Save annotations to XML.
 * <p/>
 * <p/>
 * Mostly Ripped form {@link StanfordCoreNLP}, but modified to support our own annotations.
 * <p/>
 *
 * @author hamish
 */
public class StanfordAnnotationToXML {

    @Nullable
    private static final String NAMESPACE_URI = null;

    private static final String STYLESHEET_NAME = "CoreNLP-to-HTML.xsl";

    private final GrammaticalStructureFactory gsf;

    private TreePrint constituentTreePrinter;

    // property: printable.relation.beam
    @Nullable
    private final String printableSelationBeam = null;

    public StanfordAnnotationToXML() {
        this.gsf = new PennTreebankLanguagePack().grammaticalStructureFactory();
        this.constituentTreePrinter = new TreePrint("penn");
    }

    /**
     * Wrapper around xmlPrint(Annotation, OutputStream). Added for backward compatibility.
     * <p/>
     *
     * @param annotation
     * @param w          The Writer to send the output to
     * @throws IOException
     */
    public void xmlPrint(@Nonnull Annotation annotation, @Nonnull Writer w) throws IOException, InstantiationException, IllegalAccessException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        xmlPrint(annotation, os); // this builds it as UTF-8, always
        w.write(new String(os.toByteArray(), "UTF-8")); // This will convert it to something else
        w.flush();
    }

    /**
     * Displays the output of all annotators in XML format.
     * <p/>
     *
     * @param annotation Contains the output of all annotators
     * @param os         The output stream
     * @throws IOException
     */
    public void xmlPrint(@Nonnull Annotation annotation, OutputStream os) throws IOException {
        Document xmlDoc = annotationToDoc(annotation);
        Serializer ser = new Serializer(os, "UTF-8");
        ser.setIndent(2);
        ser.setMaxLength(0);
        ser.write(xmlDoc);
        ser.flush();
    }

    /**
     * Converts the given annotation to an XML document
     */
    @Nullable
    public Document annotationToDoc(@Nonnull Annotation annotation) {
        //
        // create the XML document with the root node pointing to the namespace URL
        //
        Element root = new Element("root", NAMESPACE_URI);
        Document xmlDoc = new Document(root);
        ProcessingInstruction pi = new ProcessingInstruction("xml-stylesheet",
                "href=\"" + STYLESHEET_NAME
                        + "\" type=\"text/xsl\"");
        xmlDoc.insertChild(pi, 0);
        Element docElem = new Element("document", NAMESPACE_URI);
        root.appendChild(docElem);

        String docId = annotation.get(DocIDAnnotation.class);
        if (docId != null) {
            setSingleElement(docElem, "docId", NAMESPACE_URI, docId);
        }

        String docDate = annotation.get(DocDateAnnotation.class);
        if (docDate != null) {
            setSingleElement(docElem, "docDate", NAMESPACE_URI, docDate);
        }

        Element sentencesElem = new Element("sentences", NAMESPACE_URI);
        docElem.appendChild(sentencesElem);

        //
        // save the info for each sentence in this doc
        //
        if (annotation.get(SentencesAnnotation.class) != null) {
            int sentCount = 1;
            for (CoreMap sentence : annotation.get(SentencesAnnotation.class)) {
                Element sentElem = new Element("sentence", NAMESPACE_URI);
                sentElem.addAttribute(new Attribute("id", Integer.toString(sentCount)));
                sentCount++;

                // add the word table with all token-level annotations
                Element wordTable = new Element("tokens", NAMESPACE_URI);
                List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
                for (int j = 0; j < tokens.size(); j++) {
                    Element wordInfo = new Element("token", NAMESPACE_URI);
                    addWordInfo(wordInfo, tokens.get(j), j + 1, NAMESPACE_URI);
                    wordTable.appendChild(wordInfo);
                }
                sentElem.appendChild(wordTable);

                // add tree info
                Tree tree = sentence.get(TreeAnnotation.class);

                if (tree != null) {
                    // add the constituent tree for this sentence
                    Element parseInfo = new Element("parse", NAMESPACE_URI);
                    addConstituentTreeInfo(parseInfo, tree);
                    sentElem.appendChild(parseInfo);

                    // add the dependencies for this sentence
                    Element depInfo = new Element("basic-dependencies", NAMESPACE_URI);
                    addDependencyTreeInfo(depInfo, sentence
                            .get(BasicDependenciesAnnotation.class),
                            tokens, NAMESPACE_URI);
                    sentElem.appendChild(depInfo);

                    depInfo = new Element("collapsed-dependencies", NAMESPACE_URI);
                    addDependencyTreeInfo(depInfo, sentence
                            .get(CollapsedDependenciesAnnotation.class),
                            tokens, NAMESPACE_URI);
                    sentElem.appendChild(depInfo);

                    depInfo = new Element("collapsed-ccprocessed-dependencies", NAMESPACE_URI);
                    addDependencyTreeInfo(depInfo, sentence
                            .get(CollapsedCCProcessedDependenciesAnnotation.class),
                            tokens, NAMESPACE_URI);
                    sentElem.appendChild(depInfo);
                }

                // add the MR entities and relations
                List<EntityMention> entities = sentence
                        .get(MachineReadingAnnotations.EntityMentionsAnnotation.class);
                List<RelationMention> relations = sentence
                        .get(MachineReadingAnnotations.RelationMentionsAnnotation.class);
                if (entities != null && entities.size() > 0) {
                    Element mrElem = new Element("MachineReading", NAMESPACE_URI);
                    Element entElem = new Element("entities", NAMESPACE_URI);
                    addEntities(entities, entElem, NAMESPACE_URI);
                    mrElem.appendChild(entElem);

                    if (relations != null) {
                        Element relElem = new Element("relations", NAMESPACE_URI);
                        addRelations(relations, relElem, NAMESPACE_URI, printableSelationBeam);
                        mrElem.appendChild(relElem);
                    }

                    sentElem.appendChild(mrElem);
                }

                // add the sentence to the root
                sentencesElem.appendChild(sentElem);
            }
        }

        //
        // add the coref graph
        //
        Map<Integer, CorefChain> corefChains =
                annotation.get(CorefChainAnnotation.class);
        if (corefChains != null) {
            Element corefInfo = new Element("coreference", NAMESPACE_URI);
            if (addCorefGraphInfo(corefInfo, corefChains, NAMESPACE_URI))
                docElem.appendChild(corefInfo);
        }

        //
        // save any document-level annotations here
        //

        return xmlDoc;
    }

    /**
     * Generates the XML content for a constituent tree
     */
    private void addConstituentTreeInfo(@Nonnull Element treeInfo, Tree tree) {
        StringWriter treeStrWriter = new StringWriter();
        constituentTreePrinter.printTree(tree, new PrintWriter(treeStrWriter, true));
        String temp = treeStrWriter.toString();
        //System.err.println(temp);
        treeInfo.appendChild(temp);
    }

    private static void addDependencyTreeInfo(@Nonnull Element depInfo, @Nullable SemanticGraph graph,
                                              @Nonnull List<CoreLabel> tokens, String curNS) {
        if (graph != null) {
            for (SemanticGraphEdge edge : graph.edgeListSorted()) {
                String rel = edge.getRelation().toString();
                rel = rel.replaceAll("\\s+", "");
                int source = edge.getSource().index();
                int target = edge.getTarget().index();

                Element depElem = new Element("dep", curNS);
                depElem.addAttribute(new Attribute("type", rel));

                Element govElem = new Element("governor", curNS);
                govElem.addAttribute(new Attribute("idx", Integer.toString(source)));
                govElem.appendChild(tokens.get(source - 1).word());
                depElem.appendChild(govElem);

                Element dependElem = new Element("dependent", curNS);
                dependElem.addAttribute(new Attribute("idx", Integer.toString(target)));
                dependElem.appendChild(tokens.get(target - 1).word());
                depElem.appendChild(dependElem);

                depInfo.appendChild(depElem);
            }
        }
    }

    /**
     * Generates the XML content for a dependency tree
     */
    @SuppressWarnings("unused")
    private void addDependencyTreeInfo(@Nonnull Element depInfo, @Nullable Tree tree, String curNS) {
        if (tree != null) {
            GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
            Collection<TypedDependency> deps = gs.typedDependencies();
            for (TypedDependency dep : deps) {
                Element depElem = new Element("dep", curNS);
                depElem.addAttribute(new Attribute("type", dep.reln().getShortName()));

                Element govElem = new Element("governor", curNS);
                govElem.addAttribute(new Attribute("idx", Integer.toString(dep.gov().index())));
                govElem.appendChild(dep.gov().value());
                depElem.appendChild(govElem);

                Element dependElem = new Element("dependent", curNS);
                dependElem.addAttribute(new Attribute("idx", Integer.toString(dep.dep().index())));
                dependElem.appendChild(dep.dep().value());
                depElem.appendChild(dependElem);

                depInfo.appendChild(depElem);
            }
        }
    }

    /**
     * Generates the XML content for MachineReading entities
     */
    private static void addEntities(@Nonnull List<EntityMention> entities, @Nonnull Element top, String curNS) {
        for (EntityMention e : entities) {
            Element ee = e.toXML(curNS);
            top.appendChild(ee);
        }
    }

    /**
     * Generates the XML content for MachineReading relations
     */
    private static void addRelations(@Nonnull List<RelationMention> relations, @Nonnull Element top, String curNS,
                                     @Nullable String beamAsString) {
        double beam = 0;
        if (beamAsString != null)
            beam = Double.parseDouble(beamAsString);
        for (RelationMention r : relations) {
            if (r.printableObject(beam)) {
                Element re = r.toXML(curNS);
                top.appendChild(re);
            }
        }
    }

    /**
     * Generates the XML content for the coreference chain object
     */
    private static boolean addCorefGraphInfo(@Nonnull Element corefInfo, @Nonnull Map<Integer, CorefChain> corefChains,
                                             String curNS) {
        boolean foundCoref = false;
        for (CorefChain chain : corefChains.values()) {
            if (chain.getCorefMentions().size() <= 1)
                continue;
            foundCoref = true;
            Element chainElem = new Element("coreference", curNS);
            CorefChain.CorefMention source = chain.getRepresentativeMention();
            addCorefMention(chainElem, curNS, source, true);
            for (CorefChain.CorefMention mention : chain.getCorefMentions()) {
                if (mention == source)
                    continue;
                addCorefMention(chainElem, curNS, mention, false);
            }
            corefInfo.appendChild(chainElem);
        }
        return foundCoref;
    }

    private static void addCorefMention(@Nonnull Element chainElem, String curNS,
                                        @Nonnull CorefChain.CorefMention mention,
                                        boolean representative) {
        Element mentionElem = new Element("mention", curNS);
        if (representative) {
            mentionElem.addAttribute(new Attribute("representative", "true"));
        }

        setSingleElement(mentionElem, "sentence", curNS,
                Integer.toString(mention.sentNum));
        setSingleElement(mentionElem, "start", curNS,
                Integer.toString(mention.startIndex));
        setSingleElement(mentionElem, "end", curNS,
                Integer.toString(mention.endIndex));
        setSingleElement(mentionElem, "head", curNS,
                Integer.toString(mention.headIndex));

        chainElem.appendChild(mentionElem);
    }

    private static void addWordInfo(@Nonnull Element wordInfo, @Nonnull CoreMap token, int id, String curNS) {
        // store the position of this word in the sentence
        wordInfo.addAttribute(new Attribute("id", Integer.toString(id)));

        setSingleElement(wordInfo, "word", curNS, token.get(TextAnnotation.class));
        setSingleElement(wordInfo, "lemma", curNS, token.get(LemmaAnnotation.class));

        if (token.containsKey(CharacterOffsetBeginAnnotation.class) && token
                .containsKey(CharacterOffsetEndAnnotation.class)) {
            setSingleElement(wordInfo, "CharacterOffsetBegin", curNS, Integer.toString(token
                    .get(CharacterOffsetBeginAnnotation.class)));
            setSingleElement(wordInfo, "CharacterOffsetEnd", curNS, Integer.toString(token
                    .get(CharacterOffsetEndAnnotation.class)));
        }

        if (token.containsKey(PartOfSpeechAnnotation.class)) {
            setSingleElement(wordInfo, "POS", curNS, token
                    .get(PartOfSpeechAnnotation.class));
        }

        if (token.containsKey(NamedEntityTagAnnotation.class)) {
            setSingleElement(wordInfo, "NER", curNS, token
                    .get(NamedEntityTagAnnotation.class));
        }

        if (token.containsKey(EntityLinkingAnnotator.EntityKbIdAnnotation.class)) {
            setSingleElement(wordInfo, "ID", curNS, token
                    .get(EntityLinkingAnnotator.EntityKbIdAnnotation.class));
        }

        if (token.containsKey(NormalizedNamedEntityTagAnnotation.class)) {
            setSingleElement(wordInfo, "NormalizedNER", curNS, token
                    .get(NormalizedNamedEntityTagAnnotation.class));
        }

        if (token.containsKey(TimeAnnotations.TimexAnnotation.class)) {
            Timex timex = token.get(TimeAnnotations.TimexAnnotation.class);
            Element timexElem = new Element("Timex", curNS);
            timexElem.addAttribute(new Attribute("tid", timex.tid()));
            timexElem.addAttribute(new Attribute("type", timex.timexType()));
            timexElem.appendChild(timex.value());
            wordInfo.appendChild(timexElem);
        }

        if (token.containsKey(TrueCaseAnnotation.class)) {
            Element cur = new Element("TrueCase", curNS);
            cur.appendChild(token.get(TrueCaseAnnotation.class));
            wordInfo.appendChild(cur);
        }
        if (token.containsKey(TrueCaseTextAnnotation.class)) {
            Element cur = new Element("TrueCaseText", curNS);
            cur.appendChild(token.get(TrueCaseTextAnnotation.class));
            wordInfo.appendChild(cur);
        }

//    IntTuple corefDest;
//    if((corefDest = label.get(CorefDestAnnotation.class)) != null){
//      Element cur = new Element("coref", curNS);
//      String value = Integer.toString(corefDest.get(0)) + "." + Integer.toString(corefDest.get(1));
//      cur.setText(value);
//      wordInfo.addContent(cur);
//    }
    }

    /**
     * Helper method for addWordInfo(). If the value is not null, creates an element of the given
     * name and namespace and adds it to the tokenElement.
     * <p/>
     *
     * @param tokenElement This is the element to which the newly created element will be added
     * @param elemName     This is the name for the new XML element
     * @param curNS        The current namespace
     * @param value        This is its value
     */
    private static void setSingleElement(@Nonnull Element tokenElement, String elemName, String curNS,
                                         @Nullable String value) {
        Element cur = new Element(elemName, curNS);
        if (value != null) {
            cur.appendChild(value);
            tokenElement.appendChild(cur);
        }
    }
}
