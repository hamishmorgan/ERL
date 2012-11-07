/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.webapp;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.AnnotatorPool;
import edu.stanford.nlp.pipeline.CleanXmlAnnotator;
import edu.stanford.nlp.pipeline.PTBTokenizerAnnotator;
import edu.stanford.nlp.pipeline.WhitespaceTokenizerAnnotator;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.util.Factory;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPathExpressionException;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import uk.ac.susx.mlcl.erl.snlp.AnnotationToXMLSerializer;

/**
 *
 * @author hiam20
 */
public class WebApp {

    public static void main(String[] args) throws XPathExpressionException {

        Properties props = new Properties();
        props.put("annotators", "tokenize");
        props.put("tokenize.whitespace", "false");


        // tokenize.options:
        // Accepts the options of PTBTokenizer for example, things like 
        //  "americanize=false" 
        //  "strictTreebank3=true,
        //   untokenizable=allKeep".
        props.put("tokenize.options", "untokenizable=allKeep");



        final AnnotatorPool pool = new AnnotatorPool();
        pool.register("tokenize", new TokenizerAnnotatorFactory(props));
        pool.register("cleanXml", new CleanXmlAnnotatorFactory(props));
        pool.register("ssplit", new SentenceSplitAnnotatorFactory(props));






//        
//        
//
//        final StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
//       


        AnnotationToXMLSerializer.Builder builder = AnnotationToXMLSerializer.builder();
        builder.addAnnotationToIgnore(CoreAnnotations.XmlContextAnnotation.class);
        final AnnotationToXMLSerializer toXml = builder.build();

        Spark.get(new Route("/annotate/tokenise/:url") {
            @Override
            public Object handle(Request request, Response response) {
                try {
                    String urlString = URLDecoder.decode(request.params(":url"));
                    URL url = new URL(urlString);


                    String text = IOUtils.slurpURL(url);
//                    System.out.println(text);
                    Annotation document = new Annotation(text);


                    AnnotationPipeline pipeline = new AnnotationPipeline();
                    pipeline.addAnnotator(pool.get("tokenize"));
                    pipeline.addAnnotator(pool.get("cleanXml"));
                    pipeline.addAnnotator(pool.get("ssplit"));

                    pipeline.annotate(document);
//
//                    for (CoreLabel token : document.get(CoreAnnotations.TokensAnnotation.class)) {
//                        System.out.printf("[%d,%d] %s%n",
//                                token.beginPosition(), token.endPosition(),
//                                token.get(CoreAnnotations.TextAnnotation.class));
//                    }


                    toXml.xmlPrint(document, response.raw().getOutputStream());

                    return "";
                } catch (IOException ex) {
                    Logger.getLogger(WebApp.class.getName()).log(Level.SEVERE, null, ex);
                    response.status(500);
                    return ex.getMessage();
                }
            }
        });

    }

    public static abstract class AbstractAnnotatorFactory implements Factory<Annotator> {

        protected Properties props;

        public AbstractAnnotatorFactory(Properties props) {
            this.props = props;
        }
    }

    public static class TokenizerAnnotatorFactory extends AbstractAnnotatorFactory {

        public static final String NEWLINE_SPLITTER_PROPERTY = "ssplit.eolonly";

        public TokenizerAnnotatorFactory(Properties props) {
            super(props);
        }
        private static final long serialVersionUID = 1L;

        public Annotator create() {
            if (Boolean.valueOf(props.getProperty("tokenize.whitespace",
                    "false"))) {
                return new WhitespaceTokenizerAnnotator(props);
            } else {
                String options = props.getProperty("tokenize.options", PTBTokenizerAnnotator.DEFAULT_OPTIONS);
                boolean keepNewline = Boolean.valueOf(props.getProperty(NEWLINE_SPLITTER_PROPERTY, "false"));
                // If the user specifies "tokenizeNLs=false" in tokenize.options, then this default will
                // be overridden.
                if (keepNewline) {
                    options = "tokenizeNLs," + options;
                }
                return new PTBTokenizerAnnotator(false, options);
            }
        }
    }

