/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.webapp;

import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.logging.Level;
import javax.activation.MimetypesFileTypeMap;
import nu.xom.ParsingException;
import nu.xom.xslt.XSLException;
import org.eclipse.jetty.http.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import uk.ac.susx.mlcl.erl.AnnotationService;

/**
 *
 * @author hiam20
 */
public class WebApp {

  private static final boolean DEBUG = true;

  private static final Logger LOG = LoggerFactory.getLogger(WebApp.class);

  private final AnnotationService anno;

  public WebApp(AnnotationService anno) {
    this.anno = anno;
  }

  void init(Properties props)
	  throws ParsingException, IOException, XSLException {

    Spark.post(new LinkService("/annotate/link/"));

    Spark.get(new StaticResource("/static/", "src/main/resources/", "*"));

    Spark.after(new RequestLogger());

  }

  public class LinkService extends AbstractRoute {

    public LinkService(String path) {
      super(path);
    }

    String buildJsonError(HttpStatus status, String message) {
      return buildJsonError(status.code(), status.message(), message);
    }

    String buildJsonError(int code, String name, String message) {
      try {
	StringWriter writer = new StringWriter();
	JsonGenerator g = new JacksonFactory().createJsonGenerator(writer);

	g.writeStartObject();

	g.writeFieldName("code");
	g.writeNumber(code);

	g.writeFieldName("name");
	g.writeString(name);

	g.writeFieldName("message");
	g.writeString(message);


	g.writeEndObject();
	g.flush();
	g.close();

	return writer.toString();

      } catch (IOException ex) {
	throw new AssertionError(ex);
      }

    }

    @Override
    public Object handle(final Request request,
			 final Response response) {

      for (String queryKey : request.queryParams()) {
	LOG.debug(queryKey + " => " + request.queryParams(queryKey));
      }
      try {
	final String text = request.queryParams("text");

	response.type("application/json");

	if (text == null) {


	  halt(HttpStatus.Bad_Request.code(),
	       buildJsonError(HttpStatus.Bad_Request,
			      "The \"text\" query parameter was not provided."));
	}

//	  response.type("text/plain");



	anno.linkAsJson(text, response.raw().getOutputStream());

	return "";

      } catch (Exception ex) {
	LOG.error(ex.getLocalizedMessage(), ex);
	Throwables.propagateIfInstanceOf(ex, RuntimeException.class);
	handleException(ex, response);
	halt();
	return "";
      }
    }
  }

  /**
   * A route which serves static resource from the file directory structure. The
   * request path is mapped onto a local path, and if that path matches a file
   * it is sent to the client.
   *
   * A number of checks are made to insure system security. In particlar a file
   * will only be served if it is not hidden, if it is a normal file, and if the
   * file is direct descendent of the mapped local path. Paths are converted to
   * canonical form, so symbolic links are deliberately not support.
   *
   */
  public static class StaticResource extends AbstractRoute {

    private final File localRoot;

    private final String remoteRoot;

    private static final MimetypesFileTypeMap mimeMap;

    static {
      try {
	mimeMap = new MimetypesFileTypeMap(
		"src/main/resources/mime.types");
      } catch (IOException ex) {
	throw new RuntimeException(ex);
      }
    }

    public StaticResource(String remoteRoot, String localPath, String path)
	    throws IOException {
      super(remoteRoot + "/" + path);
      this.localRoot = new File(localPath).getCanonicalFile();
      this.remoteRoot = remoteRoot;
    }

    @Override
    public String handle(Request request, Response response) {
      try {

	String resourcePath = request.pathInfo().substring(remoteRoot.length());
	final File requestedFile = new File(localRoot, resourcePath)
		.getCanonicalFile();

	// Perform basic file access checks
	if (!requestedFile.exists()) {
	  final String msg = "Could not find static resource: "
		  + request.pathInfo();
	  halt(HttpStatus.Not_Found.code(),
	       HttpStatus.Not_Found.toHtmlString(msg));
	  return "";
	} else if (!requestedFile.canRead() || requestedFile.isHidden()) {
	  halt(HttpStatus.Forbidden.code(),
	       HttpStatus.Forbidden.toHtmlString(""));
	  return "";
	} else if (!requestedFile.isFile()) {
	  halt(HttpStatus.Bad_Request.code(),
	       HttpStatus.Bad_Request.toHtmlString(
		  "The resquested static resource is not a file."));
	  return "";
	}

	// Insure that the requested resource is a direct descendent of the
	// local path (i.e local path is an ancestoral directory.)
	if (!isParentOf(localRoot, requestedFile)) {
	  halt(HttpStatus.Forbidden.code(),
	       HttpStatus.Forbidden.toHtmlString(""));
	  return "";
	}

	final String mime = mimeMap.getContentType(requestedFile.getName());
	LOG.debug("Serving " + mime + ": " + requestedFile);
	response.raw().setContentType(mime);
	response.raw().setContentLength((int) requestedFile.length());
	response.raw().setDateHeader("Last-Modified",
				     requestedFile.lastModified());
	response.status(HttpStatus.OK.code());

	InputStream in = null;
	try {
	  in = new FileInputStream(requestedFile);
	  OutputStream out = response.raw().getOutputStream();
	  ByteStreams.copy(in, out);
	  out.flush();
	} finally {
	  Closeables.closeQuietly(in);
	}

	return null;

      } catch (Exception ex) {
	LOG.error(ex.toString() + "\n"
		+ Throwables.getStackTraceAsString(ex));
	Throwables.propagateIfInstanceOf(ex, RuntimeException.class);
	handleException(ex, response);
	return "";
      }
    }

