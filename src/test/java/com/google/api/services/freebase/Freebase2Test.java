/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.google.api.services.freebase;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.batch.BatchCallback;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpRequest;
import com.google.api.client.http.json.JsonHttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.freebase.model.ContentserviceGet;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestName;
import uk.ac.susx.mlcl.erl.test.AbstractTest;
import uk.ac.susx.mlcl.erl.test.Categories;

/**
 * A collection of tests, designed more to insure the APIs work as expected than to find bugs.
 * <p/>
 * @author hiam20
 */
@Category(Categories.OnlineTests.class)
public class Freebase2Test extends AbstractTest {

    private static JsonFactory jsonFactory;

    private static Freebase2 freebase;

    public Freebase2Test() {
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        jsonFactory = new JacksonFactory();

        final String googleApiKey = Freebase2.loadGoogleApiKey(Paths.get(
                ".googleApiKey.txt"));

        JsonHttpRequestInitializer requestInitializer =
                new JsonHttpRequestInitializer() {
                    public void initialize(JsonHttpRequest request) {
                        FreebaseRequest freebaseRequest =
                                (FreebaseRequest) request;
                        freebaseRequest.setPrettyPrint(true);
                        if (request instanceof Freebase.Mqlread) {
                            ((Freebase.Mqlread) request).setIndent(2L);
                        }
                        freebaseRequest.setKey(googleApiKey);
                    }
                };

        jsonFactory = new JacksonFactory();



        freebase = new Freebase2.Builder(
                new NetHttpTransport(), jsonFactory, null)
                .setApplicationName("ERL/1.0")
                .setJsonHttpRequestInitializer(requestInitializer)
                .setObjectParser(jsonFactory.createJsonObjectParser())
                .build();
    }


    //
    // =================================================================
    //  Search tests
    // =================================================================
    //
    @Test
    public void testSearch() throws IOException {


        Freebase2.Search search =
                freebase.search("brighton");
        search.setIndent(Boolean.TRUE);


        InputStream is = search.executeAsInputStream();
        String result = IOUtils.toString(is);
        IOUtils.closeQuietly(is);


        System.out.println(result);

    }

    @Test
    public void testSearchWithObjects() throws IOException {


        Freebase2.Search search = freebase.search("brighton");
        search.setIndent(Boolean.TRUE);
        search.setFormat(SearchFormat.ENTITY);

        SearchFormat.EntityResult srs = search.executeParseObject(
                SearchFormat.EntityResult.class);

        System.out.println(srs);
    }

    @Test
    public void testSearchFormats() throws IOException {
        for (SearchFormat format : SearchFormat.values()) {
            System.out.println("Format: " + format);
            Freebase2.Search search = freebase.search("brighton");
            search.setIndent(Boolean.TRUE);
            search.setFormat(format);

            InputStream is = search.executeAsInputStream();
            String result = IOUtils.toString(is);
            IOUtils.closeQuietly(is);

            System.out.println(result);
        }
    }

