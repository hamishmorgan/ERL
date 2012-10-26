/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl;

import com.google.api.client.googleapis.services.GoogleKeyInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.Data;
import com.google.api.services.freebase.Freebase;
import com.google.api.services.freebase.Freebase.Text.Get;
import com.google.api.services.freebase.Freebase2;
import com.google.api.services.freebase.model.ContentserviceGet;
import com.google.common.io.Closeables;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author hiam20
 */
public class FreebaseKB {

    private static final Log LOG = LogFactory.getLog(FreebaseKB.class);
    private JsonFactory jsonFactory;
    private Freebase freebase;

    public FreebaseKB() {
    }

    void init() throws IOException {
        final String googleApiKey = Freebase2.loadGoogleApiKey(
                Paths.get(".googleApiKey.txt"));
        JsonHttpRequestInitializer credential = new GoogleKeyInitializer(googleApiKey);
        this.jsonFactory = new JacksonFactory();
        this.freebase = new Freebase.Builder(
                new NetHttpTransport(), jsonFactory, null)
                .setApplicationName("ERL/1.0")
                .setJsonHttpRequestInitializer(credential)
                .build();
    }

    public String getText(String id) throws IOException {
        List<String> xx = Arrays.asList(id);
        Get get = freebase.text().get(xx);
        ContentserviceGet csGet = get.execute();
        return csGet.getResult();
    }

    static class Entity {

        @com.google.api.client.util.Key("id")
        private String id;
        @com.google.api.client.util.Key("name")
        private String name;
        @com.google.api.client.util.Key("type")
        private String type;

        public Entity(String id, String name, String type) {
            this.id = id == null ? Data.NULL_STRING : id;
            this.name = name == null ? Data.NULL_STRING : name;
            this.type = type == null ? Data.NULL_STRING : type;
        }
    }

    String buildMQLQuery(String id, String name, String type) throws IOException {
        StringWriter writer = new StringWriter();
        JsonGenerator gen = jsonFactory.createJsonGenerator(writer);
        gen.enablePrettyPrint();
        gen.writeStartArray();
        Entity ent = new Entity(null, null, type);
        gen.serialize(ent);
        //
        //            gen.writeStartObject();
        //            gen.writeFieldName("id");
        //            gen.writeNull();
        //            gen.writeFieldName("name");
        //            gen.writeNull();
        //            gen.writeFieldName("type");
        //            gen.writeString(type);
        //            gen.writeEndObject();
        gen.writeEndArray();
        gen.flush();
        return writer.toString();
    }

    public String rawMQLQuery(String query) throws IOException {
        Freebase.Mqlread mlr = freebase.mqlread(query);
        mlr.setPrettyPrint(true);
        mlr.setIndent(2L);
        InputStream is = null;
        try {
            is = mlr.executeAsInputStream();
            final Reader reader = new InputStreamReader(is);
            final StringBuilder sb = new StringBuilder();
            final char[] buff = new char[8096];
            int n;
            while ((n = reader.read(buff)) > 0) {
                sb.append(buff, 0, n);
            }
            return sb.toString();
        } finally {
            Closeables.closeQuietly(is);
        }
    }

    void getInstanceOfType(String type) throws IOException {
        String query = buildMQLQuery(null, null, type);
        System.out.println(query);
        Freebase.Mqlread mlr = freebase.mqlread(query);
        InputStream is = mlr.executeAsInputStream();
        JsonParser parser = jsonFactory.createJsonParser(is);
        while (is.available() > 0) {
            System.out.print((char) is.read());
        }
        System.out.println();
    }

    void foo() throws IOException {
        //            getInstanceOfType("/astronomy/planet");
        getInstanceOfType("/music/artist");
        //
        //              {
        //
        //            String query =
        //                    "[{\"id\":null,\"name\":null,\"type\":\"/astronomy/planet\"}]";
        //            Freebase.Mqlread mlr = freebase.mqlread(query);
        //            InputStream is = mlr.executeAsInputStream();
        //
        //            while (is.available() > 0) {
        //                System.out.print((char) is.read());
        //            }
        //            System.out.println();
        //
        //        }
        //
        //              {
        //
        //            String query =
        //                    "[{\"id\":null,\"name\":null,\"type\":\"/astronomy/planet\"}]";
        //            Freebase.Mqlread mlr = freebase.mqlread(query);
        //            InputStream is = mlr.executeAsInputStream();
        //
        //            while (is.available() > 0) {
        //                System.out.print((char) is.read());
        //            }
        //            System.out.println();
        //
        //        }
        {
            String query = "[{\"id\":null,\"name\":\"Uranus\",\"type\":null}]";
            Freebase.Mqlread mlr = freebase.mqlread(query);
            InputStream is = mlr.executeAsInputStream();
            while (is.available() > 0) {
                System.out.print((char) is.read());
            }
            System.out.println();
        }
    }

}