    boolean isParentOf(final File ancestor, final File descendence)
	    throws IOException {
      Preconditions.checkNotNull(ancestor, "ancestor");
      Preconditions.checkNotNull(descendence, "descendence");

      final File cAncestor = ancestor.getCanonicalFile();
      File file = descendence.getCanonicalFile();

      while (file != null && !file.equals(cAncestor)) {
	file = file.getParentFile();
      }
      return file != null;
    }
  }

  public static abstract class AbstractRoute extends Route {

    public AbstractRoute(String path) {
      super(path);
    }

    protected void handleException(Throwable ex, Response response) {
      LOG.error(ex.toString() + System.getProperty("line.separator")
	      + Throwables.getStackTraceAsString(ex));

      MessageFormat frmt = new MessageFormat(
	      "<strong>{0}</strong><br/><pre>{1}</pre>");

      final String message = DEBUG ? frmt.format(new Object[]{
		ex.toString(),
		Throwables.getStackTraceAsString(ex)})
	      : "";

      response.status(HttpStatus.Internal_Server_Error.code());
      response.body(HttpStatus.Internal_Server_Error.toHtmlString(message));
      halt();
    }
  }
//
//private static Route newStaticRoute(final String remotePathString,
//				      final String localPathStirng)
//	  throws IOException {
//
//    final MimetypesFileTypeMap mimeMap = new MimetypesFileTypeMap(
//	    "src/main/resources/mime.types");
//
//    final File localPath = new File(localPathStirng).getAbsoluteFile();
//
//
//    return new Route(remotePathString) {
//      @Override
//	public String handle(Request request, Response response) {
//	try {
//
//	  final File file = new File(localPath, request.pathInfo());
//
//	  if (!file.exists()) {
//	    final String msg = "Could not find static resource: "
//		    + request.pathInfo();
//	    halt(HttpStatus.Not_Found.code(),
//		 HttpStatus.Not_Found.toHtmlString(msg));
//	    return null;
//	  } else if (!file.canRead() || file.isHidden()) {
//	    halt(HttpStatus.Forbidden.code(),
//		 HttpStatus.Forbidden.toHtmlString(""));
//	    return null;
//	  } else if (!file.isFile()) {
//	    halt(HttpStatus.Bad_Request.code(),
//		 HttpStatus.Bad_Request.toHtmlString(
//		    "The resquested static resource is not a file."));
//	    return null;
//	  }
//
//	  final String mime = mimeMap.getContentType(file.getName());
//
//	  LOG.info("Serving " + mime + ": " + file);
//	  response.raw().setContentType(mime + ";charset=utf-8");
//
//	  InputStream in = null;
//	  try {
//	    in = new FileInputStream(file);
//	    OutputStream out = response.raw().getOutputStream();
//	    ByteStreams.copy(in, out);
//	  } finally {
//	    Closeables.closeQuietly(in);
//	  }
//
//	  response.status(HttpStatus.OK.code());
//	  return null;
//
//	} catch (Throwable ex) {
//	  LOG.error(ex.getLocalizedMessage(), ex);
//	  handleException(ex, response);
//	  halt();
//	  return null;
//	}
//      }
//    };
//
//  }

//
//  AnnotationPipeline createPipeline() {
//
//    AnnotationPipeline pipeline = new AnnotationPipeline();
//    pipeline.addAnnotator(pool.get("tokenize"));
//    pipeline.addAnnotator(pool.get("cleanXml"));
//    pipeline.addAnnotator(pool.get("ssplit"));
//    pipeline.addAnnotator(pool.get("ner"));
//    pipeline.addAnnotator(pool.get("el"));
//    return pipeline;
//
//  }
//
//  static Document toHTML(Document xmlDoc) throws UnsupportedEncodingException, IOException, XSLException, ParsingException {
//
//    XSLTransform transform = new XSLTransform(new nu.xom.Builder()
//	    .build(new File(
//	    "src/main/resources/CoreNLP-to-HTML_2.xsl")));
//
//    Nodes nodes = transform.transform(xmlDoc);
//    Document htmlDoc = X.document().setDocType("html")
//	    .setRoot((Element) nodes.get(0)).build();
//
//    if (DEBUG)
//      dumpQuietly(htmlDoc, new File("debug.html"));
//
//    return htmlDoc;
//
//  }
//
//  static Document toXML(Annotation document) throws InstantiationException {
//    final Document doc = xmler.toDocument(document);
//
//    if (DEBUG)
//      dumpQuietly(doc, new File("debug.xml"));
//
//    return doc;
//  }
//
//  static void dumpQuietly(Document document, File dest) {
//    OutputStream out = null;
//    try {
//      out = new BufferedOutputStream(new FileOutputStream(dest));
//      writeXml(document, out);
//    } catch (IOException ex) {
//      LOG.error(ex.toString());
//    } finally {
//      Closeables.closeQuietly(out);
//    }
//  }
//
//  static void writeHtml(Document document, OutputStream out) throws IOException {
//    XMLToStringSerializer sr = new XMLToStringSerializer(
//	    out, CHARSET.name());
//    sr.setXmlDeclarationSkipped(true);
//    sr.write(document);
//    sr.setIndent(2);
//    sr.flush();
//  }
//
//  static void writeXml(Document document, OutputStream out) throws IOException {
//    XMLToStringSerializer sr = new XMLToStringSerializer(
//	    out, CHARSET.name());
//    sr.setXmlDeclarationSkipped(false);
//    sr.setIndent(2);
//    sr.write(document);
//    sr.flush();
//  }
  public static void main(String[] args) throws Exception {

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


    AnnotationService anno = AnnotationService.newInstance(props);

    WebApp webapp = new WebApp(anno);
    webapp.init(props);





  }
}