    public static class CleanXmlAnnotatorFactory extends AbstractAnnotatorFactory {

        public static final String NEWLINE_SPLITTER_PROPERTY = "ssplit.eolonly";

        public CleanXmlAnnotatorFactory(Properties props) {
            super(props);
        }
        private static final long serialVersionUID = 1L;

        public Annotator create() {
            String xmlTags =
                    props.getProperty("clean.xmltags",
                    CleanXmlAnnotator.DEFAULT_XML_TAGS);
            String sentenceEndingTags =
                    props.getProperty("clean.sentenceendingtags",
                    CleanXmlAnnotator.DEFAULT_SENTENCE_ENDERS);
            String allowFlawedString = props.getProperty("clean.allowflawedxml");
            boolean allowFlawed = CleanXmlAnnotator.DEFAULT_ALLOW_FLAWS;
            if (allowFlawedString != null) {
                allowFlawed = Boolean.valueOf(allowFlawedString);
            }
            String dateTags =
                    props.getProperty("clean.datetags",
                    CleanXmlAnnotator.DEFAULT_DATE_TAGS);
            return new CleanXmlAnnotator(xmlTags,
                    sentenceEndingTags,
                    dateTags,
                    allowFlawed);
        }
    }

    public static class SentenceSplitAnnotatorFactory extends AbstractAnnotatorFactory {

        public static final String NEWLINE_SPLITTER_PROPERTY = "ssplit.eolonly";

        public SentenceSplitAnnotatorFactory(Properties props) {
            super(props);
        }
        private static final long serialVersionUID = 1L;

