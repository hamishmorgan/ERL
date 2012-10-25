/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl;

import com.google.api.client.googleapis.services.GoogleKeyInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.Data;
import com.google.api.services.freebase.Freebase;
import com.google.api.services.freebase.model.ContentserviceGet;
import com.google.common.io.Closeables;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 *
 * @author hiam20
 */
public class FreebaseTest {

    private static TestName name;
    private static JsonFactory jsonFactory;
    private static Freebase freebase;

    public FreebaseTest() {
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        name = new TestName();
        jsonFactory = new JacksonFactory();
        final String googleApiKey = FreebaseKB.loadGoogleApiKey();
        JsonHttpRequestInitializer credential = new GoogleKeyInitializer(googleApiKey);
        jsonFactory = new JacksonFactory();
        freebase = new Freebase.Builder(
                new NetHttpTransport(), jsonFactory, null)
                .setApplicationName("ERL/1.0")
                .setJsonHttpRequestInitializer(credential)
                .build();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        System.out.println();
        System.out.println(this.getClass().getName() + "#" + name.getMethodName());
        System.out.println();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetText() throws IOException {
        final String id = "/en/brighton_hove";

        final Freebase.Text.Get textGet = freebase.text().get(Arrays.asList(id));


//        textGet.setFormat("raw");

//        textGet.setFormat("html");

        textGet.setFormat("plain");
        textGet.setMaxlength((long) Integer.MAX_VALUE);


        final ContentserviceGet csGet = textGet.execute();

        String result = csGet.getResult();

        Assert.assertNotNull(result);
        Assert.assertTrue(!result.isEmpty());

        System.out.println(result);
    }

    @Test
    public void testBasicMQLRead() throws IOException {
        String query = ""
                + "{\n"
                + "  \"type\" : \"/music/artist\",\n"
                + "  \"name\" : \"The Police\",\n"
                + "  \"album\" : []\n"
                + "}";

        Freebase.Mqlread mlr = freebase.mqlread(query);
        mlr.setPrettyPrint(true);
        mlr.setIndent(2L);

        InputStream is = mlr.executeAsInputStream();
        String result = IOUtils.toString(is);
        IOUtils.closeQuietly(is);

        System.out.println(result);
    }

    static String toJson(Object o) throws IOException {
        StringWriter writer = new StringWriter();
        JsonGenerator gen = jsonFactory.createJsonGenerator(writer);
        gen.enablePrettyPrint();
        gen.serialize(o);
        gen.flush();
        return writer.toString();
        
    }
    @Test
    public void testCursorMQLRead() throws IOException {
        String query = ""
                + "[{\n"
                + "  \"type\" : \"/music/artist\",\n"
                + "  \"name\" : null,\n"
                + "  \"limit\" : 5\n"
                + "}]";


        Freebase.Mqlread mlr = freebase.mqlread(query);

        
        System.out.println("Cursor = " + mlr.getCursor());

        mlr.setPrettyPrint(true);
        mlr.setIndent(2L);
        mlr.setCursor(Data.NULL_STRING);
        
        
        System.out.println(toJson(mlr));

        System.out.println("Query = " + mlr.getQuery());
        System.out.println("Cursor = " + mlr.getCursor());

        {
            
            HttpResponse response = mlr.executeUnparsed();
         
            InputStream is = response.getContent();
            String result = IOUtils.toString(is);
            IOUtils.closeQuietly(is);

            System.out.println("Result = " + result);
            System.out.println("Cursor = " + mlr.getCursor());
            System.out.println("Headers = " + response.getHeaders());
            
            
        }
    
    }
}
