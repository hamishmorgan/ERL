/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package edu.stanford.nlp;

import com.google.common.io.Closeables;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefClusterAnnotation;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefClusterIdAnnotation;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefGraphAnnotation;
import edu.stanford.nlp.ie.machinereading.structure.MachineReadingAnnotations.GenderAnnotation;
import edu.stanford.nlp.ie.regexp.RegexNERSequenceClassifier;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TrueCaseTextAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.CleanXmlAnnotator;
import edu.stanford.nlp.pipeline.CustomAnnotationSerializer;
import edu.stanford.nlp.pipeline.DefaultPaths;
import edu.stanford.nlp.pipeline.PTBTokenizerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.TokenizerAnnotator;
import edu.stanford.nlp.pipeline.TrueCaseAnnotator;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.IntTuple;
import edu.stanford.nlp.util.Pair;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import uk.ac.susx.mlcl.erl.snlp.AnnotationToXML;
import uk.ac.susx.mlcl.erl.test.AbstractTest;
import uk.ac.susx.mlcl.erl.test.Categories;

import javax.annotation.Nonnull;

/**
 * A collection of "tests" that experiment with the Stanford Core NLP library, and generally check
 * that it works it as expected.
 * <p/>
 * @author Hamish Morgan
 */
//@Ignore
public class StanfordNLPTest extends AbstractTest {

    public static void saveAnnotation(Annotation document, @Nonnull File file) throws IOException {
        boolean compressed = file.getName().endsWith(".gz");
        CustomAnnotationSerializer ser = new CustomAnnotationSerializer(compressed, true);
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file));
            ser.save(document, os);
            os.flush();
        } finally {
            Closeables.close(os, true);
        }
    }

    public static Annotation loadAnnotation(@Nonnull File file)
            throws IOException, ClassNotFoundException {
        final boolean compressed = file.getName().endsWith(".gz");
        final CustomAnnotationSerializer ser =
                new CustomAnnotationSerializer(compressed, true);
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            return ser.load(is);
        } finally {
            Closeables.close(is, true);
        }
    }
