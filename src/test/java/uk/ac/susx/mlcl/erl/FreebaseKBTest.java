/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.Data;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 *
 * @author hiam20
 */
public class FreebaseKBTest {

    private static TestName name;
    private static JsonFactory jsonFactory;

    public FreebaseKBTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        name = new TestName();
        jsonFactory = new JacksonFactory();
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

    @Test()
    public void testCursor() throws IOException {
        String q = "{\n"
                + "  \"cursor\":true,\n"
                + "  \"query\": [{ \n"
                + "    \"type\" : \"/music/artist\",\n"
                + "    \"name\" : null,\n"
                + "    \"limit\" : 10 \n"
                + "  }]\n"
                + "}\n";
        runQuery(q);
    }
//    {
//  "cursor":true,
//  "query": [{
//    "type":"/music/album",
//    "artist":"The Police",
//    "name":null,
//    "limit":10
//  }]
//}

    static String runQuery(String query) throws IOException {
        try {
            System.out.println("Query: " + query);
            FreebaseKB kb = new FreebaseKB();
            kb.init();

            String result = kb.rawMQLQuery(query);
            Assert.assertTrue(result != null);
            Assert.assertTrue(!result.isEmpty());

            System.out.println("Result: " + result);
            return result;
        } catch (GoogleJsonResponseException ex) {
            System.out.println("Exception: " + ex.getMessage());
            throw ex;
        }
    }
//    static String formatSimpleQuery(String... strings) throws IOException {
//        StringWriter writer = new StringWriter();
//        JsonGenerator gen = jsonFactory.createJsonGenerator(writer);
//
//        gen.writeStartObject();
//
//        for (int i = 0; i < strings.length; i += 2) {
//            gen.writeFieldName(strings[i]);
//            if (strings[i + 1] == null) {
//                gen.writeNull();
//            } else {
//                gen.writeString(strings[i + 1]);
//            }
//
//        }
//
//        gen.writeEndObject();
//        gen.flush();
//        return writer.toString();
//    }
//    
//    /**
//     * Test of init method, of class FreebaseKB.
//     */
//    @Test
//    public void testInit() throws Exception {
//        System.out.println("init");
//        FreebaseKB instance = new FreebaseKB();
//        instance.init();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getText method, of class FreebaseKB.
//     */
//    @Test
//    public void testGetText() throws Exception {
//        System.out.println("getText");
//        String id = "";
//        FreebaseKB instance = new FreebaseKB();
//        String expResult = "";
//        String result = instance.getText(id);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of buildMQLQuery method, of class FreebaseKB.
//     */
//    @Test
//    public void testBuildMQLQuery() throws Exception {
//        System.out.println("buildMQLQuery");
//        String id = "";
//        String name = "";
//        String type = "";
//        FreebaseKB instance = new FreebaseKB();
//        String expResult = "";
//        String result = instance.buildMQLQuery(id, name, type);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of rawMQLQuery method, of class FreebaseKB.
//     */
//    @Test
//    public void testRawMQLQuery() throws Exception {
//        System.out.println("rawMQLQuery");
//        String query = "";
//        FreebaseKB instance = new FreebaseKB();
//        String expResult = "";
//        String result = instance.rawMQLQuery(query);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getInstanceOfType method, of class FreebaseKB.
//     */
//    @Test
//    public void testGetInstanceOfType() throws Exception {
//        System.out.println("getInstanceOfType");
//        String type = "";
//        FreebaseKB instance = new FreebaseKB();
//        instance.getInstanceOfType(type);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of foo method, of class FreebaseKB.
//     */
//    @Test
//    public void testFoo() throws Exception {
//        System.out.println("foo");
//        FreebaseKB instance = new FreebaseKB();
//        instance.foo();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of loadGoogleApiKey method, of class FreebaseKB.
//     */
//    @Test
//    public void testLoadGoogleApiKey() throws Exception {
//        System.out.println("loadGoogleApiKey");
//        String expResult = "";
//        String result = FreebaseKB.loadGoogleApiKey();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
