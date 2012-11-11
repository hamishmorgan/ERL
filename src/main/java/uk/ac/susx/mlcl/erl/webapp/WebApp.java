/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.webapp;

import edu.stanford.nlp.ie.NERClassifierCombiner;
import edu.stanford.nlp.ie.regexp.NumberSequenceClassifier;
import edu.stanford.nlp.ie.regexp.RegexNERSequenceClassifier;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.AnnotatorPool;
import edu.stanford.nlp.pipeline.CharniakParserAnnotator;
import edu.stanford.nlp.pipeline.CleanXmlAnnotator;
import edu.stanford.nlp.pipeline.DefaultPaths;
import edu.stanford.nlp.pipeline.DeterministicCorefAnnotator;
import edu.stanford.nlp.pipeline.GenderAnnotator;
import edu.stanford.nlp.pipeline.MorphaAnnotator;
import edu.stanford.nlp.pipeline.NERCombinerAnnotator;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.PTBTokenizerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;
import edu.stanford.nlp.pipeline.RegexNERAnnotator;
import edu.stanford.nlp.pipeline.TrueCaseAnnotator;
import edu.stanford.nlp.pipeline.WhitespaceTokenizerAnnotator;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.util.Factory;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.PropertiesUtils;
import edu.stanford.nlp.util.ReflectionLoading;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownServiceException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPathExpressionException;
import nu.xom.DocType;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.NodeFactory;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.xslt.XSLException;
import nu.xom.xslt.XSLTransform;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import uk.ac.susx.mlcl.erl.snlp.AnnotationToXMLSerializer;
import uk.ac.susx.mlcl.erl.snlp.EntityLinkingAnnotator;
import uk.ac.susx.mlcl.erl.snlp.XMLToStringSerializer;

/**
 *
 * @author hiam20
 */
public class WebApp {

    private static final Log LOG = LogFactory.getLog(WebApp.class);

    private static final Charset CHARSET = Charset.forName("UTF-8");

