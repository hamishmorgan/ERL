/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.t9kb;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;

import static org.junit.Assert.*;
import org.xml.sax.SAXException;
import uk.ac.susx.mlcl.erl.test.AbstractTest;

/**
 *
 * @author hiam20
 */
public class T9KnowledgeBaseTest extends AbstractTest {

    private static final File TEST_DATA_DIR = new File("src/test/data");
    private static final File TEST_OUTPUT_DIR = new File("target/testout");

    public T9KnowledgeBaseTest() {
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        if (!TEST_OUTPUT_DIR.exists() && !TEST_OUTPUT_DIR.mkdirs()) {
            throw new IOException("Failed to create test output dir: " + TEST_OUTPUT_DIR);
        }
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    private void deleteMapDBIfExists(File path) throws IOException {

        File path_p = new File(path.getParentFile(), path.getName() + ".p");
        File path_t = new File(path.getParentFile(), path.getName() + ".t");

        if (path.exists() && !path.delete()) {
            throw new IOException("Failed to delete MapDB file: " + path);
        }
        if (path_p.exists() && !path_p.delete()) {
            throw new IOException("Failed to delete MapDB file: " + path_p);
        }
        if (path_t.exists() && !path_t.delete()) {
            throw new IOException("Failed to delete MapDB file: " + path_t);
        }
    }

    @Test
    public void testCreate() throws ParserConfigurationException, SAXException, IOException {
        System.out.println("testCreate");


        File xmlFile = new File(TEST_DATA_DIR, "tac09-kb-sample.xml");
        File dbFile = new File(TEST_OUTPUT_DIR, "tac09-kb-sample.mapdb");

        deleteMapDBIfExists(dbFile);

        T9KnowledgeBase kb = T9KnowledgeBase.create(dbFile, xmlFile);


        assertFalse(kb.isEmpty());

        kb.close();



//        // Tristan Nitot
//        T9Entity ent = kb.getEntityById("E0420900");
//        System.out.println(ent);



    }

    /**
     * Test of open method, of class T9KnowledgeBase.
     */
    @Test
    public void testOpen() {
        System.out.println("testOpen");

        File dbFile = new File(TEST_DATA_DIR, "tac09-kb-sample.mapdb");
        T9KnowledgeBase kb = T9KnowledgeBase.open(dbFile);
        kb.close();

    }

    /**
     * Test of getEntityById method, of class T9KnowledgeBase.
     */
    @Test
    public void testGetEntityById() throws IOException {
        System.out.println("getEntityById");


        String id = "E0000051";

        File dbFile = new File(TEST_DATA_DIR, "tac09-kb-sample.mapdb");
        T9KnowledgeBase instance = T9KnowledgeBase.open(dbFile);

        T9Entity result = instance.getEntityById(id);

        assertEquals(id, result.getId());

        instance.close();
    }

    /**
     * Test of getEntityByName method, of class T9KnowledgeBase.
     */
    @Test
    public void testGetEntityByName() throws IOException {
        System.out.println("getEntityByName");
        String name = "Panama";

        File dbFile = new File(TEST_DATA_DIR, "tac09-kb-sample.mapdb");

        T9KnowledgeBase instance = T9KnowledgeBase.open(dbFile);
        T9Entity result = instance.getEntityByName(name);

        assertEquals(name, result.getName());
    }

    /**
     * Test of getEntityByName method, of class T9KnowledgeBase. This time using a name containing
     * accented characters.
     */
    @Test
    @Ignore(value = "Accent c18n is not currently supported.")
    public void testGetEntityByName2() throws IOException {
        System.out.println("getEntityByName");
        String name = "Panamá";

        File dbFile = new File(TEST_DATA_DIR, "tac09-kb-sample.mapdb");

        T9KnowledgeBase instance = T9KnowledgeBase.open(dbFile);
        T9Entity result = instance.getEntityByName(name);

        assertNotNull(result);
        assertEquals(name, result.getName());
    }

    /**
     * Test of getNameById method, of class T9KnowledgeBase.
     */
    @Test
    public void testGetNameById() throws IOException {
        System.out.println("getNameById");
        String name = "Hardtner, Kansas";
        File dbFile = new File(TEST_DATA_DIR, "tac09-kb-sample.mapdb");
        T9KnowledgeBase instance = T9KnowledgeBase.open(dbFile);
        String expResult = "E0000053";
        String result = instance.getNameById(name);
        assertEquals(expResult, result);
    }

    /**
     * Test of getTextForId method, of class T9KnowledgeBase.
     */
    @Test
    public void testGetTextForId() throws IOException {
        System.out.println("getTextForId");
        String id = "E0000075";
        File dbFile = new File(TEST_DATA_DIR, "tac09-kb-sample.mapdb");
        T9KnowledgeBase instance = T9KnowledgeBase.open(dbFile);
        String expResult =
                "Sepulga River\n"
                + "\n"
                + "Sepulga River is a river in the south-central region of the state of Alabama,\n"
                + "United States. It terminates at its confluence with the Conecuh River near the\n"
                + "northwest border of the Conecuh National Forest.\n"
                + "\n"
                + "Coordinates needed:\n"
                + "";
        String result = instance.getTextForId(id);
//        
//        System.out.println(expResult);
//        System.out.println(result);
        assertEquals(expResult, result);
    }

    /**
     * Test of close method, of class T9KnowledgeBase.
     */
    @Test
    public void testClose() {
        System.out.println("close");
        File dbFile = new File(TEST_DATA_DIR, "tac09-kb-sample.mapdb");
        T9KnowledgeBase instance = T9KnowledgeBase.open(dbFile);
        instance.close();

        try {
            instance.getEntityById("E0000075");
            fail();
        } catch (IOException e) {
            System.out.println("XXX: " + e);
        }
    }

    @Test
    public void testSize() throws IOException {
        System.out.println("size");
        File dbFile = new File(TEST_DATA_DIR, "tac09-kb-sample.mapdb");
        T9KnowledgeBase instance = T9KnowledgeBase.open(dbFile);
        int actualSize = instance.size();

        assertEquals(133, actualSize);
    }

    @Test
    public void testIsEmpty() throws IOException {
        System.out.println("size");
        final File dbFile = new File(TEST_DATA_DIR, "tac09-kb-sample.mapdb");
        T9KnowledgeBase instance = T9KnowledgeBase.open(dbFile);

        assertFalse("Expected db to be non-empty but found that it was empty.", instance.isEmpty());
    }

    @Test
    public void testIterator() throws IOException {
        System.out.println("size");
        File dbFile = new File(TEST_DATA_DIR, "tac09-kb-sample.mapdb");
        T9KnowledgeBase instance = T9KnowledgeBase.open(dbFile);
        
        System.out.print("Entities: ");
        for(T9Entity entity : instance) {
            System.out.print(entity.getId() + ", ");
        }
        System.out.println();
    }
}
