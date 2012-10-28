/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package edu.stanford.nlp;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import uk.ac.susx.mlcl.erl.test.AbstractTest;
import uk.ac.susx.mlcl.erl.test.Categories;

/**
 * 
 * 
 * 
 * @author hamish
 */
@Ignore

public class StanfordNLPTest extends AbstractTest {

    public StanfordNLPTest() {
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
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //

    @Test
    @Category(Categories.SlowTests.class)
    public void snlpUsageExample() {
        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
        Properties props = new Properties();
        props.put("annotators",
                  "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);


        // read some TEXT2 in the TEXT2 variable
        String text = TEXT2; // Add your TEXT2 here!

        // create an empty Annotation just with the given TEXT2
        Annotation document = new Annotation(text);

        // run all Annotators on this TEXT2
        pipeline.annotate(document);

        System.out.println(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                // this is the TEXT2 of the token
                String word = token.get(TextAnnotation.class);
                // this is the POS tag of the token
                String pos = token.get(PartOfSpeechAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(NamedEntityTagAnnotation.class);

                System.out.printf("%s\t%s\t%s\t%s%n",
                                  token.originalText(), word, pos, ne);
            }

            // this is the parse tree of the current sentence
            Tree tree = sentence.get(TreeAnnotation.class);

            // this is the Stanford dependency graph of the current sentence
            SemanticGraph dependencies = sentence
                    .get(CollapsedCCProcessedDependenciesAnnotation.class);
        }

        // This is the coreference link graph
        // Each chain stores a set of mentions that link to each other,
        // along with a method for getting the most representative mention
        // Both sentence and token offsets start at 1!
        Map<Integer, CorefChain> graph =
                document.get(CorefChainAnnotation.class);
        System.out.println(graph);
    }

    @Test
    public void testTokeniSe() {
        Properties props = new Properties();
        props.put("annotators", "tokenize");

        // tokenize.whitespace
        // if set to true, separates words only when whitespace is encountered.
        props.put("tokenize.whitespace", "false");

        // tokenize.options:
        // Accepts the options of PTBTokenizer for example, things like 
        //  "americanize=false" 
        //  "strictTreebank3=true,
        //   untokenizable=allKeep".
        props.put("tokenize.options", "untokenizable=allKeep");

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        String text = TEXT1;

        Annotation document = new Annotation(text);

        pipeline.annotate(document);

        System.out.println(document);

        document.get(TokensAnnotation.class);

        List<CoreLabel> tokens = document.get(TokensAnnotation.class);
        for (CoreLabel token : tokens) {
            System.out.printf("[%d,%d] %s%n",
                              token.beginPosition(), token.endPosition(),
                              token.get(TextAnnotation.class));
        }
    }

    @Test
    public void testSentenceSplit() {

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit");


        // ssplit.eolonly: 
        // only split sentences on newlines. Works well in conjunction with "-tokenize.whitespace true", in which case StanfordCoreNLP will split one sentence per line, only separating words on whitespace.
        props.put("ssplit.eolonly", "false");

        // ssplit.isOneSentence: 
        // each document is to be treated as one sentence, no splitting at all.        
        props.put("ssplit.isOneSentence", "false");

        // props.put("ssplit.boundariesToDiscard", "");

        //  props.put("ssplit.htmlBoundariesToDiscard", "");

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        String text = TEXT1;

        // create an empty Annotation just with the given TEXT2
        Annotation document = new Annotation(text);

        // run all Annotators on this TEXT2
        pipeline.annotate(document);

        System.out.println(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            System.out.println("BEGIN SENTENCE");

            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {

                String word = token.get(TextAnnotation.class);

                System.out.printf("[%d,%d] %s%n",
                                  token.beginPosition(), token.endPosition(),
                                  token.get(TextAnnotation.class));
            }
            System.out.println("END SENTENCE");
        }
    }

    @Test
    public void testPOSTagger() {

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos");

        /*
         * pos.model: POS model to use. There is no need to explicitly set this option, unless you
         * want to use a different POS model (for advanced developers only). By default, this is set
         * to the POS model included in the stanford-corenlp-models JAR file.
         *
         * edu/stanford/nlp/models/pos-tagger/...
         * .../english-bidirectional/english-bidirectional-distsim.tagger
         * .../english-left3words/english-left3words-distsim.tagger
         * .../wsj-bidirectional/wsj-0-18-bidirectional-distsim.tagger
         * .../wsj-left3words/wsj-0-18-left3words-distsim.tagger
         */
        String p = "edu/stanford/nlp/models/pos-tagger/";
        String s = ".tagger";
        props.put("pos.model", p + "english-bidirectional/english-bidirectional-distsim" + s);
//        props.put("pos.model", p + "english-left3words/english-left3words-distsim" +  s);
//        props.put("pos.model", p + "wsj-bidirectional/wsj-0-18-bidirectional-distsim" +  s);
//        props.put("pos.model", p + "wsj-left3words/wsj-0-18-left3words-distsim" +  s);

        /*
         * pos.maxlen: Maximum sentence size for the POS sequence tagger. Any sentence larger than
         * this is split in smaller sentences before tagging. Useful to control the speed of the
         * tagger on noisy text without punctuation marks.
         */
        props.put("pos.maxlen", Integer.toString(Integer.MAX_VALUE));


        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        String text = TEXT1;

        Annotation document = new Annotation(text);

        pipeline.annotate(document);

        System.out.println(document);

        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {

            System.out.println("BEGIN SENTENCE");

            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {

                String word = token.get(TextAnnotation.class);
                String pos = token.get(PartOfSpeechAnnotation.class);

                System.out.printf("[%d,%d] %s\t%s%n",
                                  token.beginPosition(), token.endPosition(),
                                  token.get(TextAnnotation.class), pos);
            }
            System.out.println("END SENTENCE");
        }
    }

    @Test
    public void testLematisation() {

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");

        // There are no options for the lematiser 

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        String text = TEXT1;

        Annotation document = new Annotation(text);

        pipeline.annotate(document);

        System.out.println(document);

        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {

            System.out.println("BEGIN SENTENCE");

            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {

                String word = token.get(TextAnnotation.class);
                String pos = token.get(PartOfSpeechAnnotation.class);

                String lemma = token.get(LemmaAnnotation.class);

                System.out.printf("[%d,%d] %s\t%s\t%s%n",
                                  token.beginPosition(), token.endPosition(),
                                  token.get(TextAnnotation.class), pos, lemma);
            }
            System.out.println("END SENTENCE");
        }
    }

    @Test
    public void testNER() {

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner");

        /*
         * ner.model: NER model(s) in a comma separated list to use in addition to the ones
         * specified below. These models will take precedence over those models. By default, the
         * models in 3class, 7class, and MISCclass are used in that order. Setting one of the those
         * parameters to blank will turn off the default model.
         *
         * ner.model.3class: NER model for the basic three NE classes (PERSON, ORGANIZATION,
         * LOCATION). There is no need to explicitly set this option, unless you want to use a
         * different NER model (for advanced developers only). By default, this is set to the
         * all.3class NER model included in the stanford-corenlp-models JAR file.
         *
         * ner.model.7class: NER model that includes the above three classes and four additional
         * numerical entities (from the MUC corpus). There is no need to explicitly set this option,
         * unless you want to use a different NER model (for advanced developers only). By default,
         * this is set to the muc.7class NER model included in the stanford-corenlp-models JAR file.
         *
         * ner.model.MISCclass: NER model that recognizes MISCellaneous named entities (trained on
         * the CoNLL corpus). There is no need to explicitly set this option, unless you want to use
         * a different NER model (for advanced developers only). By default, this is set to the
         * conll.4class NER model included in the stanford-corenlp-models JAR file.
         */
//        props.put("ner.model", "xxx");

        /*
         * ner.applyNumericClassifiers: Whether or not to use numeric classifiers, including SUTime.
         * These are hardcoded for English, so if using a different language, this should be set to
         * false.
         */
        props.put("ner.applyNumericClassifiers", "true");

        /*
         * ner.useSUTime
         *
         * No idea what this does
         */
//        props.put("ner.useSUTime", "true");


        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);


        String text = TEXT1;

        Annotation document = new Annotation(text);

        pipeline.annotate(document);

        System.out.println(document);

        List<CoreMap> sentences = document.get(SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                String word = token.get(TextAnnotation.class);
                String pos = token.get(PartOfSpeechAnnotation.class);
                String lemma = token.get(LemmaAnnotation.class);
                String ne = token.get(NamedEntityTagAnnotation.class);


                System.out.printf("[%d,%d] %s\t%s\t%s\t%s%n",
                                  token.beginPosition(), token.endPosition(),
                                  token.get(TextAnnotation.class), pos, lemma, ne);
            }

        }

    }