    public static void main(String[] args) throws XPathExpressionException, ParsingException, IOException, XSLException {

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
        pool.register("lemma", new MorphaAnnotatorFactory(props));
        pool.register("pos", new POSTaggerAnnotatorFactory(props));
        pool.register("parse", new ParserAnnotatorFactory(props));
        pool.register("ner", new NERAnnotatorFactory(props));
        pool.register("coref", new CorefAnnotatorFactory(props));
        pool.register("el", new EntityLinkingAnnotator.Factory());






//        
//        
//
//        final StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
//       


        AnnotationToXMLSerializer.Builder builder = AnnotationToXMLSerializer.builder();
        builder.addAnnotationToIgnore(CoreAnnotations.XmlContextAnnotation.class);
        builder.addSimplifiedName(EntityLinkingAnnotator.EntityKbIdAnnotation.class, "link");
        final AnnotationToXMLSerializer toXml = builder.build();



        Document stylesheet = new nu.xom.Builder().build(new File(
                "src/main/resources/CoreNLP-to-HTML.xsl"));
        final XSLTransform transform = new XSLTransform(stylesheet);


        Spark.get(new Route("/annotate/tokenise/") {
            @Override
            public Object handle(Request request, Response response) {


                try {
                    String urlString = request.queryParams("url");
                    urlString = URLDecoder.decode(urlString, CHARSET.name());
                    URL url = new URL(urlString);

                    String text = IOUtils.slurpURL(url);
//                    System.out.println(text);
                    Annotation document = new Annotation(text);

                    AnnotationPipeline pipeline = new AnnotationPipeline();
                    pipeline.addAnnotator(pool.get("tokenize"));
                    pipeline.addAnnotator(pool.get("cleanXml"));
                    pipeline.addAnnotator(pool.get("ssplit"));
//                    pipeline.addAnnotator(pool.get("ner"));
//                    pipeline.addAnnotator(pool.get("el"));

                    pipeline.annotate(document);

                    Document xmlDoc = toXml.toDocument(document);
                    Nodes nodes = transform.transform(xmlDoc);

                    NodeFactory nf = new NodeFactory();
                    Document outDoc = nf.startMakingDocument();

                    outDoc.setDocType(new DocType("html"));
                    outDoc.setRootElement((Element) nodes.get(0));

                    nf.finishMakingDocument(outDoc);
                    
                    XMLToStringSerializer sr = new XMLToStringSerializer(
                            response.raw().getOutputStream(), CHARSET.name());
                    sr.setXmlDeclarationSkipped(true);


                    sr.write(outDoc);
                    sr.flush();

                    response.type("text/html");

                    return "";
                } catch (InstantiationException ex) {
                    Logger.getLogger(WebApp.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                } catch (XSLException ex) {
                    LOG.error(ex);
                    halt();
                    return null;
                } catch (MalformedURLException ex) {
                    LOG.warn(ex);
                    response.status(HttpStatus.Bad_Request.code()); // Bad Request
                    return HttpStatus.Bad_Request.toHtmlString(ex.getMessage());
                } catch (UnknownServiceException ex) {
                    LOG.warn(ex);
                    response.status(HttpStatus.Bad_Request.code()); // Bad Request
                    return HttpStatus.Bad_Request.toHtmlString(ex.getMessage());
                } catch (FileNotFoundException ex) {
                    LOG.warn(ex);
                    response.status(HttpStatus.Bad_Request.code()); // Bad Request
                    return HttpStatus.Bad_Request.toHtmlString(
                            "The requested resource could not be found: "+ ex.getMessage());
                } catch (IOException ex) {
                    // UnsupportedEncodingException
                    LOG.error(ex);
                    response.status(HttpStatus.Internal_Server_Error.code());  // Server Error
                    return HttpStatus.Internal_Server_Error.toHtmlString(ex.getMessage());
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

    public static class TokenizerAnnotatorFactory extends AbstractAnnotatorFactory implements Serializable {

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
                String options = props.getProperty("tokenize.options",
                                                   PTBTokenizerAnnotator.DEFAULT_OPTIONS);
                boolean keepNewline = Boolean.valueOf(props.getProperty(NEWLINE_SPLITTER_PROPERTY,
                                                                        "false"));
                // If the user specifies "tokenizeNLs=false" in tokenize.options, then this default will
                // be overridden.
                if (keepNewline) {
                    options = "tokenizeNLs," + options;
                }
                return new PTBTokenizerAnnotator(false, options);
            }
        }
    }

    public static class CleanXmlAnnotatorFactory extends AbstractAnnotatorFactory implements Serializable {

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

    public static class SentenceSplitAnnotatorFactory extends AbstractAnnotatorFactory implements Serializable {

        public static final String NEWLINE_SPLITTER_PROPERTY = "ssplit.eolonly";

        public SentenceSplitAnnotatorFactory(Properties props) {
            super(props);
        }

        private static final long serialVersionUID = 1L;

        public Annotator create() {
            boolean nlSplitting = Boolean.valueOf(props.getProperty(NEWLINE_SPLITTER_PROPERTY,
                                                                    "false"));
            if (nlSplitting) {
                boolean whitespaceTokenization = Boolean.valueOf(props.getProperty(
                        "tokenize.whitespace", "false"));
                WordsToSentencesAnnotator wts;
                if (whitespaceTokenization) {
                    if (System.getProperty("line.separator").equals("\n")) {
                        wts = WordsToSentencesAnnotator.newlineSplitter(false, "\n");
                    } else {
                        // throw "\n" in just in case files use that instead of
                        // the system separator
                        wts = WordsToSentencesAnnotator.newlineSplitter(false, System.getProperty(
                                "line.separator"), "\n");
                    }
                } else {
                    wts = WordsToSentencesAnnotator.newlineSplitter(false, PTBTokenizer
                            .getNewlineToken());
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

//        //
//        // POS tagger
//        //
    public static class POSTaggerAnnotatorFactory extends AbstractAnnotatorFactory implements Serializable {

        private static final long serialVersionUID = 1L;

        public POSTaggerAnnotatorFactory(Properties props) {
            super(props);
        }

        public Annotator create() {
            try {
                String maxLenStr = props.getProperty("pos.maxlen");
                int maxLen = Integer.MAX_VALUE;
                if (maxLenStr != null) {
                    maxLen = Integer.parseInt(maxLenStr);
                }
                return new POSTaggerAnnotator(props.getProperty("pos.model",
                                                                DefaultPaths.DEFAULT_POS_MODEL),
                                              false, maxLen);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class MorphaAnnotatorFactory extends AbstractAnnotatorFactory implements Serializable {

        private static final long serialVersionUID = 1L;

        public MorphaAnnotatorFactory(Properties props) {
            super(props);
        }

        public Annotator create() {
            return new MorphaAnnotator(false);
        }
    }

    public static class NERAnnotatorFactory extends AbstractAnnotatorFactory implements Serializable {

        private static final long serialVersionUID = 1L;

        public NERAnnotatorFactory(Properties props) {
            super(props);
        }

        public Annotator create() {
            List<String> models = new ArrayList<String>();
            List<Pair<String, String>> modelNames = new ArrayList<Pair<String, String>>();
            modelNames.add(new Pair<String, String>("ner.model", null));
            modelNames.add(new Pair<String, String>("ner.model.3class",
                                                    DefaultPaths.DEFAULT_NER_THREECLASS_MODEL));
            modelNames.add(new Pair<String, String>("ner.model.7class",
                                                    DefaultPaths.DEFAULT_NER_MUC_MODEL));
            modelNames.add(new Pair<String, String>("ner.model.MISCclass",
                                                    DefaultPaths.DEFAULT_NER_CONLL_MODEL));

            for (Pair<String, String> name : modelNames) {
                String model = props.getProperty(name.first, name.second);
                if (model != null && model.length() > 0) {
                    models.addAll(Arrays.asList(model.split(",")));
                }
            }
            if (models.isEmpty()) {
                throw new RuntimeException("no NER models specified");
            }
            NERClassifierCombiner nerCombiner;
            try {
                boolean applyNumericClassifiers =
                        PropertiesUtils.getBool(props,
                                                NERClassifierCombiner.APPLY_NUMERIC_CLASSIFIERS_PROPERTY,
                                                NERClassifierCombiner.APPLY_NUMERIC_CLASSIFIERS_DEFAULT);
                boolean useSUTime =
                        PropertiesUtils.getBool(props,
                                                NumberSequenceClassifier.USE_SUTIME_PROPERTY,
                                                NumberSequenceClassifier.USE_SUTIME_DEFAULT);
                nerCombiner = new NERClassifierCombiner(applyNumericClassifiers,
                                                        useSUTime, props,
                                                        models.toArray(new String[models.size()]));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            // ms 2009, no longer needed: the functionality of all these annotators is now included in NERClassifierCombiner
        /*
             * AnnotationPipeline pipeline = new AnnotationPipeline(); pipeline.addAnnotator(new
             * NERCombinerAnnotator(nerCombiner, false)); pipeline.addAnnotator(new
             * NumberAnnotator(false)); pipeline.addAnnotator(new TimeWordAnnotator(false));
             * pipeline.addAnnotator(new QuantifiableEntityNormalizingAnnotator(false, false));
             * return pipeline;
             */
            return new NERCombinerAnnotator(nerCombiner, false);
        }
    }

    public static class RegexNERAnnotatorFactory extends AbstractAnnotatorFactory implements Serializable {

        private static final long serialVersionUID = 1L;

        public RegexNERAnnotatorFactory(Properties props) {
            super(props);
        }

        public Annotator create() {
            String mapping = props.getProperty("regexner.mapping",
                                               DefaultPaths.DEFAULT_REGEXNER_RULES);
            String ignoreCase = props.getProperty("regexner.ignorecase", "false");
            String validPosPattern = props.getProperty("regexner.validpospattern",
                                                       RegexNERSequenceClassifier.DEFAULT_VALID_POS);
            return new RegexNERAnnotator(mapping, Boolean.valueOf(ignoreCase), validPosPattern);
        }
    }

    public static class GenderAnnotatorFactory extends AbstractAnnotatorFactory implements Serializable {

        private static final long serialVersionUID = 1L;

        public GenderAnnotatorFactory(Properties props) {
            super(props);
        }

        public Annotator create() {
            return new GenderAnnotator(false, props.getProperty("gender.firstnames",
                                                                DefaultPaths.DEFAULT_GENDER_FIRST_NAMES));
        }
    }

    public static class TrueCaseAnnotatorFactory extends AbstractAnnotatorFactory implements Serializable {

        private static final long serialVersionUID = 1L;

        public TrueCaseAnnotatorFactory(Properties props) {
            super(props);
        }

        public Annotator create() {
            String model = props.getProperty("truecase.model", DefaultPaths.DEFAULT_TRUECASE_MODEL);
            String bias = props.getProperty("truecase.bias", TrueCaseAnnotator.DEFAULT_MODEL_BIAS);
            String mixed = props.getProperty("truecase.mixedcasefile",
                                             DefaultPaths.DEFAULT_TRUECASE_DISAMBIGUATION_LIST);
            return new TrueCaseAnnotator(model, bias, mixed, false);
        }
    }

    public static class NFLTokenizerAnnotatorFactory extends AbstractAnnotatorFactory implements Serializable {

//
        private static final long serialVersionUID = 1L;

        public NFLTokenizerAnnotatorFactory(Properties props) {
            super(props);
        }

        public Annotator create() {
            final String className =
                    "edu.stanford.nlp.pipeline.NFLTokenizerAnnotator";
            return ReflectionLoading.loadByReflection(className);
        }
    }
//

    public static class NFLAnnotatorFactory extends AbstractAnnotatorFactory implements Serializable {

        private static final long serialVersionUID = 1L;

        public NFLAnnotatorFactory(Properties props) {
            super(props);
        }

        public Annotator create() {
            // these paths now extracted inside c'tor
            // String gazetteer = props.getProperty("nfl.gazetteer", DefaultPaths.DEFAULT_NFL_GAZETTEER);
            // String entityModel = props.getProperty("nfl.entity.model", DefaultPaths.DEFAULT_NFL_ENTITY_MODEL);
            // String relationModel = props.getProperty("nfl.relation.model", DefaultPaths.DEFAULT_NFL_RELATION_MODEL);
            final String className = "edu.stanford.nlp.pipeline.NFLAnnotator";
            return ReflectionLoading.loadByReflection(className, props);
        }
    }

    public static class ParserAnnotatorFactory extends AbstractAnnotatorFactory implements Serializable {

        private static final long serialVersionUID = 1L;

        public ParserAnnotatorFactory(Properties props) {
            super(props);
        }

        public Annotator create() {
            String parserType = props.getProperty("parser.type", "stanford");
            String maxLenStr = props.getProperty("parser.maxlen");

            if (parserType.equalsIgnoreCase("stanford")) {
                ParserAnnotator anno = new ParserAnnotator("parser", props);
                return anno;
            } else if (parserType.equalsIgnoreCase("charniak")) {
                String model = props.getProperty("parser.model");
                String parserExecutable = props.getProperty("parser.executable");
                if (model == null || parserExecutable == null) {
                    throw new RuntimeException(
                            "Both parser.model and parser.executable properties must be specified if parser.type=charniak");
                }
                int maxLen = 399;
                if (maxLenStr != null) {
                    maxLen = Integer.parseInt(maxLenStr);
                }

                CharniakParserAnnotator anno = new CharniakParserAnnotator(model, parserExecutable,
                                                                           false, maxLen);

                return anno;
            } else {
                throw new RuntimeException("Unknown parser type: " + parserType
                        + " (currently supported: stanford and charniak)");
            }
        }
    }

    public static class CorefAnnotatorFactory extends AbstractAnnotatorFactory implements Serializable {

        private static final long serialVersionUID = 1L;

        public CorefAnnotatorFactory(Properties props) {
            super(props);
        }

        public Annotator create() {
            return new DeterministicCorefAnnotator(props);
        }
    }
}
