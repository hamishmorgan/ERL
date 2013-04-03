/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.webapp;

import com.google.api.client.json.*;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.common.base.Throwables;
import com.google.common.io.CharStreams;
import com.google.common.net.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;
import uk.ac.susx.mlcl.erl.AnnotationService;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;

/**
 *
 * @author hiam20
 */
public class LinkService extends Route {

    private static final Logger LOG = LoggerFactory.getLogger(StaticResource.class);
    private static final boolean DEBUG = true;
    private AnnotationService anno;
    private Charset charset = Charset.forName("UTF-8");
    private final JsonFactory jsonFactory = new JacksonFactory();

    public LinkService(AnnotationService anno, String path) {
        super(path);
        this.anno = anno;
    }

    public static class RequestData {

        public String text = null;

        public RequestData() {
        }

        public RequestData(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    @Override
    public Object handle(final Request request, final Response response) {

        // Store the request query, which currently is just a blob of plain text
        final RequestData data;

        // Where we get the query from depends on the content-type of the request
        final MediaType mediaType = MediaType.parse(request.contentType()).withoutParameters();
        if(mediaType.is(MediaType.FORM_DATA.withoutParameters())) {
            // Query data is URL encoded in the body. This is handled by spark
            data = new RequestData(request.queryParams("text"));
        } else if(mediaType.is(MediaType.JSON_UTF_8.withoutParameters())) {
            // Query data is a json object
            JsonParser jsonParser = null;
            try {
                JsonObjectParser oParser = jsonFactory.createJsonObjectParser();
                Reader r = CharStreams.asCharSource(request.body()).openStream();
                data = oParser.parseAndClose(r, RequestData.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalArgumentException("Unknown request content type:" + request.contentType());
        }

        response.type("application/json");

        if (data.getText() == null) {
            halt(HttpStatus.Bad_Request.code(),
                 buildJsonError(HttpStatus.Bad_Request,
                                "The \"text\" query parameter was not provided."));
            return "";
        } else {

            try {
                anno.linkAsJson(data.getText(), response.raw().getOutputStream(), charset);
                return "";
            } catch (Throwable ex) {
                LOG.error(ex.getLocalizedMessage(), ex);

                final String message = ex.getLocalizedMessage()
                        + System.getProperty("line.separator")
                        + Throwables.getStackTraceAsString(ex);

                halt(HttpStatus.Internal_Server_Error.code(),
                     buildJsonError(HttpStatus.Internal_Server_Error,
                                    DEBUG ? message : ""));

            }
        }
        return "";
    }

    private static String buildJsonError(HttpStatus status, String message) {
        return buildJsonError(status.code(), status.message(), message);
    }

    private static String buildJsonError(int code, String name, String message) {
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
}