    static String TEXT1 =
            "A tree is a perennial woody plant. It most often has many secondary branches supported clear of the ground on a single main stem or trunk with clear apical dominance. A minimum height specification at maturity is cited by some authors, varying from 3?m to 6?m; some authors set a minimum of 10?cm trunk diameter (30?cm girth). Woody plants that do not meet these definitions by having multiple stems and/or small size are usually called shrubs, although many trees such as Mallee do not meet such definitions. Compared with most other plants, trees are long-lived, some reaching several thousand years old and growing to up to 115?m (379?ft) high.\n";

    static String TEXT2 =
            "A tree is a perennial woody plant. It most often has many secondary branches supported clear of the ground on a single main stem or trunk with clear apical dominance. A minimum height specification at maturity is cited by some authors, varying from 3?m to 6?m; some authors set a minimum of 10?cm trunk diameter (30?cm girth). Woody plants that do not meet these definitions by having multiple stems and/or small size are usually called shrubs, although many trees such as Mallee do not meet such definitions. Compared with most other plants, trees are long-lived, some reaching several thousand years old and growing to up to 115?m (379?ft) high.\n"
            + "Trees are an important component of the natural landscape because of their prevention of erosion and the provision of a weather-sheltered ecosystem in and under their foliage. They also play an important role in producing oxygen and reducing carbon dioxide in the atmosphere, as well as moderating ground temperatures. They are also elements in landscaping and agriculture, both for their aesthetic appeal and their orchard crops (such as apples). Wood from trees is a building material, as well as a primary energy source in many developing countries.\n"
            + "\n" + "\n"
            + "A flower, sometimes known as a bloom or blossom, is the reproductive structure found in flowering plants (plants of the division Magnoliophyta, also called angiosperms). The biological function of a flower is to effect reproduction, usually by providing a mechanism for the union of sperm with eggs. Flowers may facilitate outcrossing (fusion of sperm and eggs from different individuals in a population) or allow selfing (fusion of sperm and egg from the same flower). Some flowers produce diaspores without fertilization (parthenocarpy). Flowers contain sporangia and are the site where gametophytes develop. Flowers give rise to fruit and seeds. Many flowers have evolved to be attractive to animals, so as to cause them to be vectors for the transfer of pollen.\n"
            + "In addition to facilitating the reproduction of flowering plants, flowers have long been admired and used by humans to beautify their environment, and also as objects of romance, ritual, religion, medicine and as a source of food.\n"
            + "A stereotypical flower consists of four kinds of structures attached to the tip of a short stalk. Each of these kinds of parts is arranged in a whorl on the receptacle. The four main whorls (starting\n"
            + "\n" + "\n"
            + "Anchovies are a family (Engraulidae) of small, common salt-water forage fish. There are 144 species in 17 genera, found in the Atlantic, Indian, and Pacific Oceans. Anchovies are usually classified as an oily fish.\n"
            + "Anchovies are small, green fish with blue reflections due to a silver longitudinal stripe that runs from the base of the caudal fin. They range from 2 centimetres (0.79?in) to 40 centimetres (16?in) in adult length, and the body shape is variable with more slender fish in northern populations.\n"
            + "The snout is blunt with tiny, sharp teeth in both jaws. The snout contains a unique rostral organ, believed to be sensory in nature, although its exact function is unknown. The mouth is larger than that of herrings and silversides, two fish anchovies closely resemble in other respects. The anchovy eats plankton and fry (recently-hatched fish).\n"
            + "Anchovies are found in scattered areas throughout the world's oceans, but are concentrated in temperate waters, and are rare or absent in very cold or very warm seas. They are generally very accepting of a wide range of temperatures and salinity. Large schools can be found in shallow, brackish areas with muddy bottoms, as in estuaries and\n"
            + "\n" + "\n"
            + "Brighton /?bra?t?n/ is the major part of the city of Brighton and Hove (formed from the previous towns of Brighton, Hove, Portslade and several other villages) in East Sussex, England on the south coast of Great Britain. For administrative purposes, Brighton and Hove is not part of the non-metropolitan county of East Sussex, but remains part of the ceremonial county of East Sussex, within the historic county of Sussex.\n"
            + "The ancient settlement of Brighthelmstone dates from before Domesday Book (1086), but it emerged as a health resort featuring sea bathing during the 18th century and became a destination for day-trippers from London after the arrival of the railway in 1841. Brighton experienced rapid population growth, reaching a peak of over 160,000 by 1961. Modern Brighton forms part of the Brighton/Worthing/Littlehampton conurbation stretching along the coast, with a population of around 480,000.\n"
            + "Brighton has two universities and a medical school (which is operated jointly by both universities).\n"
            + "In the Domesday Book, Brighton was called Bristelmestune and a rent of 4,000 herring was established. In June 1514 Brighthelmstone was burnt to the ground by French raiders during a war\n"
            + "\n" + "\n"
            + "LOL, an abbreviation for laughing out loud, or laugh out loud, is a common element of Internet slang. It was used historically on Usenet but is now widespread in other forms of computer-mediated communication, and even face-to-face communication. It is one of many initialisms for expressing bodily reactions, in particular laughter, as text, including initialisms for more emphatic expressions of laughter such as LMAO (\"laugh(ing) my ass off\"), and ROTFL or ROFL (\"roll(ing) on the floor laughing\"). Other unrelated expansions include the now mostly historical \"lots of luck\" or \"lots of love\" used in letter-writing.\n"
            + "The list of acronyms \"grows by the month\" and they are collected along with emoticons and smileys into folk dictionaries that are circulated informally amongst users of Usenet, IRC, and other forms of (textual) computer-mediated communication. These initialisms are controversial, and several authors recommend against their use, either in general or in specific contexts such as business communications.\n"
            + "LOL was first documented in the Oxford English Dictionary in March 2011.\n"
            + "Laccetti (professor of humanities at Stevens Institute of Technology) and Molski, in their essay\n"
            + "\n" + "\n"
            + "Java is a programming language originally developed by James Gosling at Sun Microsystems (which has since merged into Oracle Corporation) and released in 1995 as a core component of Sun Microsystems' Java platform. The language derives much of its syntax from C and C++ but has a simpler object model and fewer low-level facilities. Java applications are typically compiled to bytecode (class file) that can run on any Java Virtual Machine (JVM) regardless of computer architecture. Java is a general-purpose, concurrent, class-based, object-oriented language that is specifically designed to have as few implementation dependencies as possible. It is intended to let application developers \"write once, run anywhere\" (WORA), meaning that code that runs on one platform does not need to be recompiled to run on another. Java is as of 2012 one of the most popular programming languages in use, particularly for client-server web applications, with a reported 10 million users.\n"
            + "The original and reference implementation Java compilers, virtual machines, and class libraries were developed by Sun from 1995. As of May 2007, in compliance with the specifications of the Java Community Process, Sun\n"
            + "\n" + "\n"
            + "Firefighters (historically firemen) are rescuers extensively trained primarily to extinguish hazardous fires that threaten civilian populations and property, and to rescue people from dangerous incidents, such as collapsed and burning buildings. The increasing complexity of modern industrialized life with an increase in the scale of hazards has created an increase in the skills needed in firefighting technology and a broadening of the firefighter-rescuer's remit. They sometimes provide emergency medical services. The fire service, or fire and rescue service, also known in some countries as the fire brigade or fire department, is one of the main emergency services. Firefighting and firefighters have become ubiquitous around the world, from wildland areas to urban areas, and aboard ships.\n"
            + "According to Merriam-Webster's Dictionary, the English word \"firefighter\" has been used since 1903. In recent decades it has become the preferred term, replacing the older \"fireman\", since many women serve as firefighters, and also because the term \"fireman\" can have other meanings, including someone who sets or stokes fires - exactly the opposite of the firefighting role.\n"
            + "In some countries,";

}
