/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.webapp;

import com.google.common.base.Throwables;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.AnnotatorPool;
import eu.ac.susx.mlcl.xml.AnnotationToXML;
import eu.ac.susx.mlcl.xml.XMLToStringSerializer;
import eu.ac.susx.mlcl.xml.XomB;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownServiceException;
import java.nio.charset.Charset;
import java.util.Properties;
import javax.xml.xpath.XPathExpressionException;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.xslt.XSLException;
import nu.xom.xslt.XSLTransform;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import uk.ac.susx.mlcl.erl.snlp.CleanXmlAnnotator2;
import uk.ac.susx.mlcl.erl.snlp.CorefAnnotatorFactory;
import uk.ac.susx.mlcl.erl.snlp.EntityLinkingAnnotator;
import uk.ac.susx.mlcl.erl.snlp.MorphaAnnotatorFactory;
import uk.ac.susx.mlcl.erl.snlp.NERAnnotatorFactory;
import uk.ac.susx.mlcl.erl.snlp.POSTaggerAnnotatorFactory;
import uk.ac.susx.mlcl.erl.snlp.ParserAnnotatorFactory;
import uk.ac.susx.mlcl.erl.snlp.SentenceSplitAnnotatorFactory;
import uk.ac.susx.mlcl.erl.snlp.TokenizerAnnotatorFactory;

/**
 *
 * @author hiam20
 */
public class WebApp {

    private static final Logger LOG = LoggerFactory.getLogger(WebApp.class);
    private static final Charset CHARSET = Charset.forName("UTF-8");
    private static final XomB X = new XomB();
    private static final AnnotationToXML xmler = newAnnotationToXML();
    ;
    /*
     * 
     */
    private AnnotatorPool pool = null;

    public void WebApp() {
    }

    static AnnotationToXML newAnnotationToXML()  {
        try {
            AnnotationToXML.Builder builder = AnnotationToXML.builder();
//            builder.configure(new PropertiesConfiguration("stanfordXml.properties"));
//            builder.addAnnotationToIgnore(
//                    CoreAnnotations.XmlContextAnnotation.class);
//            builder.addSimplifiedName(
//                    EntityLinkingAnnotator.EntityKbIdAnnotation.class, "link");
            return builder.build();
        } catch (Throwable ex) {
           Throwables.propagateIfPossible(ex);
           throw new RuntimeException(ex);
        }
    }

    private enum ResponseType {

        XML("text/xml") {
            @Override
            public void doResponse(Annotation document, Response response) throws Exception {
                final Document xmlDoc = toXML(document);
                writeXml(xmlDoc, response.raw().getOutputStream());
                response.type(mimeType);
            }
        },
        HTML("text/html") {
            @Override
            public void doResponse(Annotation document, Response response) throws Exception {

                final Document xmlDoc = toXML(document);
                final Document htmlDoc = toHTML(xmlDoc);
                writeHtml(htmlDoc, response.raw().getOutputStream());

                response.type(mimeType);
            }
        };
        String mimeType;

        private ResponseType(String mimeType) {
            this.mimeType = mimeType;
        }

        public abstract void doResponse(Annotation document, Response response)
                throws Exception;
    }