    //
    // =================================================================
    //  Text tests
    // =================================================================
    //
    @Test
    public void testGetText() throws IOException {
        final String id = "/en/brighton_hove";

        final Freebase.Text.Get textGet =
                freebase.text().get(Arrays.asList(id));

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
    //
    // =================================================================
    //  MQLRead tests
    // =================================================================
    //

    @Test
    public void testBasicMQLRead() throws IOException {
        String query = ""
                + "{\n"
                + "  \"type\" : \"/music/artist\",\n"
                + "  \"name\" : \"The Police\",\n"
                + "  \"album\" : []\n"
                + "}";

        Freebase.Mqlread mlr = freebase.mqlread(query);

        InputStream is = mlr.executeAsInputStream();
        String result = IOUtils.toString(is);
        IOUtils.closeQuietly(is);

        System.out.println(result);
    }

    @Test
    public void testCursorMQLRead() throws IOException {

        String query = ""
                + "[{\n"
                + "  \"type\" : \"/music/artist\",\n"
                + "  \"a:name~=\" : \"Tree\",\n"
                + "  \"name\" : null,\n"
                + "  \"limit\" : 5\n"
                + "}]";


        // Of course the empty string is true >_<
        String cursor = "";

        int i = 0;
        while (i < 10 && !cursor.equals("false")) {

            Freebase.Mqlread mlr = freebase.mqlread(query);

            mlr.setCursor(cursor);

            System.out.println(toJson(mlr));
            System.out.println("Query = " + mlr.getQuery());

            HttpResponse response = mlr.executeUnparsed();

            InputStream is = response.getContent();
            String result = IOUtils.toString(is);
            IOUtils.closeQuietly(is);

            System.out.println("Result = " + result);

            // Extract the cursor
            JsonParser parser = jsonFactory.createJsonParser(result);
            parser.skipToKey("cursor");
            cursor = parser.getText();

        }
    }
    //
    // =================================================================
    //  Image tests
    // =================================================================
    //

    @Test
    public void testImage() throws IOException {

        List<String> id = Arrays.asList("en/university_of_sussex");
        Freebase.Image im = freebase.image(id);

        // one of: fit, fill, fillcrop, fillcropmid
        im.setMode("fit");

        // 0,0 for original size
        im.setMaxheight(0L);
        im.setMaxwidth(0L);

        im.setPad(false);

        im.execute();

        final String fileExtension;
        String contentType = im.getLastResponseHeaders().getContentType();
        if (contentType.equals("image/png")) {
            fileExtension = "png";
        } else if (contentType.equals("image/jpeg")) {
            fileExtension = "jpeg";
        } else if (contentType.equals("image/gif")) {
            fileExtension = "gif";
        } else {
            throw new AssertionError(
                    "Unknown image content type: " + contentType);
        }

        FileOutputStream out = new FileOutputStream("out." + fileExtension);
        im.download(out);
        out.flush();
        out.close();


    }

    //
    // =================================================================
    //  MQL functionality tests
    // =================================================================
    //
    @Test
    public void getAlbumsByThePolice() throws IOException {
        String q = "{"
                + "\"type\" : \"/music/artist\","
                + "\"name\" : \"The Police\","
                + "\"album\" : []"
                + "}";
        runQuery(q);
    }

    @Test
    public void getIdOfSpecificAlbum() throws IOException {
        String q = "{"
                + "\"type\" : \"/music/album\","
                + "\"artist\": \"The Police\","
                + "\"name\" : \"Synchronicity\","
                + "\"id\" : null"
                + "}";
        runQuery(q);
    }

    @Test(expected = GoogleJsonResponseException.class)
    public void testBadlyFormedQuery() throws IOException {

        // there is a missing " in the id's value
        String q = " {\n"
                + "  \"id\": \"/en/the_police,\n"
                + "  \"name\" : null,\n"
                + "  \"type\" : []\n"
                + "}\n"
                + "       ";
        runQuery(q);
    }

    @Test(expected = GoogleJsonResponseException.class)
    public void testUniquenessError() throws IOException {

        String q = " {\n"
                + "  \"id\": \"/en/the_police\",\n"
                + "  \"name\" : null,\n"
                + "  \"type\" : null\n"
                + "}\n"
                + "       ";
        runQuery(q);
    }

    @Test()
    public void getTypesForID() throws IOException {

        String q = " {\n"
                + "  \"id\": \"/en/the_police\",\n"
                + "  \"name\" : null,\n"
                + "  \"type\" : []\n"
                + "}\n"
                + "       ";

        runQuery(q);
    }

    @Test()
    public void getNamesForId() throws IOException {
        String q = " {\n"
                + "  \"id\": \"/en/united_states\",\n"
                + "  \"name\" : [{}]\n"
                + "}\n"
                + "       ";
        runQuery(q);
    }

    @Test()
    public void testNestedSubquery() throws IOException {

        String q = " {\n"
                + "  \"type\" : \"/music/artist\",\n"
                + "  \"name\" : \"The Police\",\n"
                + "  \"album\" : {\n"
                + "    \"name\" : \"Synchronicity\",\n"
                + "  \"primary_release\" : { \"track\" : [] }\n"
                + "  }\n"
                + "}";

        runQuery(q);
    }

    @Test()
    public void testNestedSubquery2() throws IOException {

        String q = "[{\n"
                + "  \"type\":\"/music/artist\",\n"
                + "  \"name\":null,\n"
                + "  \"album\": [{\n"
                + "    \"name\":null,\n"
                + "  \"primary_release\" : { \"track\" : [{\"name\":\"Too Much Information\",  \"length\": null}] }\n"
                + "  }]\n"
                + "}]";

        runQuery(q);
    }

    @Test()
    public void foo1() throws IOException {

        String q = "{\n"
                + "  \"id\" : \"/en/the_police\",\n"
                + "  \"name\" : {},\n"
                + "  \"type\" : [{}]\n"
                + "}";

        runQuery(q);
    }

    @Test()
    public void foo2() throws IOException {

        String q = "{\n"
                + "  \"type\" : \"/music/album\",\n"
                + "  \"name\" : \"Synchronicity\",\n"
                + "  \"artist\" : \"The Police\",\n"
                + "  \"primary_release\" : { \"track\" : [] }\n"
                + "}";

        runQuery(q);
    }

    @Test()
    public void getKeysForObject() throws IOException {
        String q = "{\n"
                + "  \"id\":\"/en/the_police\",\n"
                + "  \"key\":[{}]\n"
                + "}";
        runQuery(q);
    }

    @Test()
    public void getKeysForNamespace2() throws IOException {
        String q = "{\n"
                + "  \"type\":\"/type/namespace\",\n"
                + "  \"id\":\"/topic\",\n"
                + "  \"key\":[{}],\n"
                + "  \"keys\":[{}]\n"
                + "}";
        runQuery(q);
    }

    @Test()
    public void testBidirectionallity() throws IOException {
        String q = "[{\n"
                + "  \"type\":\"/music/artist\",\n"
                + "  \"name\":null,\n"
                + "  \"album\":[{\n"
                + "    \"name\":\"Greatest Hits\",\n"
                + "    \"artist\":{\n"
                + "      \"name\": null,\n"
                + "      \"album\":\"Super Hits\"\n"
                + "    }\n"
                + "  }]\n"
                + "}]";
        runQuery(q);
    }

    @Test()
    public void testObjectPropertiesWildcard() throws IOException {
        String q = "{\n"
                + "  \"id\":\"/en/brighton\",\n"
                + "  \"*\":null\n"
                + "}";
        runQuery(q);
    }

    @Test()
    public void testLocationPropertiesWildcard() throws IOException {
        String q = "{\n"
                + "  \"id\":\"/en/brighton\",\n"
                + "  \"type\":\"/location/location\",\n"
                + "  \"*\":null\n"
                + "}";
        runQuery(q);
    }

    @Test()
    public void testFinalCountryNationals() throws IOException {
        String q = "{\n"
                + "  \"type\" : \"/location/country\",\n"
                + "  \"id\" : \"/en/united_kingdom\",\n"
                + "  \"!/people/person/nationality\" : []\n"
                + "}";
        runQuery(q);
    }

    @Test()
    public void testLimit() throws IOException {
        String q = "[{\n"
                + "  \"type\":\"/music/artist\",\n"
                + "  \"name\":null,\n"
                + "  \"limit\":2000\n"
                + "}]";
        runQuery(q);
    }

    @Test()
    public void testLimit2() throws IOException {
        String q = " [{\n"
                + "  \"type\":\"/music/artist\",\n"
                + "  \"name\":null,\n"
                + "  \"track\":{\n"
                + "    \"name\":\"Masters of War\",\n"
                + "    \"limit\":0\n"
                + "  },\n"
                + "  \"limit\":3\n"
                + "}]";
        runQuery(q);
    }

    @Test()
    public void testFinalCountryNationalsWithLimit() throws IOException {
        String q = "{\n"
                + "  \"type\" : \"/location/country\",\n"
                + "  \"id\" : \"/en/united_kingdom\",\n"
                + "  \"!/people/person/nationality\" : [{\"name\":null, \"limit\":10}]\n"
                + "}";
        runQuery(q);
    }

    @Test()
    public void testCountCountryNationals() throws IOException {
        String q = "{\n"
                + "  \"type\" : \"/location/country\",\n"
                + "  \"id\" : \"/en/united_kingdom\",\n"
                + "  \"!/people/person/nationality\" : {\"return\":\"count\"}\n"
                + "}";
        runQuery(q);
    }

    @Test()
    public void testCountCountryNationals2() throws IOException {
        String q = "{\n"
                + "  \"type\" : \"/people/person\",\n"
                + "  \"nationality\" : {\"id\" : \"/en/united_kingdom\"},\n"
                + "  \"return\":\"count\" \n"
                + "}";
        runQuery(q);
    }

    @Test()
    @Ignore("Causes 503 Baackend error. >_<")
    public void testCountTopics() throws IOException {
        String q = "{\n"
                + "  \"type\" : \"/common/topic\",\n"
                + "  \"return\":\"count\" \n"
                + "}";
        runQuery(q);
    }

    @Test()
    public void testEstimateCountTopics() throws IOException {
        String q = "{\n"
                + "  \"type\" : \"/common/topic\",\n"
                + "  \"return\":\"estimate-count\" \n"
                + "}";
        runQuery(q);
    }

    @Test()
    public void testFindLongestTrack() throws IOException {
        String q = "{\n"
                + "  \"type\":\"/music/album\",\n"
                + "  \"name\":\"Synchronicity\",\n"
                + "  \"artist\":\"The Police\",\n"
                + "  \"primary_release\" : { \n"
                + "     \"track\": {\n"
                + "         \"name\":null,\n"
                + "         \"length\":null,\n"
                + "         \"sort\":\"-length\",\n"
                + "         \"limit\":1 \n"
                + "     } \n"
                + "  } \n"
                + "} \n";
        runQuery(q);
    }

    @Test()
    public void testFindLongestTrack2() throws IOException {
        String q = "[{\n"
                + "  \"type\":\"/music/track\",\n"
                + "  \"artist\":\"The Police\",\n"
                + "  \"name\":null,\n"
                + "  \"length\":null,\n"
                + "  \"sort\":[\"-length\",\"name\"], \n"
                + "  \"limit\":10 \n"
                + "}]";
        runQuery(q);
    }

    @Test()
    public void startingActorsOfPsycho() throws IOException {
        String q = "[{\n"
                + "  \"type\" : \"/film/film\",\n"
                + "  \"name\" : \"Psycho\",\n"
                + "  \"directed_by\":\"Alfred Hitchcock\",\n"
                + "  \"starring\" : [{\n"
                + "    \"actor\" : null,\n"
                + "    \"character\" : null,\n"
                + "    \"index\" : null,\n"
                + "    \"sort\" : \"index\",\n"
                + "    \"limit\" : 2\n"
                + "  }]\n"
                + "}]\n";
        runQuery(q);
    }

    @Test()
    public void testOptional() throws IOException {
        String q = "[{\n"
                + "  \"type\" : \"/music/artist\",\n"
                + "  \"name\" : null,\n"
                + "  \"track\" : \"Masters of War\", \n"
                + "  \"album\" : [{\n"
                + "    \"name\" : \"Greatest Hits\",\n"
                + "    \"optional\" : \"optional\"\n"
                + "  }]\n"
                + "}]\n";
        runQuery(q);
    }

    @Test()
    public void testForbidden() throws IOException {
        String q = "[{\n"
                + "  \"type\" : \"/music/artist\",\n"
                + "  \"name\" : null,\n"
                + "  \"track\" : \"Masters of War\",\n"
                + "  \"album\" : {\n"
                + "    \"name\" : \"Greatest Hits\",\n"
                + "    \"optional\" : \"forbidden\"\n"
                + "  }\n"
                + "}]\n";
        runQuery(q);
    }

    @Test()
    public void testDateRange() throws IOException {
        String q = "[{\n"
                + "   \"type\":\"/music/album\",\n"
                + "   \"name\":null,\n"
                + "   \"artist\":null,\n"
                + "   \"release_date>=\":\"1999-01-01\",\n"
                + "   \"release_date<=\":\"1999-01-31\", \n"
                + "   \"return\" : \"count\""
                + "}]";
        runQuery(q);
    }

    @Test()
    public void testPatterns() throws IOException {
        String q = "[{\n"
                + "  \"type\" : \"/music/artist\",\n"
                + "  \"name\" : null,\n"
                + "  \"name~=\" : \"^The * *s$\"\n"
                + "}]";
        runQuery(q);
    }
    //
    // =================================================================
    //  Batch queries
    // =================================================================
    //

    @Test
    public void testBatchSearch() throws IOException {

        BatchCallback<SearchFormat.EntityResult, Void> callback =
                new BatchCallback<SearchFormat.EntityResult, Void>() {
                    public void onSuccess(SearchFormat.EntityResult t,
                                          GoogleHeaders responseHeaders) {
                        System.out.println(t.toPrettyString());
                    }

                    public void onFailure(Void e,
                                          GoogleHeaders responseHeaders)
                            throws IOException {
                        throw new UnsupportedOperationException(
                                "Not supported yet.");
                    }
                };

        String[] words = new String[]{"trees", "flowers", "72368764",
            "Anchovies", "brighton", "cheese burger", "lol", "shplah",
            "java", "fire"};

        BatchRequest batch = freebase.batch();

        for (String word : words) {
            batch.queue(freebase.search(word).buildHttpRequest(),
                        SearchFormat.EntityResult.class,
                        Void.TYPE, callback);
        }
        batch.execute();


    }

    @Test
    public void testBatchGetText() throws IOException {

        BatchCallback<ContentserviceGet, Void> callback =
                new BatchCallback<ContentserviceGet, Void>() {
                    public void onSuccess(ContentserviceGet t,
                                          GoogleHeaders responseHeaders) {
                        System.out.println(t.getResult() + "\n\n");
                    }

                    public void onFailure(Void e,
                                          GoogleHeaders responseHeaders)
                            throws IOException {
                        throw new UnsupportedOperationException(
                                "Not supported yet.");
                    }
                };

        String[] ids = new String[]{"/en/tree", "/en/flower", "/en/anchovy",
            "/en/brighton", "/en/lol",
            "/wikipedia/pt/Java_$0028linguagem_de_programa$00E7$00E3o$0029",
            "/en/firefighter"};

        BatchRequest batch = freebase.batch();

        for (String word : ids) {
            batch.queue(freebase.text().get(Arrays.asList(word))
                    .buildHttpRequest(),
                        ContentserviceGet.class,
                        Void.TYPE, callback);
        }
        batch.execute();


    }

    //
    // =================================================================
    //  Utilities
    // =================================================================
    //
    static String runQuery(String query) throws IOException {
        try {
            System.out.println("Query: " + query);

            Freebase.Mqlread mlr = freebase.mqlread(query);

            InputStream is = mlr.executeAsInputStream();
            String result = IOUtils.toString(is);
            IOUtils.closeQuietly(is);

            junit.framework.Assert.assertTrue(result != null);
            junit.framework.Assert.assertTrue(!result.isEmpty());

            System.out.println("Result: " + result);
            return result;
        } catch (GoogleJsonResponseException ex) {
            System.out.println("Exception: " + ex.getMessage());
            throw ex;
        }
    }

    static String toJson(Object o) throws IOException {
        StringWriter writer = new StringWriter();
        JsonGenerator gen = jsonFactory.createJsonGenerator(writer);
        gen.enablePrettyPrint();
        gen.serialize(o);
        gen.flush();
        return writer.toString();
    }
}
