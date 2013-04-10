/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.webapp;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.common.base.Throwables;
import com.google.common.io.CharStreams;
import com.google.common.net.MediaType;
import org.codehaus.jackson.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;
import uk.ac.susx.mlcl.erl.AnnotationService;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * @author hiam20
 */
public class LinkService extends Route {

    private static final Logger LOG = LoggerFactory.getLogger(LinkService.class);
    private static final boolean DEBUG = true;
    private final JsonFactory jsonFactory = new JacksonFactory();
    private AnnotationService anno;
    private Charset charset = Charset.forName("UTF-8");

    public LinkService(AnnotationService anno, String path) {
        super(path);
        this.anno = anno;
    }

   private String doError(Response response, HttpStatus status, String message) {
        response.type(MediaType.JSON_UTF_8.withoutParameters().toString());
        LinkError error = new LinkError(status.code(), status.message(), message);
        error.setFactory(jsonFactory);
        halt(status.code(),  error.toPrettyString());
        return "";
    }

    @Override
    public Object handle(final Request request, final Response response) {

        // Store the request query, which currently is just a blob of plain text
        final BasicLinkRequest data;

        // Where we get the query from depends on the content-type of the request
        final MediaType mediaType = MediaType.parse(request.contentType());

        final Charset requestCharset = mediaType.charset().isPresent()
                ? mediaType.charset().get()
                : Charset.defaultCharset();

        if (mediaType.withoutParameters().is(MediaType.FORM_DATA.withoutParameters())) {

            // Query data is URL encoded in the body. This is handled by spark
            data = new BasicLinkRequest(request.queryParams("text"));

        } else if (mediaType.withoutParameters().is(MediaType.JSON_UTF_8.withoutParameters())) {

            // Query data is a json object
            LOG.debug("Decoding JSON body: {}", request.body());
            final JsonObjectParser oParser = jsonFactory.createJsonObjectParser();
            try {
                Reader r = CharStreams.asCharSource(request.body()).openStream();
                data = oParser.parseAndClose(r, BasicLinkRequest.class);
            } catch (JsonParseException e) {
                return doError(response, HttpStatus.Bad_Request,
                        "Failed to parse JSON payload: " + e.getLocalizedMessage());
            } catch (IOException impossible) {
                throw new AssertionError(impossible);
            }

        } else {
            return doError(response, HttpStatus.Bad_Request,
                    "Unknown request content type:" + request.contentType());
        }

        if (data.getText() == null) {
            return doError(response, HttpStatus.Bad_Request,
                    "The \"text\" query parameter was not provided.");
        } else {

            try {
                response.type("application/json");
                anno.linkAsJson(data.getText(), response.raw().getOutputStream(), charset);
                return "";
            } catch (Throwable ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
                final String message = ex.getLocalizedMessage()
                        + System.getProperty("line.separator")
                        + Throwables.getStackTraceAsString(ex);
                return doError(response, HttpStatus.Internal_Server_Error, message);
            }
        }
    }

}
