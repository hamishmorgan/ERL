/*
 * Copyright (c) 2012, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.webapp;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.jackson.JacksonFactory;
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
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A spark filter that that simply dumps all the request information to a logger as a JSON object
 * structure.
 *
 * @author Hamish Morgan
 */
@Immutable
@Nonnull
public class ResponseLogger extends Filter {

    public static final LogLevel DEFAULT_LEVEL = LogLevel.DEBUG;
    public static final String DEFAULT_PATH = SparkUtils.ALL_PATHS;
    private static final Logger LOG = LoggerFactory.getLogger(ResponseLogger.class);
    private final JsonFactory jsonFactory;
    private final LogLevel level;

    public ResponseLogger(String path, LogLevel level, JsonFactory jsonFactory) {
        super(checkNotNull(path));
        this.jsonFactory = checkNotNull(jsonFactory);
        this.level = checkNotNull(level);
    }

    public ResponseLogger(String path, LogLevel level) {
        this(path, level, Lazy.DEFAULT_JSON_FACTORY);
    }

    public ResponseLogger(LogLevel level) {
        this(DEFAULT_PATH, level);
    }

    public ResponseLogger(String path) {
        this(path, DEFAULT_LEVEL);
    }

    public ResponseLogger() {
        this(DEFAULT_PATH);
    }

    private static void writeResponse(@Nonnull Response response, @Nonnull JsonGenerator g)
            throws IOException {

        g.writeStartObject();

        writeKeyValue("body", response.body(), g);
        g.writeFieldName("raw");
        writeHttpServletResponse(response.raw(), g);

        g.writeEndObject();
    }

    private static void writeHttpServletResponse(@Nonnull HttpServletResponse raw,
                                                 @Nonnull JsonGenerator g)
            throws IOException {

        g.writeStartObject();


        writeKeyValue("characterEncoding", raw.getCharacterEncoding(), g);
        writeKeyValue("contentType", raw.getContentType(), g);
        writeKeyValue("locale", raw.getLocale().toString(), g);
        writeKeyValue("toString", raw.toString(), g);

        g.writeEndObject();

    }

    private static <T> void writeArray(@Nullable Iterable<T> items,
                                       @Nonnull JsonGenerator generator)
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
                                      @Nonnull JsonGenerator generator)
            throws IOException {
        generator.writeFieldName(checkNotNull(key));
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeString(value);
        }
    }

    private static void writeKeyValue(String key, boolean value,
                                      @Nonnull JsonGenerator generator)
            throws IOException {
        generator.writeFieldName(checkNotNull(key));
        generator.writeBoolean(value);
    }

    private static boolean isFormData(@Nonnull final Request request) {
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
    public void handle(Request ignore, @Nonnull Response response) {
        checkNotNull(response, "response");

        if (!level.isEnabled(LOG)) {
            return;
        }

        try {
            final StringWriter writer = new StringWriter();
            final JsonGenerator generator = jsonFactory.createJsonGenerator(writer);
            generator.enablePrettyPrint();

            generator.writeStartObject();
            generator.writeFieldName("request");
            writeResponse(response, generator);
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
