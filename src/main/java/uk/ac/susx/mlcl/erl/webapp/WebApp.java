/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.webapp;

import uk.ac.susx.mlcl.erl.snlp.SentenceSplitAnnotatorFactory;
import uk.ac.susx.mlcl.erl.snlp.POSTaggerAnnotatorFactory;
import uk.ac.susx.mlcl.erl.snlp.NERAnnotatorFactory;
import uk.ac.susx.mlcl.erl.snlp.MorphaAnnotatorFactory;
import uk.ac.susx.mlcl.erl.snlp.CleanXmlAnnotatorFactory;
import uk.ac.susx.mlcl.erl.snlp.ParserAnnotatorFactory;
import uk.ac.susx.mlcl.erl.snlp.TokenizerAnnotatorFactory;
import uk.ac.susx.mlcl.erl.snlp.CorefAnnotatorFactory;
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
import eu.ac.susx.mlcl.xml.AnnotationToXML;
import uk.ac.susx.mlcl.erl.snlp.EntityLinkingAnnotator;
import eu.ac.susx.mlcl.xml.XMLToStringSerializer;

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


        AnnotationToXML.Builder builder = AnnotationToXML.builder();
        builder.addAnnotationToIgnore(CoreAnnotations.XmlContextAnnotation.class);
        builder.addSimplifiedName(EntityLinkingAnnotator.EntityKbIdAnnotation.class, "link");
        final AnnotationToXML toXml = builder.build();



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
}
