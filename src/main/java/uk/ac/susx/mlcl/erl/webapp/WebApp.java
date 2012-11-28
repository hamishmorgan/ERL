/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.webapp;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.jackson.JacksonFactory;
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
import java.util.Set;
import javax.activation.MimetypesFileTypeMap;
import nu.xom.ParsingException;
import nu.xom.xslt.XSLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Filter;
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

    void init(Properties props) throws ParsingException, IOException, XSLException {

        Spark.before(new RequestLogger("/*"));

        Spark.post(new AbstractRoute("/annotate/link/") {
            @Override
            public Object handle(final Request request, final Response response) {

                for (String queryKey : request.queryParams()) {
                    LOG.debug(queryKey + " => " + request.queryParams(queryKey));
                }
                try {
                    response.type("application/json");
//	  response.type("text/plain");

                    final String text = request.queryParams("text");


                    anno.linkAsJson(text, response.raw().getOutputStream());

                    return "";

                } catch (Exception ex) {
                    LOG.error(ex.toString() + "\n" + Throwables.getStackTraceAsString(ex));
                    Throwables.propagateIfInstanceOf(ex, RuntimeException.class);
                    handleException(ex, response);
                    halt();
                    return "";
                }
            }
        });

        Spark.get(new StaticRoute("/*", "src/main/resources/"));

    }

    public static class StaticRoute extends AbstractRoute {

        private final File localPath;
        static final MimetypesFileTypeMap mimeMap;

        static {
            try {
                mimeMap = new MimetypesFileTypeMap(
                        "src/main/resources/mime.types");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        public StaticRoute(String remotePath, String localPath) {
            super(remotePath);
            this.localPath = new File(localPath).getAbsoluteFile();

        }

        @Override
        public String handle(Request request, Response response) {
            try {

                final File file = new File(localPath, request.pathInfo());

                if (!file.exists()) {
                    final String msg = "Could not find static resource: "
                            + request.pathInfo();
                    halt(HttpStatus.Not_Found.code(),
                         HttpStatus.Not_Found.toHtmlString(msg));
                    return "";
                } else if (!file.canRead() || file.isHidden()) {
                    halt(HttpStatus.Forbidden.code(),
                         HttpStatus.Forbidden.toHtmlString(""));
                    return "";
                } else if (!file.isFile()) {
                    halt(HttpStatus.Bad_Request.code(),
                         HttpStatus.Bad_Request.toHtmlString(
                            "The resquested static resource is not a file."));
                    return "";
                }

                final String mime = mimeMap.getContentType(file.getName());

                LOG.info("Serving " + mime + ": " + file);
                response.raw().setContentType(mime + ";charset=utf-8");
                response.status(HttpStatus.OK.code());

                InputStream in = null;
                try {
                    in = new FileInputStream(file);
                    OutputStream out = response.raw().getOutputStream();
                    ByteStreams.copy(in, out);
                } finally {
                    Closeables.closeQuietly(in);
                }

                return "";

            } catch (Exception ex) {
                LOG.error(ex.toString() + "\n" + Throwables.getStackTraceAsString(ex));
                Throwables.propagateIfInstanceOf(ex, RuntimeException.class);
                handleException(ex, response);
                return "";
            }
        }
    }

    public static abstract class AbstractRoute extends Route {

        public AbstractRoute(String path) {
            super(path);
        }

        protected void handleException(Throwable ex, Response response) {
            LOG.error(ex.toString() + "\n" + Throwables.getStackTraceAsString(ex));

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
//	  LOG.error(ex.toString() + "\n" + Throwables.getStackTraceAsString(ex));
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

    private static class RequestLogger extends Filter {

        private static final Logger LOG = LoggerFactory.getLogger(RequestLogger.class);
        private final JsonFactory factory = new JacksonFactory();
        private boolean logRequest = true;

        public RequestLogger() {
        }

        public RequestLogger(String path) {
            super(path);
        }

        void writeArray(Iterable<String> items, JsonGenerator generator) throws IOException {
            generator.writeStartArray();
            for (String item : items) {
                generator.writeString(item);
            }
            generator.writeEndArray();
        }

        void writeKeyValue(String key, String value, JsonGenerator generator) throws IOException {
            generator.writeFieldName(key);
            generator.writeString(value);
        }

        @Override
        public void handle(Request request, Response response) {

            StringWriter writer = new StringWriter();
            try {
                JsonGenerator generator = factory.createJsonGenerator(writer);
                generator.enablePrettyPrint();


                generator.writeStartObject();

                if (logRequest) {
                    generator.writeFieldName("request");
                    generator.writeStartObject();

                    writeKeyValue("url", request.url(), generator);

                    generator.writeFieldName("attributes");
                    generator.writeStartObject();
                    for (String attribute : request.attributes()) {
                        generator.writeFieldName(attribute);
                        generator.writeString(request.attribute(attribute).toString());
                    }
                    generator.writeEndObject();;

                    generator.writeFieldName("headers");
                    generator.writeStartObject();
                    for (String header : request.headers()) {
                        generator.writeFieldName(header);
                        generator.writeString(request.headers(header));
                    }
                    generator.writeEndObject();;

                    writeKeyValue("body", request.body(), generator);
                    writeKeyValue("contentType", request.contentType(), generator);
                    writeKeyValue("requestMethod", request.requestMethod(), generator);


                    generator.writeFieldName("queryParams");
                    generator.writeStartObject();
                    for (String queryParam : request.queryParams()) {
                        generator.writeFieldName(queryParam);
                        generator.writeString(request.queryParams(queryParam));
                    }
                    generator.writeEndObject();;

                    writeKeyValue("queryString", request.queryString(), generator);
                    writeKeyValue("userAgent", request.userAgent(), generator);

                    generator.writeEndObject();;
                }

                generator.writeEndObject();
                generator.flush();

                LOG.debug(writer.toString());

            } catch (IOException ex) {
                throw new AssertionError(ex);
            }


        }
    }
}
