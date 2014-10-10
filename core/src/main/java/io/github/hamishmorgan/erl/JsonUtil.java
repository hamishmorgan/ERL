package io.github.hamishmorgan.erl;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import io.github.hamishmorgan.erl.snlp.annotations.EntityKbIdAnnotation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class JsonUtil {


    private final JsonFactory jsonFactory;

    public JsonUtil(JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }


    public String annotationToJson(@Nonnull Annotation document) throws IOException {
        final StringWriter writer = new StringWriter();
        annotationToJson(document, writer);
        return writer.toString();
    }


    @VisibleForTesting
    public void printAnnotationAsJson(@Nonnull Annotation document) throws IOException {
        final PrintWriter writer = new PrintWriter(System.out);
        annotationToJson(document, writer);
        writer.flush();
    }


    private List<List<CoreLabel>> getEntityChunks(@Nonnull Annotation document) {
        final List<List<CoreLabel>> chunks = Lists.newArrayList();
        final List<CoreLabel> tokens = document.get(CoreAnnotations.TokensAnnotation.class);
        final Iterator<CoreLabel> it = tokens.iterator();
        String prevId = null;
        String prevType = null;
        List<CoreLabel> chunk = Lists.newArrayList();
        while (it.hasNext()) {
            final CoreLabel nextToken = it.next();
            final String nextId = nextToken.get(EntityKbIdAnnotation.class);
            final String nextType = nextToken.get(CoreAnnotations.NamedEntityTagAnnotation.class);

            final boolean entityChanged
                    = ((prevId != nextId) || ((prevId != null) && !prevId.equals(nextId)))
                    || ((prevType != nextType) || ((null != nextType) && !prevType.equals(nextType)));

            if (entityChanged) {
                if (!chunk.isEmpty())
                    chunks.add(chunk);
                chunk = Lists.newArrayList();
            }

            // add current token
            chunk.add(nextToken);
            prevId = nextId;
            prevType = nextType;
        }

        if (!chunk.isEmpty())
            chunks.add(chunk);
        return chunks;
    }


    @VisibleForTesting
    public void annotationToJson(@Nonnull Annotation document, Writer writer) throws IOException {
        final JsonGenerator generator = jsonFactory.createJsonGenerator(writer);
        generator.enablePrettyPrint();
        generator.writeStartArray();

        final List<List<CoreLabel>> chunks = getEntityChunks(document);
        writeEntityChunks(generator, document, chunks);

        generator.writeEndArray();
        generator.flush();
    }


    private void writeEntityChunks(@Nonnull JsonGenerator generator, @Nonnull Annotation document, @Nonnull List<List<CoreLabel>> chunks) throws IOException {
        final String documentText = document.get(CoreAnnotations.TextAnnotation.class);

        int prevEnd = 0;  // The index of the last character in the sequence
        for (List<CoreLabel> chunk : chunks) {
            if (chunk.isEmpty())
                continue;

            final int start = chunk.get(0).beginPosition();
            final int end = chunk.get(chunk.size() - 1).endPosition();
            final String seq = documentText.substring(start, end);
            final String id = chunk.get(0).get(EntityKbIdAnnotation.class);
            final String type = chunk.get(0).get(CoreAnnotations.NamedEntityTagAnnotation.class);

            if (start > prevEnd) {
                writeJsonObj(generator, documentText.substring(prevEnd, start), null, null);
            }

            writeJsonObj(generator, seq, id, type);
            prevEnd = end;
        }

        if (prevEnd < documentText.length()) {
            writeJsonObj(generator, documentText.substring(prevEnd, documentText.length()), null, null);
        }
    }

    private void writeJsonObj(@Nonnull JsonGenerator generator, @Nonnull String text,
                              @Nullable String entityId, @Nullable String entityType)
            throws IOException {

        generator.writeStartObject();
        generator.writeFieldName("text");
        generator.writeString(xmlEncode(text));

        if (entityId != null) {
            generator.writeFieldName("id");
            generator.writeString(entityId);
            generator.writeFieldName("url");
            generator.writeString("http://www.freebase.com/view" + entityId);
        }

        if (entityType != null) {
            generator.writeFieldName("type");
            generator.writeString(entityType);
        }
        generator.writeEndObject();
    }

    @Nonnull
    private static String xmlEncode(@Nonnull String input) {
        checkNotNull(input, "input");

        // Search through the string for the first character the requires encoding
        int i = 0;
        searching:
        while (i < input.length()) {
            switch (input.charAt(i)) {
                case '<':
                case '>':
                case '&':
                    break searching;
                default:
                    i++;
            }
        }

        // If no encoding is required just return the input string unmodified
        if (i == input.length()) {
            return input;
        }

        // Encoding required, write everything to a builder replacing special
        // characters with their escape sequences.
        final StringBuilder output = new StringBuilder();
        output.append(input, 0, i);
        while (i < input.length()) {
            switch (input.charAt(i)) {
                case '<':
                    output.append("&lt;");
                    break;
                case '>':
                    output.append("&gt;");
                    break;
                case '&':
                    output.append("&amp;");
                    break;
                default:
                    output.append(input.charAt(i));
            }
            i++;
        }

        return output.toString();
    }

}