//
//    @Test
//    @Category(Categories.IntegrationTests.class)
//    public void getSerializerFor() throws IOException {
//
//        final boolean verbose = false;
//
//        String path = "freebase_brighton.txt";
//        String text = readTestData(path);
//        Annotation document = new Annotation(text);
//
//        String ext = ".s.gz";
//        {
//            TokenizerAnnotator tokeniser = new PTBTokenizerAnnotator(verbose);
//            tokeniser.annotate(document);
//        }
//        {
//            WordsToSentencesAnnotator wts = new WordsToSentencesAnnotator(verbose);
//            wts.annotate(document);
//        }
//
//        saveAnnotation(document, new File(TEST_DATA_PATH, path + "-tok-ss" + ext));
//
//        {
//            POSTaggerAnnotator pta = new POSTaggerAnnotator(
//                    DefaultPaths.DEFAULT_POS_MODEL, verbose, Integer.MAX_VALUE);
//            pta.annotate(document);
//        }
//        saveAnnotation(document, new File(TEST_DATA_PATH, path + "-tok-ss-pos" + ext));
//
//        {
//            MorphaAnnotator morph = new MorphaAnnotator(verbose);
//            morph.annotate(document);
//        }
//
//        saveAnnotation(document, new File(TEST_DATA_PATH, path + "-tok-ss-pos-lemma" + ext));
//        
////        
////
////        final CharacterShapeAnnotator characterClassC18N = new CharacterShapeAnnotator();
////        pipeline.addAnnotator(characterClassC18N);
////
////        pipeline.annotate(document);
////
////        for (CoreLabel token : document.get(CoreAnnotations.TokensAnnotation.class)) {
////            String word = token.get(CoreAnnotations.TextAnnotation.class);
////            String clazz = token.get(CharacterShapeAnnotator.Annotation.class);
////
////            Assert.assertNotNull(word);
////            Assert.assertNotNull(clazz);
////            Assert.assertEquals(clazz.length(), word.length());
////
////            System.out.printf("[%d,%d] %s => %s%n",
////                              token.beginPosition(), token.endPosition(), word, clazz);
////        }
//    }

    /**
     * The one an only API usage example form the documentation... or at least the only one I've
     * ever found. Good job!
     * <p/>
     * @throws IOException
     */
    @Test
    @Category(Categories.SlowTests.class)
    public void snlpUsageExample() throws IOException {
        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
        Properties props = new Properties();
        props.put("annotators",
                  "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);


        // read some TEXT2 in the TEXT2 variable
        String text = readTestData("freebase_brighton.txt");


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

            System.out.println("tree: " + tree);

            // this is the Stanford dependency graph of the current sentence
            SemanticGraph dependencies = sentence
                    .get(
                    SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);

            System.out.println("dependencies: " + dependencies);
        }

        // This is the coreference link graph
        // Each chain stores a set of mentions that link to each other,
        // along with a method for getting the most representative mention
        // Both sentence and token offsets start at 1!
        Map<Integer, CorefChain> graph =
                document.get(CorefChainAnnotation.class);
        System.out.println(graph);
    }

    /**
     * Run just the English tokeniser, on some wikipedia description of "Brighton" topic.
     * <p/>
     * @throws IOException
     */
    @Test
    public void testTokeniSe() throws IOException {
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

        String text = readTestData("freebase_brighton.txt");
        Annotation document = new Annotation(text);

        pipeline.annotate(document);

        for (CoreLabel token : document.get(TokensAnnotation.class)) {
            System.out.printf("[%d,%d] %s%n",
                              token.beginPosition(), token.endPosition(),
                              token.get(TextAnnotation.class));
        }

    }

    @Test
    public void testSentenceSplit() throws IOException {

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
        String text = readTestData("freebase_brighton.txt");

        // create an empty Annotation just with the given TEXT2
        Annotation document = new Annotation(text);

        // run all Annotators on this TEXT2
        pipeline.annotate(document);

        for (CoreMap sentence : document.get(SentencesAnnotation.class)) {
            System.out.println("BEGIN SENTENCE");
            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                String word = token.get(TextAnnotation.class);
                System.out.printf("[%d,%d] %s%n",
                                  token.beginPosition(), token.endPosition(), word);
            }
            System.out.println("END SENTENCE");
        }

    }

    @Test
    public void testSentenceSplitSansProperties() throws IOException {

        final boolean verbose = false;

        AnnotationPipeline pipeline = new AnnotationPipeline();

        TokenizerAnnotator tokeniser = new PTBTokenizerAnnotator(verbose);
        pipeline.addAnnotator(tokeniser);

        WordsToSentencesAnnotator wts = new WordsToSentencesAnnotator(verbose);
        wts.setOneSentence(false);
        pipeline.addAnnotator(wts);

        String text = readTestData("freebase_brighton.txt");

        Annotation document = new Annotation(text);

        pipeline.annotate(document);

        for (CoreMap sentence : document.get(SentencesAnnotation.class)) {
            System.out.println("BEGIN SENTENCE");
            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                String word = token.get(TextAnnotation.class);
                System.out.printf("[%d,%d] %s%n",
                                  token.beginPosition(), token.endPosition(), word);
            }
            System.out.println("END SENTENCE");
        }
    }

    @Test
    public void testCleanXML() throws IOException {
        Properties props = new Properties();
        props.put("annotators",
                  StanfordCoreNLP.STANFORD_TOKENIZE
                + "," + StanfordCoreNLP.STANFORD_CLEAN_XML
                + "," + StanfordCoreNLP.STANFORD_SSPLIT);

        props.put("clean.xmltags", ".*");
        props.put("clean.sentenceendingtags", "p|div|br");
        props.put("clean.allowflawedxml", CleanXmlAnnotator.DEFAULT_ALLOW_FLAWS);
        props.put("clean.datetags", CleanXmlAnnotator.DEFAULT_DATE_TAGS);

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        String text = readTestData("wikipedia_brighton.html");
//        String text = IOUtils.toString(URI.create("http://en.wikipedia.org/wiki/Brighton"));

        Annotation document = new Annotation(text);

        pipeline.annotate(document);

//        System.out.println(document);


        document.get(TokensAnnotation.class);

        for (CoreMap sentence : document.get(SentencesAnnotation.class)) {

            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            System.out.println("BEGIN SENTENCE");

            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {

                String word = token.get(TextAnnotation.class);

                System.out.printf("[%d,%d] %s%n",
                                  token.beginPosition(), token.endPosition(), word);
            }
            System.out.println("END SENTENCE");
        }
    }

    @Test
    public void testPOSTagger() throws IOException {

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
        String text = readTestData("freebase_brighton.txt");

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
                                  word, pos);
            }
            System.out.println("END SENTENCE");
        }
    }

    @Test
    public void testLematisation() throws IOException {

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");

        // There are no options for the lematiser 

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        String text = readTestData("freebase_brighton.txt");

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
                                  word, pos, lemma);
            }
            System.out.println("END SENTENCE");
        }
    }

    @Test
    public void testNER() throws IOException {

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

        String path = "freebase_brighton.txt";
        String ext = ".s.gz";
        String text = readTestData(path);


        Annotation document = new Annotation(text);

        pipeline.annotate(document);

        saveAnnotation(document, new File(TEST_DATA_PATH,
                                          path + "-tok-sent-pos-lemma-ner" + ext));


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
                                  word, pos, lemma, ne);
            }

        }



    }

    @Test
    public void testRegexNER() throws IOException {

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, regexner");

        // "edu/stanford/nlp/models/regexner/type_map_clean"
        // 
        props.put("regexner.mapping", "src/test/models/edu/stanford/nlp/models/brighton_type_map");

        props.put("regexner.ignorecase", "false");

        props.put("regexner.validpospattern", RegexNERSequenceClassifier.DEFAULT_VALID_POS);

        // props.put("ssplit.boundariesToDiscard", "");

        //  props.put("ssplit.htmlBoundariesToDiscard", "");

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        String text = readTestData("freebase_brighton.txt");

        // create an empty Annotation just with the given TEXT2
        Annotation document = new Annotation(text);

        // run all Annotators on this TEXT2
        pipeline.annotate(document);

//        RegexNERAnnotator

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            System.out.println("BEGIN SENTENCE");

            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {

                String word = token.get(TextAnnotation.class);
                String ne = token.get(NamedEntityTagAnnotation.class);

                System.out.printf("[%d,%d] %s\t%s%n",
                                  token.beginPosition(), token.endPosition(),
                                  word, ne);
            }
            System.out.println("END SENTENCE");
        }
    }

    @Test
    public void testGender() throws IOException {

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, gender");

        props.put("gender.firstnames", DefaultPaths.DEFAULT_GENDER_FIRST_NAMES);


        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        String text = readTestData("popular_names.txt");

        Annotation document = new Annotation(text);

        pipeline.annotate(document);

        for (CoreMap sentence : document.get(SentencesAnnotation.class)) {
            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {

                String word = token.get(TextAnnotation.class);
                String gender = token.get(GenderAnnotation.class);

                System.out.printf("[%4d|%4d] %10s\t%6s%n",
                                  token.beginPosition(), token.endPosition(),
                                  word, gender == null ? "" : gender);
            }
        }
    }

    @Test
    @Ignore("hoses my machine for some reason...")
    public void testTrueCase() throws IOException {

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, truecase");

        props.put("truecase.model", DefaultPaths.DEFAULT_TRUECASE_MODEL);
        props.put("truecase.bias", TrueCaseAnnotator.DEFAULT_MODEL_BIAS);
        props.put("truecase.mixedcasefile", DefaultPaths.DEFAULT_TRUECASE_DISAMBIGUATION_LIST);


        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        String text = readTestData("freebase_brighton.txt");

        Annotation document = new Annotation(text);

        pipeline.annotate(document);

        for (CoreMap sentence : document.get(SentencesAnnotation.class)) {
            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {

                String word = token.get(TextAnnotation.class);
                String truecase = token.get(TrueCaseTextAnnotation.class);

                System.out.printf("[%4d|%4d] %10s\t%6s%n",
                                  token.beginPosition(), token.endPosition(),
                                  word, truecase);
            }
        }
    }

    /**
     * Run the Stanford parse annotator.
     * <p/>
     * Provides full syntactic analysis, using both the constituent and the dependency
     * representations. The constituent-based output is saved in TreeAnnotation. We generate three
     * dependency-based outputs, as follows: basic, uncollapsed dependencies, saved in
     * BasicDependenciesAnnotation; collapsed dependencies saved in CollapsedDependenciesAnnotation;
     * and collapsed dependencies with processed coordinations, in
     * CollapsedCCProcessedDependenciesAnnotation. Most users of our parser will prefer the latter
     * representation. For more details on the parser, please see this page. For more details about
     * the dependencies, please refer to this page.
     * <p/>
     * <
     * p/> Generated annotations: TreeAnnotation, BasicDependenciesAnnotation,
     * CollapsedDependenciesAnnotation, CollapsedCCProcessedDependenciesAnnotation
     * <p/>
     * @throws IOException
     */
    @Test
    public void testStanfordParser() throws IOException {

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, parse");

        // stanford, charniak
        props.put("parser.type", "stanford");

        /*
         * parser.maxlen: if set, the annotator parses only sentences shorter (in terms of number of
         * tokens) than this number. For longer sentences, the parser creates a flat structure,
         * where every token is assigned to the non-terminal X. This is useful when parsing noisy
         * web text, which may generate arbitrarily long sentences. By default, this option is not
         * set.
         */
        props.put("parser.maxlen", Integer.toString(Integer.MAX_VALUE));

        /*
         * parser.model: parsing model to use. There is no need to explicitly set this option,
         * unless you want to use a different parsing model (for advanced developers only). By
         * default, this is set to the parsing model included in the stanford-corenlp-models JAR
         * file.
         */
        props.put("parse.model", LexicalizedParser.DEFAULT_PARSER_LOC);



        props.put("props.debug", Boolean.toString(false));


        props.put("props.flags", ParserAnnotator.DEFAULT_FLAGS);

        // Function<Tree, Tree>
//        props.put("props.treemap", null);


        props.put("parser.maxtime", Integer.toString(Integer.MAX_VALUE));


        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        String text = readTestData("freebase_brighton.txt");

        Annotation document = new Annotation(text);

        pipeline.annotate(document);

        for (CoreMap sentence : document.get(SentencesAnnotation.class)) {

            Tree tree = sentence.get(TreeAnnotation.class);

            System.out.println("tree: " + tree);

            SemanticGraph dep1 = sentence
                    .get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
            System.out.println("basic dependencies: " + dep1);

            SemanticGraph dep2 = sentence
                    .get(SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation.class);
            System.out.println("collapsed dependencies: " + dep2);

            // this is the Stanford dependency graph of the current sentence
            SemanticGraph dep3 = sentence
                    .get(
                    SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
            System.out.println("collaprsed ccp dependencies: " + dep3);

        }


        saveAnnotation(document,
                       new File(TEST_DATA_PATH,
                                "freebase_brighton.txt-tok-sent-parse.s.gz"));


    }

    @Test
    public void testDCoref() throws IOException, InstantiationException {

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, parse, lemma, ner, dcoref");


        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        String text = readTestData("freebase_brighton.txt");

        Annotation document = new Annotation(text);

        pipeline.annotate(document);


//import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
//import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefClusterAnnotation;
//import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefGraphAnnotation;

        System.out.println("\n\nCorefChainAnnotation:\n");
        Map<Integer, CorefChain> chains = document.get(CorefChainAnnotation.class);
        for (Entry<Integer, CorefChain> entries : chains.entrySet()) {
            if (entries.getValue().getCorefMentions().size() > 1) {
                System.out.println(entries.getKey());
                System.out.println(entries.getValue());
                System.out.println();
            }
        }

        System.out.println("\n\nCorefGraphAnnotation:\n");
        List<Pair<IntTuple, IntTuple>> graph = document.get(CorefGraphAnnotation.class);
        System.out.println(graph);


        System.out.println("\n\nCorefClusterAnnotation:\n");
        for (CoreMap sentence : document.get(SentencesAnnotation.class)) {

            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {

                String word = token.get(TextAnnotation.class);

                Integer clusterId = token.get(CorefClusterIdAnnotation.class);

                Set<CoreLabel> cluster = token.get(CorefClusterAnnotation.class);


                System.out.printf("%s %s %s",
                                  word,
                                  clusterId == null ? "" : Integer.toString(clusterId),
                                  cluster == null ? "" : cluster.toString());

//                
//                
//                Set<CoreLabel> cluster = token.get(CorefClusterAnnotation.class);
//                System.out.println(cluster);
//                
//                CorefClusterIdAnnotation
//                
//                List<Pair<IntTuple, IntTuple>> graph = token.get(CorefGraphAnnotation.class);
//                System.out.println(graph);

//                
//                
//                System.out.printf("[%4d|%4d] %10s\t%6s%n",
//                                  token.beginPosition(), token.endPosition(),
//                                  word, cluster.toString());
//
//
//                System.out.printf("[%4d|%4d] %10s\t%6s%n",
//                                  token.beginPosition(), token.endPosition(),
//                                  word, graph.toString());
            }


        }



        saveAnnotation(document,
                       new File(TEST_DATA_PATH,
                                "freebase_brighton.txt-tok-sent-parse-lemma-ner-dcoref.s.gz"));


//
//        StanfordAnnotationToXML ax = new StanfordAnnotationToXML();
//        ax.xmlPrint(document, System.out);


        AnnotationToXML ax2 = AnnotationToXML.builder().build();
        ax2.xmlPrint(document, System.out);
    }
}
