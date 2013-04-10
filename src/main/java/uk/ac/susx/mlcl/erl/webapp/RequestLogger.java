/*
 * Copyright (c) 2012, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.webapp;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.common.collect.Lists;
import org.eclipse.jetty.http.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.utils.SparkUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A spark filter that that simply dumps all the request information to a logger as a JSON object
 * structure.
 *
 * @author Hamish Morgan
 */
@Immutable
@Nonnull
public class RequestLogger extends Filter {

    public static final LogLevel DEFAULT_LEVEL = LogLevel.DEBUG;
    public static final String DEFAULT_PATH = SparkUtils.ALL_PATHS;
    private static final Logger LOG = LoggerFactory.getLogger(RequestLogger.class);
    private final JsonFactory jsonFactory;
    private final LogLevel level;

    public RequestLogger(String path, LogLevel level, JsonFactory jsonFactory) {
        super(checkNotNull(path));
        this.jsonFactory = checkNotNull(jsonFactory);
        this.level = checkNotNull(level);
    }

    public RequestLogger(String path, LogLevel level) {
        this(path, level, Lazy.DEFAULT_JSON_FACTORY);
    }

    public RequestLogger(LogLevel level) {
        this(DEFAULT_PATH, level);
    }

    public RequestLogger(String path) {
        this(path, DEFAULT_LEVEL);
    }

    public RequestLogger() {
        this(DEFAULT_PATH);
    }

    private static void writeRequest(Request request, JsonGenerator g)
            throws IOException {

        // XXX: Work-around to Spark bug: During POST method requests, if the body
        // is read before queryParams, the params will never be populated.
        if (isFormData(request)) {
            request.queryParams();
        }

        g.writeStartObject();

        writeKeyValue("url", request.url(), g);

        g.writeFieldName("headers");
        g.writeStartObject();
        for (String header : request.headers()) {
            g.writeFieldName(header);
            g.writeString(request.headers(header));
        }
        g.writeEndObject();

        // XXX: Don't touch the body or it may never be retrievable again (due to Spark wackiness)
//        writeKeyValue("body", request.body(), g);

        writeKeyValue("contentType", request.contentType(), g);
        writeKeyValue("requestMethod", request.requestMethod(), g);

        g.writeFieldName("queryParams");
        g.writeStartObject();
        for (String queryParam : request.queryParams()) {
            g.writeFieldName(queryParam);
            g.writeString(request.queryParams(queryParam));
        }
        g.writeEndObject();

        writeKeyValue("queryString", request.queryString(), g);
        writeKeyValue("userAgent", request.userAgent(), g);

        g.writeFieldName("attributes");
        g.writeStartObject();
        for (String attribute : request.attributes()) {
            g.writeFieldName(attribute);
            g.writeString(request.attribute(attribute).toString());
        }
        g.writeEndObject();

        g.writeFieldName("raw");
        writeHttpServletRequest(request.raw(), g);

        g.writeEndObject();
    }

    private static void writeHttpServletRequest(HttpServletRequest raw,
                                                JsonGenerator g)
            throws IOException {

        g.writeStartObject();
        writeKeyValue("authType", raw.getAuthType(), g);

        g.writeFieldName("cookies");
        writeArray(raw.getCookies() == null ? Lists.newArrayList()
                : Lists.newArrayList(raw.getCookies()), g);

        writeKeyValue("method", raw.getMethod(), g);

        // Attempt to use URI object to combine the various remote components, but
        // fall back to just string formatting if something goes wrong.
        try {
            final URI uri = new URI(
                    null, raw.getRemoteUser(), raw.getRemoteHost(),
                    raw.getRemotePort(), null, null, null);
            writeKeyValue("remote", uri.toString(), g);
        } catch (URISyntaxException ex) {
            final String uriString = String.format(
                    "//%s@[%s]:%d", raw.getRemoteUser(),
                    raw.getRemoteHost(), raw.getRemotePort());
            writeKeyValue("remote", uriString, g);
        }

        writeKeyValue("requestedSessionId", raw.getRequestedSessionId(), g);
        writeKeyValue("secure", raw.isSecure(), g);

        writeKeyValue("toString", raw.toString(), g);

        g.writeEndObject();

    }

    private static <T> void writeArray(@Nullable Iterable<T> items,
                                       JsonGenerator generator)
            throws IOException {
        if (items == null) {
            generator.writeNull();
        } else {
            generator.writeStartArray();
            for (T item : items) {
                generator.writeString(item.toString());
            }
            generator.writeEndArray();
        }
    }

    private static void writeKeyValue(String key, @Nullable String value,
                                      JsonGenerator generator)
            throws IOException {
        generator.writeFieldName(checkNotNull(key));
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeString(value);
        }
    }

    private static void writeKeyValue(String key, boolean value,
                                      JsonGenerator generator)
            throws IOException {
        generator.writeFieldName(checkNotNull(key));
        generator.writeBoolean(value);
    }

    private static boolean isFormData(final Request request) {
        if (request.contentType() == null) {
            return false;
        }
        // http://www.w3.org/Protocols/rfc1341/4_Content-Type.html
        // Content-Type := type "/" subtype *[";" parameter]

        final int colonIdx = request.contentType().indexOf(';');
        final String ctype = ((colonIdx == -1)
                ? request.contentType()
                : request.contentType().substring(0, colonIdx));


        return MimeTypes.Type.FORM_ENCODED.is(ctype);
    }

    public LogLevel getLevel() {
        return level;
    }

    public JsonFactory getJsonFactory() {
        return jsonFactory;
    }

    @Override
    public void handle(Request request, Response IGNORED) {
        checkNotNull(request, "request");

        if (!level.isEnabled(LOG)) {
            return;
        }

        try {
            final StringWriter writer = new StringWriter();
            final JsonGenerator generator = jsonFactory.createJsonGenerator(writer);
            generator.enablePrettyPrint();

            generator.writeStartObject();
            generator.writeFieldName("request");
            writeRequest(request, generator);
            generator.writeEndObject();

            generator.flush();
            generator.close();
            level.log(LOG, writer.toString());

        } catch (IOException ex) {
            // Generator writes to memory buffer so IOExceptions are impossible.
            throw new AssertionError(ex);
        }
    }

    /**
     * Hold a single instance of the default JsonFactory, which will only be instantiated the first
     * time it is accessed.
     */
    public static class Lazy {

        public static final JsonFactory DEFAULT_JSON_FACTORY = new JacksonFactory();

        private Lazy() {
        }
    }
}
