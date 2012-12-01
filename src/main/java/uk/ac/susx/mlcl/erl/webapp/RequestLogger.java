/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.webapp;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.utils.SparkUtils;

/**
 * A spark filter that that simply dumps all the request information to a logger
 * as a json object structure.
 *
 * @author Hamish Morgan
 */
public class RequestLogger extends Filter {

  private static final Logger LOG = LoggerFactory.getLogger(RequestLogger.class);

  public static final LogLevel DEFAULT_LEVEL = LogLevel.DEBUG;

  private final JsonFactory factory;

  private final LogLevel level;

  public RequestLogger(String path, LogLevel level, JsonFactory factory) {
    super(Preconditions.checkNotNull(path));
    this.factory = Preconditions.checkNotNull(factory);
    this.level = Preconditions.checkNotNull(level);
  }

  public RequestLogger(String path) {
    this(path, DEFAULT_LEVEL, new JacksonFactory());
  }

  public RequestLogger() {
    this(SparkUtils.ALL_PATHS);
  }

  public LogLevel getLevel() {
    return level;
  }

  public JsonFactory getFactory() {
    return factory;
  }

  @Override
  public void handle(Request request, Response response) {
    Preconditions.checkNotNull(response);
    Preconditions.checkNotNull(response);

    if (!level.isEnabled(LOG))
      return;

    try {
      final StringWriter writer = new StringWriter();
      final JsonGenerator g = factory.createJsonGenerator(writer);
      g.enablePrettyPrint();

      g.writeStartObject();
      g.writeFieldName("request");
      writeRequest(request, g);
      g.writeEndObject();

      g.flush();
      g.close();
      level.log(LOG, writer.toString());

    } catch (IOException ex) {
      // Writes to memory buffer so IOExceptions are impossible.
      throw new AssertionError(ex);
    }
  }

  private static void writeRequest(Request request, JsonGenerator g)
	  throws IOException {
    g.writeStartObject();

    writeKeyValue("url", request.url(), g);

    g.writeFieldName("headers");
    g.writeStartObject();
    for (String header : request.headers()) {
      g.writeFieldName(header);
      g.writeString(request.headers(header));
    }
    g.writeEndObject();

    writeKeyValue("body", request.body(), g);
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
    g.writeEndObject();

  }

  private static <T> void writeArray(Iterable<T> items,
				     JsonGenerator generator)
	  throws IOException {
    if (items == null)
      generator.writeNull();
    else {
      generator.writeStartArray();
      for (T item : items) {
	generator.writeString(item.toString());
      }
      generator.writeEndArray();
    }
  }

  private static void writeKeyValue(String key, String value,
				    JsonGenerator generator)
	  throws IOException {
    Preconditions.checkNotNull(key);
    generator.writeFieldName(key);
    if (value == null)
      generator.writeNull();
    else
      generator.writeString(value);
  }

  private static void writeKeyValue(String key, int value,
				    JsonGenerator generator)
	  throws IOException {
    Preconditions.checkNotNull(key);
    generator.writeFieldName(key);
    generator.writeNumber(value);
  }

  private static void writeKeyValue(String key, boolean value,
				    JsonGenerator generator)
	  throws IOException {
    Preconditions.checkNotNull(key);
    generator.writeFieldName(key);
    generator.writeBoolean(value);
  }
}
