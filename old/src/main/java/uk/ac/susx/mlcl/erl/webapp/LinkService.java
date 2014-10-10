/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.webapp;

import com.google.api.client.json.GenericJson;
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
import uk.ac.susx.mlcl.erl.AnnotationServiceImpl2;

import javax.annotation.Nonnull;
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
    private final AnnotationServiceImpl2 anno;
    private Charset charset = Charset.forName("UTF-8");

    public LinkService(AnnotationServiceImpl2 anno, String path) {
        super(path);
        this.anno = anno;
    }

    @Nonnull
    @Override
    public Object handle(@Nonnull final Request request, @Nonnull final Response response) {

        final MediaType mediaType = MediaType.parse(request.contentType());

        final Charset requestCharset = mediaType.charset().isPresent()
                ? mediaType.charset().get()
                : charset;

        if (mediaType.withoutParameters().is(MediaType.FORM_DATA.withoutParameters())) {

            // Query data is URL encoded in the body. This is handled by spark
            final SimpleLinkRequest data = new SimpleLinkRequest(request.queryParams("text"));
            return doLink(data, response);

        } else if (mediaType.withoutParameters().is(MediaType.JSON_UTF_8.withoutParameters())) {

            // Query data is a json object
            LOG.debug("Decoding JSON body: {}", request.body());

            try {
                // First try parsing body as a simple text request
                try {
                    final SimpleLinkRequest data = parseJson(request.body(), SimpleLinkRequest.class);
                    return doLink(data, response);
                } catch (JsonObjectParseException e1) {
                    // If parsing failed then we shall attempt the other LinkRequest type (bellow)
                }

                // Try with the more complex LinkRequest
                try {
                    final LinkRequest data = parseJson(request.body(), LinkRequest.class);
                    return doLink(data, response);
                } catch (JsonObjectParseException e2) {
                    // If parsing failed again then there's nothing else we can do
                    return doError(response, HttpStatus.Bad_Request,
                            "Failed to parse JSON payload: " + e2.getLocalizedMessage());
                }

            } catch (JsonParseException e2) {
                // If parsing failed again then there's nothing else we can do
                return doError(response, HttpStatus.Bad_Request,
                        "Failed to parse JSON payload: " + e2.getLocalizedMessage());
            }

        } else {
            return doError(response, HttpStatus.Bad_Request,
                    "Unknown request content type:" + request.contentType());
        }

    }

    @Nonnull
    @SuppressWarnings("SameReturnValue")
    private String doError(@Nonnull Response response, @Nonnull HttpStatus status, String message) {
        response.type(MediaType.JSON_UTF_8.withoutParameters().toString());
        LinkError error = new LinkError(status.code(), status.message(), message);
        error.setFactory(jsonFactory);
        halt(status.code(), error.toPrettyString());
        return "";
    }

    private <T extends GenericJson> T parseJson(String str, Class<T> dataClass)
            throws JsonParseException, JsonObjectParseException {
        try {
            final JsonObjectParser oParser = jsonFactory.createJsonObjectParser();
            final Reader r = CharStreams.asCharSource(str).openStream();
            T result = oParser.parseAndClose(r, dataClass);

            if (!result.getUnknownKeys().isEmpty()) {
                throw new JsonObjectParseException("Unknown keys: " + result.keySet());
            }

            return result;
        } catch (JsonParseException e2) {
            throw e2;
        } catch (IOException impossible) {
            // Reader wraps a string so low level IO problems should never occur
            throw new AssertionError(impossible);
        }
    }

    @Nonnull
    private String doLink(@Nonnull SimpleLinkRequest data, @Nonnull Response response) {
        if (data.getText() == null) {
            return doError(response, HttpStatus.Bad_Request,
                    "The \"text\" query parameter was not provided.");
        }

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

    @Nonnull
    private String doLink(@Nonnull LinkRequest data, @Nonnull Response response) {
        if (data.getDocuments() == null) {
            return doError(response, HttpStatus.Bad_Request,
                    "The \"documents\" key was not provided.");
        }
        if (data.getDocuments().isEmpty()) {
            return doError(response, HttpStatus.Bad_Request,
                    "The \"documents\" object is empty.");
        }
        if (!data.getDocuments().get(0).getUnknownKeys().isEmpty()) {
            return doError(response, HttpStatus.Bad_Request,
                    "The first document contains unknown keys: " +
                            data.getDocuments().get(0).getUnknownKeys().keySet());
        }
        if (data.getDocuments().get(0).getText() == null) {
            return doError(response, HttpStatus.Bad_Request,
                    "The \"text\" key was not provided in the first document.");
        }

        try {
            final String text = data.getDocuments().get(0).getText();
            response.type("application/json");
            anno.linkAsJson(text, response.raw().getOutputStream(), charset);
            return "";
        } catch (Throwable ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
            final String message = ex.getLocalizedMessage()
                    + System.getProperty("line.separator")
                    + Throwables.getStackTraceAsString(ex);
            return doError(response, HttpStatus.Internal_Server_Error, message);
        }

    }

    static class JsonObjectParseException extends Exception {

        private static final long serialVersionUID = 0;

        JsonObjectParseException() {
        }

        JsonObjectParseException(String message) {
            super(message);
        }

        JsonObjectParseException(String message, Throwable cause) {
            super(message, cause);
        }

        JsonObjectParseException(Throwable cause) {
            super(cause);
        }
    }


}