    void init(Properties props) throws ParsingException, IOException, XSLException {

        {
            pool = new AnnotatorPool();
            pool.register("tokenize", new TokenizerAnnotatorFactory(props));
            pool.register("cleanXml", new CleanXmlAnnotator2.Factory(props));
            pool.register("ssplit", new SentenceSplitAnnotatorFactory(props));
            pool.register("lemma", new MorphaAnnotatorFactory(props));
            pool.register("pos", new POSTaggerAnnotatorFactory(props));
            pool.register("parse", new ParserAnnotatorFactory(props));
            pool.register("ner", new NERAnnotatorFactory(props));
            pool.register("coref", new CorefAnnotatorFactory(props));
            pool.register("el", new EntityLinkingAnnotator.Factory());
        }


        Spark.get(new Route("/annotate/tokenise/") {
            @Override
            public Object handle(Request request, Response response) {


                try {
                    final URL url = new URL(URLDecoder.decode(
                            request.queryParams("url"), CHARSET.name()));

                    final ResponseType type;
                    {
                        String typeStr = request.queryParams("type");
                        type = typeStr == null
                                ? ResponseType.XML
                                : ResponseType.valueOf(typeStr.toUpperCase());
                    }


                    final String text = IOUtils.slurpURL(url);

                    final Annotation document = new Annotation(text);
                    document.set(CoreAnnotations.DocIDAnnotation.class, url.toString());

                    createPipeline().annotate(document);

                    type.doResponse(document, response);

                    return "";
                } catch (InstantiationException ex) {
                    LOG.error(ex.toString());
                    LOG.error(Throwables.getStackTraceAsString(ex));
                    halt();
                    return null;
                } catch (XSLException ex) {
                    LOG.error(ex.toString());
                    LOG.error(Throwables.getStackTraceAsString(ex));
                    halt();
                    return null;
                } catch (MalformedURLException ex) {
                    LOG.warn(ex.toString());
                    LOG.warn(Throwables.getStackTraceAsString(ex));
                    response.status(HttpStatus.Bad_Request.code()); // Bad Request
                    return HttpStatus.Bad_Request.toHtmlString(ex.getMessage());

                } catch (UnknownServiceException ex) {
                    LOG.warn(ex.toString());
                    LOG.warn(Throwables.getStackTraceAsString(ex));
                    response.status(HttpStatus.Bad_Request.code()); // Bad Request
                    return HttpStatus.Bad_Request.toHtmlString(ex.getMessage());
                } catch (FileNotFoundException ex) {

                    LOG.warn(ex.toString());
                    LOG.warn(Throwables.getStackTraceAsString(ex));
                    response.status(HttpStatus.Bad_Request.code()); // Bad Request
                    return HttpStatus.Bad_Request.toHtmlString(
                            "The requested resource could not be found: " + ex.getMessage());
                } catch (IOException ex) {
                    // UnsupportedEncodingException
                    LOG.error(ex.toString());
                    response.status(HttpStatus.Internal_Server_Error.code());  // Server Error
                    return HttpStatus.Internal_Server_Error.toHtmlString(ex.getMessage());

                } catch (Throwable throwable) {
                    LOG.error(throwable.toString());
                    LOG.error(Throwables.getStackTraceAsString(throwable));
                    halt();
                    return null;
                }
            }
        });

    }

    AnnotationPipeline createPipeline() {

        AnnotationPipeline pipeline = new AnnotationPipeline();
        pipeline.addAnnotator(pool.get("tokenize"));
//        pipeline.addAnnotator(pool.get("cleanXml"));
        pipeline.addAnnotator(pool.get("ssplit"));
        pipeline.addAnnotator(pool.get("ner"));
        pipeline.addAnnotator(pool.get("el"));
        return pipeline;

    }

    static Document toHTML(Document xmlDoc) throws UnsupportedEncodingException, IOException, XSLException, ParsingException {

        XSLTransform transform = new XSLTransform(new nu.xom.Builder().build(new File(
                "src/main/resources/CoreNLP-to-HTML_2.xsl")));

        Nodes nodes = transform.transform(xmlDoc);
        Document htmlDoc = X.document().setDocType("html")
                .setRoot((Element) nodes.get(0)).build();
        return htmlDoc;

    }

    static Document toXML(Annotation document) throws InstantiationException {

        return xmler.toDocument(document);
    }

    static void writeHtml(Document document, OutputStream out) throws IOException {
        XMLToStringSerializer sr = new XMLToStringSerializer(
                out, CHARSET.name());
        sr.setXmlDeclarationSkipped(true);
        sr.write(document);
        sr.setIndent(2);
        sr.flush();
    }

    static void writeXml(Document document, OutputStream out) throws IOException {
        XMLToStringSerializer sr = new XMLToStringSerializer(
                out, CHARSET.name());
        sr.setXmlDeclarationSkipped(false);
        sr.setIndent(2);
        sr.write(document);
        sr.flush();
    }

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


        props.put("clean.allowflawedxml", "true");


        WebApp webapp = new WebApp();
        webapp.init(props);





    }
}