        public Annotator create() {
            boolean nlSplitting = Boolean.valueOf(props.getProperty(NEWLINE_SPLITTER_PROPERTY, "false"));
            if (nlSplitting) {
                boolean whitespaceTokenization = Boolean.valueOf(props.getProperty("tokenize.whitespace", "false"));
                WordsToSentencesAnnotator wts;
                if (whitespaceTokenization) {
                    if (System.getProperty("line.separator").equals("\n")) {
                        wts = WordsToSentencesAnnotator.newlineSplitter(false, "\n");
                    } else {
                        // throw "\n" in just in case files use that instead of
                        // the system separator
                        wts = WordsToSentencesAnnotator.newlineSplitter(false, System.getProperty("line.separator"), "\n");
                    }
                } else {
                    wts = WordsToSentencesAnnotator.newlineSplitter(false, PTBTokenizer.getNewlineToken());
                }
                return wts;
            } else {
                WordsToSentencesAnnotator wts =
                        new WordsToSentencesAnnotator(false);

                // regular boundaries
                String bounds = props.getProperty("ssplit.boundariesToDiscard");
                if (bounds != null) {
                    String[] toks = bounds.split(",");
                    // for(int i = 0; i < toks.length; i ++)
                    //   System.err.println("BOUNDARY: " + toks[i]);
                    wts.setSentenceBoundaryToDiscard(new HashSet<String>(Arrays.asList(toks)));
                }

                // HTML boundaries
                bounds = props.getProperty("ssplit.htmlBoundariesToDiscard");
                if (bounds != null) {
                    String[] toks = bounds.split(",");
                    wts.addHtmlSentenceBoundaryToDiscard(new HashSet<String>(Arrays.asList(toks)));
                }

                // Treat as one sentence
                String isOneSentence = props.getProperty("ssplit.isOneSentence");
                if (isOneSentence != null) {
                    wts.setOneSentence(Boolean.parseBoolean(isOneSentence));
                }

                return wts;
            }
        }
    }
//
//    private static synchronized AnnotatorPool getDefaultAnnotatorPool(final Properties props) {
//        // if the pool already exists reuse!
//        if (pool != null) {
//            return pool;
//        }
//
//        pool = new AnnotatorPool();
//
//        //
//        // tokenizer: breaks text into a sequence of tokens
//        // this is required for all following annotators!
//        //
//
//        //
//        // sentence splitter: splits the above sequence of tokens into
//        // sentences.  This is required when processing entire documents or
//        // text consisting of multiple sentences
//        //
//        pool.register(STANFORD_SSPLIT, new Factory<Annotator>() {
//            private static final long serialVersionUID = 1L;
//
//            public Annotator create() {
//                boolean nlSplitting = Boolean.valueOf(props.getProperty(NEWLINE_SPLITTER_PROPERTY, "false"));
//                if (nlSplitting) {
//                    boolean whitespaceTokenization = Boolean.valueOf(props.getProperty("tokenize.whitespace", "false"));
//                    WordsToSentencesAnnotator wts;
//                    if (whitespaceTokenization) {
//                        if (System.getProperty("line.separator").equals("\n")) {
//                            wts = WordsToSentencesAnnotator.newlineSplitter(false, "\n");
//                        } else {
//                            // throw "\n" in just in case files use that instead of
//                            // the system separator
//                            wts = WordsToSentencesAnnotator.newlineSplitter(false, System.getProperty("line.separator"), "\n");
//                        }
//                    } else {
//                        wts = WordsToSentencesAnnotator.newlineSplitter(false, PTBTokenizer.getNewlineToken());
//                    }
//                    return wts;
//                } else {
//                    WordsToSentencesAnnotator wts =
//                            new WordsToSentencesAnnotator(false);
//
//                    // regular boundaries
//                    String bounds = props.getProperty("ssplit.boundariesToDiscard");
//                    if (bounds != null) {
//                        String[] toks = bounds.split(",");
//                        // for(int i = 0; i < toks.length; i ++)
//                        //   System.err.println("BOUNDARY: " + toks[i]);
//                        wts.setSentenceBoundaryToDiscard(new HashSet<String>(Arrays.asList(toks)));
//                    }
//
//                    // HTML boundaries
//                    bounds = props.getProperty("ssplit.htmlBoundariesToDiscard");
//                    if (bounds != null) {
//                        String[] toks = bounds.split(",");
//                        wts.addHtmlSentenceBoundaryToDiscard(new HashSet<String>(Arrays.asList(toks)));
//                    }
//
//                    // Treat as one sentence
//                    String isOneSentence = props.getProperty("ssplit.isOneSentence");
//                    if (isOneSentence != null) {
//                        wts.setOneSentence(Boolean.parseBoolean(isOneSentence));
//                    }
//
//                    return wts;
//                }
//            }
//        });
//
//        //
//        // POS tagger
//        //
//        pool.register(STANFORD_POS, new Factory<Annotator>() {
//            private static final long serialVersionUID = 1L;
//
//            public Annotator create() {
//                try {
//                    String maxLenStr = props.getProperty("pos.maxlen");
//                    int maxLen = Integer.MAX_VALUE;
//                    if (maxLenStr != null) {
//                        maxLen = Integer.parseInt(maxLenStr);
//                    }
//                    return new POSTaggerAnnotator(props.getProperty("pos.model", DefaultPaths.DEFAULT_POS_MODEL), false, maxLen);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        });
//
//        //
//        // Lemmatizer
//        //
//        pool.register(STANFORD_LEMMA, new Factory<Annotator>() {
//            private static final long serialVersionUID = 1L;
//
//            public Annotator create() {
//                return new MorphaAnnotator(false);
//            }
//        });
//
//        //
//        // NER
//        //
//        pool.register(STANFORD_NER, new Factory<Annotator>() {
//            private static final long serialVersionUID = 1L;
//
//            public Annotator create() {
//                List<String> models = new ArrayList<String>();
//                List<Pair<String, String>> modelNames = new ArrayList<Pair<String, String>>();
//                modelNames.add(new Pair<String, String>("ner.model", null));
//                modelNames.add(new Pair<String, String>("ner.model.3class", DefaultPaths.DEFAULT_NER_THREECLASS_MODEL));
//                modelNames.add(new Pair<String, String>("ner.model.7class", DefaultPaths.DEFAULT_NER_MUC_MODEL));
//                modelNames.add(new Pair<String, String>("ner.model.MISCclass", DefaultPaths.DEFAULT_NER_CONLL_MODEL));
//
//                for (Pair<String, String> name : modelNames) {
//                    String model = props.getProperty(name.first, name.second);
//                    if (model != null && model.length() > 0) {
//                        models.addAll(Arrays.asList(model.split(",")));
//                    }
//                }
//                if (models.isEmpty()) {
//                    throw new RuntimeException("no NER models specified");
//                }
//                NERClassifierCombiner nerCombiner;
//                try {
//                    boolean applyNumericClassifiers =
//                            PropertiesUtils.getBool(props,
//                            NERClassifierCombiner.APPLY_NUMERIC_CLASSIFIERS_PROPERTY,
//                            NERClassifierCombiner.APPLY_NUMERIC_CLASSIFIERS_DEFAULT);
//                    boolean useSUTime =
//                            PropertiesUtils.getBool(props,
//                            NumberSequenceClassifier.USE_SUTIME_PROPERTY,
//                            NumberSequenceClassifier.USE_SUTIME_DEFAULT);
//                    nerCombiner = new NERClassifierCombiner(applyNumericClassifiers,
//                            useSUTime, props,
//                            models.toArray(new String[models.size()]));
//                } catch (FileNotFoundException e) {
//                    throw new RuntimeException(e);
//                }
//                // ms 2009, no longer needed: the functionality of all these annotators is now included in NERClassifierCombiner
//        /*
//                 AnnotationPipeline pipeline = new AnnotationPipeline();
//                 pipeline.addAnnotator(new NERCombinerAnnotator(nerCombiner, false));
//                 pipeline.addAnnotator(new NumberAnnotator(false));
//                 pipeline.addAnnotator(new TimeWordAnnotator(false));
//                 pipeline.addAnnotator(new QuantifiableEntityNormalizingAnnotator(false, false));
//                 return pipeline;
//                 */
//                return new NERCombinerAnnotator(nerCombiner, false);
//            }
//        });
//
//        //
//        // Regex NER
//        //
//        pool.register(STANFORD_REGEXNER, new Factory<Annotator>() {
//            private static final long serialVersionUID = 1L;
//
//            public Annotator create() {
//                String mapping = props.getProperty("regexner.mapping", DefaultPaths.DEFAULT_REGEXNER_RULES);
//                String ignoreCase = props.getProperty("regexner.ignorecase", "false");
//                String validPosPattern = props.getProperty("regexner.validpospattern", RegexNERSequenceClassifier.DEFAULT_VALID_POS);
//                return new RegexNERAnnotator(mapping, Boolean.valueOf(ignoreCase), validPosPattern);
//            }
//        });
//
//        //
//        // Gender Annotator
//        //
//        pool.register(STANFORD_GENDER, new Factory<Annotator>() {
//            private static final long serialVersionUID = 1L;
//
//            public Annotator create() {
//                return new GenderAnnotator(false, props.getProperty("gender.firstnames", DefaultPaths.DEFAULT_GENDER_FIRST_NAMES));
//            }
//        });
//
//
//        //
//        // True caser
//        //
//        pool.register(STANFORD_TRUECASE, new Factory<Annotator>() {
//            private static final long serialVersionUID = 1L;
//
//            public Annotator create() {
//                String model = props.getProperty("truecase.model", DefaultPaths.DEFAULT_TRUECASE_MODEL);
//                String bias = props.getProperty("truecase.bias", TrueCaseAnnotator.DEFAULT_MODEL_BIAS);
//                String mixed = props.getProperty("truecase.mixedcasefile", DefaultPaths.DEFAULT_TRUECASE_DISAMBIGUATION_LIST);
//                return new TrueCaseAnnotator(model, bias, mixed, false);
//            }
//        });
//
//        //
//        // Post-processing tokenization rules for the NFL domain
//        //
//        pool.register(STANFORD_NFL_TOKENIZE, new Factory<Annotator>() {
//            private static final long serialVersionUID = 1L;
//
//            public Annotator create() {
//                final String className =
//                        "edu.stanford.nlp.pipeline.NFLTokenizerAnnotator";
//                return ReflectionLoading.loadByReflection(className);
//            }
//        });
//
//        //
//        // Entity and relation extraction for the NFL domain
//        //
//        pool.register(STANFORD_NFL, new Factory<Annotator>() {
//            private static final long serialVersionUID = 1L;
//
//            public Annotator create() {
//                // these paths now extracted inside c'tor
//                // String gazetteer = props.getProperty("nfl.gazetteer", DefaultPaths.DEFAULT_NFL_GAZETTEER);
//                // String entityModel = props.getProperty("nfl.entity.model", DefaultPaths.DEFAULT_NFL_ENTITY_MODEL);
//                // String relationModel = props.getProperty("nfl.relation.model", DefaultPaths.DEFAULT_NFL_RELATION_MODEL);
//                final String className = "edu.stanford.nlp.pipeline.NFLAnnotator";
//                return ReflectionLoading.loadByReflection(className, props);
//            }
//        });
//
//        //
//        // Parser
//        //
//        pool.register(STANFORD_PARSE, new Factory<Annotator>() {
//            private static final long serialVersionUID = 1L;
//
//            public Annotator create() {
//                String parserType = props.getProperty("parser.type", "stanford");
//                String maxLenStr = props.getProperty("parser.maxlen");
//
//                if (parserType.equalsIgnoreCase("stanford")) {
//                    ParserAnnotator anno = new ParserAnnotator("parser", props);
//                    return anno;
//                } else if (parserType.equalsIgnoreCase("charniak")) {
//                    String model = props.getProperty("parser.model");
//                    String parserExecutable = props.getProperty("parser.executable");
//                    if (model == null || parserExecutable == null) {
//                        throw new RuntimeException("Both parser.model and parser.executable properties must be specified if parser.type=charniak");
//                    }
//                    int maxLen = 399;
//                    if (maxLenStr != null) {
//                        maxLen = Integer.parseInt(maxLenStr);
//                    }
//
//                    CharniakParserAnnotator anno = new CharniakParserAnnotator(model, parserExecutable, false, maxLen);
//
//                    return anno;
//                } else {
//                    throw new RuntimeException("Unknown parser type: " + parserType + " (currently supported: stanford and charniak)");
//                }
//            }
//        });
//
//        //
//        // Coreference resolution
//        //
//        pool.register(STANFORD_DETERMINISTIC_COREF, new Factory<Annotator>() {
//            private static final long serialVersionUID = 1L;
//
//            public Annotator create() {
//                return new DeterministicCorefAnnotator(props);
//            }
//        });
//
//        // add annotators loaded via reflection from classnames specified
//        // in the properties
//        for (Object propertyKey : props.keySet()) {
//            if (!(propertyKey instanceof String)) {
//                continue; // should this be an Exception?
//            }
//            String property = (String) propertyKey;
//            if (property.startsWith(CUSTOM_ANNOTATOR_PREFIX)) {
//                final String customName =
//                        property.substring(CUSTOM_ANNOTATOR_PREFIX.length());
//                final String customClassName = props.getProperty(property);
//                System.err.println("Registering annotator " + customName
//                        + " with class " + customClassName);
//                pool.register(customName, new Factory<Annotator>() {
//                    private static final long serialVersionUID = 1L;
//                    private final String name = customName;
//                    private final String className = customClassName;
//                    private final Properties properties = props;
//
//                    public Annotator create() {
//                        return ReflectionLoading.loadByReflection(className, name,
//                                properties);
//                    }
//                });
//            }
//        }
//
//
//        //
//        // add more annotators here!
//        //
//        return pool;
//    }
}
