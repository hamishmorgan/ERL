/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.webapp;


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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownServiceException;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.logging.Level;
import javax.xml.xpath.XPathExpressionException;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.xslt.XSLException;
import nu.xom.xslt.XSLTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import uk.ac.susx.mlcl.erl.snlp.CleanXmlAnnotatorFactory;
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
	builder
		.addAnnotationToIgnore(
		CoreAnnotations.XmlContextAnnotation.class);
	builder.addSimplifiedName(
		EntityLinkingAnnotator.EntityKbIdAnnotation.class, "link");
	final AnnotationToXML xmler = builder.build();



	Document stylesheet = new nu.xom.Builder().build(new File(
		"src/main/resources/CoreNLP-to-HTML.xsl"));
	final XSLTransform transform = new XSLTransform(stylesheet);

	final XomB X = new XomB();

	Spark.get(new Route("/annotate/tokenise/") {
	    @Override
	    public Object handle(Request request, Response response) {


		try {
		    String urlString = request.queryParams("url");
		    urlString = URLDecoder.decode(urlString, CHARSET.name());

		    final URL url = new URL(urlString);
		    final String id = url.toString();

		    String text = IOUtils.slurpURL(url);

		    Annotation document = new Annotation(text);
		    document.set(CoreAnnotations.DocIDAnnotation.class, id);

		    AnnotationPipeline pipeline = new AnnotationPipeline();
		    pipeline.addAnnotator(pool.get("tokenize"));
		    pipeline.addAnnotator(pool.get("cleanXml"));
		    pipeline.addAnnotator(pool.get("ssplit"));
                    pipeline.addAnnotator(pool.get("ner"));
                    pipeline.addAnnotator(pool.get("el"));

		    pipeline.annotate(document);

		    Document xmlDoc = xmler.toDocument(document);
		    Nodes nodes = transform.transform(xmlDoc);

		    Document htmlDoc = X.document().setDocType("html")
			    .setRoot((Element) nodes.get(0)).build();

		    XMLToStringSerializer sr = new XMLToStringSerializer(
			    response.raw().getOutputStream(), CHARSET.name());
		    sr.setXmlDeclarationSkipped(true);
		    sr.write(htmlDoc);
		    sr.flush();

		    response.type("text/html");

		    return "";
		} catch (InstantiationException ex) {
		    LOG.error(ex.getLocalizedMessage());
		    return null;
		} catch (XSLException ex) {
		    LOG.error(ex.getLocalizedMessage());
		    halt();
		    return null;
		} catch (MalformedURLException ex) {
		    LOG.warn(ex.getLocalizedMessage());
		    response.status(HttpStatus.Bad_Request.code()); // Bad Request
		    return HttpStatus.Bad_Request.toHtmlString(ex.getMessage());
		} catch (UnknownServiceException ex) {
		    LOG.warn(ex.getLocalizedMessage());
		    response.status(HttpStatus.Bad_Request.code()); // Bad Request
		    return HttpStatus.Bad_Request.toHtmlString(ex.getMessage());
		} catch (FileNotFoundException ex) {
		    LOG.warn(ex.getLocalizedMessage());
		    response.status(HttpStatus.Bad_Request.code()); // Bad Request
		    return HttpStatus.Bad_Request.toHtmlString(
			    "The requested resource could not be found: " + ex
			    .getMessage());
		} catch (IOException ex) {
		    // UnsupportedEncodingException
		    LOG.error(ex.getLocalizedMessage());
		    response.status(HttpStatus.Internal_Server_Error.code());  // Server Error
		    return HttpStatus.Internal_Server_Error.toHtmlString(ex
			    .getMessage());
		} catch(Throwable throwable) {
		    LOG.error(throwable.getMessage());
		    halt();
		    return null;
		}
	    }
	});

    }
}
