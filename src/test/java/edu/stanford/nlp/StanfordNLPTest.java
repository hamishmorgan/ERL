/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package edu.stanford.nlp;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
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
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.CleanXmlAnnotator;
import edu.stanford.nlp.pipeline.DefaultPaths;
import edu.stanford.nlp.pipeline.PTBTokenizerAnnotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.TokenizerAnnotator;
import edu.stanford.nlp.pipeline.TrueCaseAnnotator;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import uk.ac.susx.mlcl.erl.test.AbstractTest;
import uk.ac.susx.mlcl.erl.test.Categories;

/**
 * A collection of "tests" that experiment with the Stanford Core NLP library, and generally check
 * that it works it as expected.
 * <p/>
 * @author Hamish Morgan
 */
//@Ignore
public class StanfordNLPTest extends AbstractTest {

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
                    .get(CollapsedCCProcessedDependenciesAnnotation.class);

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

        String text = readTestData("freebase_brighton.txt");


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
    public void testTrueCase() throws IOException {

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, truecase");

        props.put("truecase.model", DefaultPaths.DEFAULT_TRUECASE_MODEL);
        props.put("truecase.bias", TrueCaseAnnotator.DEFAULT_MODEL_BIAS);
        props.put("truecase.mixedcasefile", DefaultPaths.DEFAULT_TRUECASE_DISAMBIGUATION_LIST);


        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        String text = readTestData("popular_names.txt");

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
}
