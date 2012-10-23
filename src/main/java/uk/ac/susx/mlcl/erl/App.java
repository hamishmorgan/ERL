package uk.ac.susx.mlcl.erl;

import com.google.api.client.googleapis.services.GoogleKeyInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.freebase.Freebase;
import com.google.api.services.freebase.Freebase.Text.Get;
import com.google.api.services.freebase.model.ContentserviceGet;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Hello world!
 * <p/>
 */
public class App {

    private static final Log LOG = LogFactory.getLog(App.class);

    static class Entity implements Serializable {

        private static final long serialVersionUID = 1L;
        
        public String id;
        public String name;
        public String type;

        public Entity(String id, String name, String type) {
            this.id = id;
            this.name = name;
            this.type = type;
        }
        
        
    }
    static class FreebaseKB {

        private JsonFactory jsonFactory;

        private Freebase freebase;

        void init() throws IOException {

            final String googleApiKey = loadGoogleApiKey();

            JsonHttpRequestInitializer credential =
                    new GoogleKeyInitializer(googleApiKey);

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

        String buildMQLQuery(String id, String name, String type)
                throws IOException {
              StringWriter writer = new StringWriter();
            JsonGenerator gen = jsonFactory.createJsonGenerator(writer);
            gen.writeStartArray();
            
            Entity ent = new Entity(null, null, type);
            gen.serialize(ent);
//            
            gen.writeStartObject();
            gen.writeFieldName("id");
            gen.writeNull();
            gen.writeFieldName("name");
            gen.writeNull();
            gen.writeFieldName("type");
            gen.writeString(type);
            gen.writeEndObject();
            gen.writeEndArray();
            gen.flush();
            
            return writer.toString();
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
            
            
            getInstanceOfType("/astronomy/planet");
            
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

        {
            String query =
                    "[{\"id\":null,\"name\":\"Uranus\",\"type\":null}]";
            Freebase.Mqlread mlr = freebase.mqlread(query);

            InputStream is = mlr.executeAsInputStream();

            while (is.available() > 0) {
                System.out.print((char) is.read());
            }
            System.out.println();

        }

        }
        static String loadGoogleApiKey() throws IOException {

            final String googleApiKey;
            Path googleApiKeyPath = Paths.get("google_api_key.txt");
            if (Files.exists(googleApiKeyPath)) {
                byte[] bytes = Files.readAllBytes(googleApiKeyPath);
                googleApiKey = new String(bytes);
            } else {
                LOG.warn("google_api_key.txt does not exist");
                googleApiKey = null;
            }

            return googleApiKey;
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Hello World!");

        FreebaseKB fkb = new FreebaseKB();
        fkb.init();

        System.out.println(fkb.getText("en/barack_obama"));
        
        
        // /astronomy/planet\
        
        
        fkb.foo();;
//        
        
//        
//
//        Freebase freebase = initFreebase();
//
//        List<String> xx = new ArrayList<String>();
//        xx.add("/en/barack_obama");
//        ContentserviceGet get = freebase.text().get(xx).execute();
//        System.out.println(get.getResult());
//
//      

//        mlr.execute();
//        System.out.println(mlr.executeUnparsed());


//        
//        
//        AccessMethod accessMethod = null;
//        
//        String tokenServerEncodedUrl = null;
//        
//        HttpExecuteInterceptor clientAuthentication = null;
//        HttpRequestInitializer requestInitializer = null;
//        
//        // Set up the HTTP transport and JSON factory
//        HttpTransport httpTransport = new NetHttpTransport();
//
//        
//        // Set up OAuth 2.0 access of protected resources
//        // using the refresh and access tokens, automatically
//        // refreshing the access token when it expires
//        
//        Credential credential = new Credential.Builder(accessMethod)
//            .setJsonFactory(jsonFactory)
//            .setTransport(httpTransport)
//            .setTokenServerEncodedUrl(tokenServerEncodedUrl)
//            .setClientAuthentication(clientAuthentication)
//            .setRequestInitializer(requestInitializer)
//            .build();
//
//        
//        Freebase freebase = new Freebase.Builder(
//                httpTransport, jsonFactory, credential).build();
//        
//        
//        Freebase.Mqlread foo = freebase.mqlread("");
//        foo.execute();
//        
//        

//        
//        
//        
//        // Set up the main Google+ class
//        Plus plus = new Plus(httpTransport, jsonFactory, credential);
//
//        // Make a request to access your profile and display it to console
//        Person profile = plus.people().get("me").execute();
//        System.out.println("ID: " + profile.getId());
//        System.out.println("Name: " + profile.getDisplayName());
//        System.out.println("Image URL: " + profile.getImage().getUrl());
//        System.out.println("Profile URL: " + profile.getUrl());

    }
}
