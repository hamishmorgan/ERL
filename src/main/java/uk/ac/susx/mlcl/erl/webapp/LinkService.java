/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.webapp;

import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.common.base.Throwables;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;
import uk.ac.susx.mlcl.erl.AnnotationService;

/**
 *
 * @author hiam20
 */
public class LinkService extends Route {

    private static final Logger LOG = LoggerFactory.getLogger(StaticResource.class);
    private static final boolean DEBUG = true;
    private AnnotationService anno;
    private Charset charset = Charset.forName("UTF-8");

    public LinkService(AnnotationService anno, String path) {
        super(path);
        this.anno = anno;
    }

    @Override
    public Object handle(final Request request, final Response response) {

        final String text = request.queryParams("text");
        response.type("application/json");

        if (text == null) {
            halt(HttpStatus.Bad_Request.code(),
                 buildJsonError(HttpStatus.Bad_Request,
                                "The \"text\" query parameter was not provided."));
            return "";
        } else {

            try {
                anno.linkAsJson(text, response.raw().getOutputStream(), charset);
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
