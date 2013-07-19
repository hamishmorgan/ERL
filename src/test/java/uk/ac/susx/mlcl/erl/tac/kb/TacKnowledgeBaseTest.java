/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.tac.kb;

import org.junit.Ignore;
import org.junit.Test;
import uk.ac.susx.mlcl.erl.tac.io.Tac2009KnowledgeBaseIO;
import uk.ac.susx.mlcl.erl.test.AbstractTest;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author hiam20
 */
public class TacKnowledgeBaseTest extends AbstractTest {


    /**
     * Test of open method, of class TacKnowledgeBase.
     */
    @Test
    public void testOpen() {
        File dbFile = getResourceAsFile("tac09-kb-sample.mapdb");
        TacKnowledgeBase kb = TacKnowledgeBase.open(dbFile);
        kb.close();
    }

    /**
     * Test of getEntityById method, of class TacKnowledgeBase.
     */
    @Test
    public void testGetEntityById() throws IOException {

        String id = "E0000051";

        File dbFile = getResourceAsFile("tac09-kb-sample.mapdb");
        TacKnowledgeBase instance = TacKnowledgeBase.open(dbFile);

        Entity result = instance.getEntityById(id);

        assertEquals(id, result.getId());

        instance.close();
    }

    /**
     * Test of getEntityByName method, of class TacKnowledgeBase.
     */
    @Test
    public void testGetEntityByName() throws IOException {
        String name = "Panama";

        File dbFile = getResourceAsFile("tac09-kb-sample.mapdb");

        TacKnowledgeBase instance = TacKnowledgeBase.open(dbFile);
        Entity result = instance.getEntityByName(name);

        assertEquals(name, result.getName());
    }

    /**
     * Test of getEntityByName method, of class TacKnowledgeBase. This time using a name containing
     * accented characters.
     */
    @Test
    @Ignore(value = "Accent c18n is not currently supported.")
    public void testGetEntityByName2() throws IOException {
        String name = "Panamá";

        File dbFile = getResourceAsFile("tac09-kb-sample.mapdb");

        TacKnowledgeBase instance = TacKnowledgeBase.open(dbFile);
        Entity result = instance.getEntityByName(name);

        assertNotNull(result);
        assertEquals(name, result.getName());
    }

    /**
     * Test of getNameById method, of class TacKnowledgeBase.
     */
    @Test
    public void testGetNameById() throws IOException {
        String name = "Hardtner, Kansas";
        File dbFile = getResourceAsFile("tac09-kb-sample.mapdb");
        TacKnowledgeBase instance = TacKnowledgeBase.open(dbFile);
        String expResult = "E0000053";
        String result = instance.getNameById(name);
        assertEquals(expResult, result);
    }

    /**
     * Test of getTextForId method, of class TacKnowledgeBase.
     */
    @Test
    public void testGetTextForId() throws IOException {
        String id = "E0000075";
        File dbFile = getResourceAsFile("tac09-kb-sample.mapdb");
        TacKnowledgeBase instance = TacKnowledgeBase.open(dbFile);
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
     * Test of close method, of class TacKnowledgeBase.
     */
    @Test
    public void testClose() {
        File dbFile = getResourceAsFile("tac09-kb-sample.mapdb");
        TacKnowledgeBase instance = TacKnowledgeBase.open(dbFile);
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
        File dbFile = getResourceAsFile("tac09-kb-sample.mapdb");
        TacKnowledgeBase instance = TacKnowledgeBase.open(dbFile);
        int actualSize = instance.size();

        assertEquals(133, actualSize);
    }

    @Test
    public void testIsEmpty() throws IOException {
        final File dbFile = getResourceAsFile("tac09-kb-sample.mapdb");
        TacKnowledgeBase instance = TacKnowledgeBase.open(dbFile);

        assertFalse("Expected db to be non-empty but found that it was empty.", instance.isEmpty());
    }

    @Test
    public void testIterator() throws IOException {
        File dbFile = getResourceAsFile("tac09-kb-sample.mapdb");
        TacKnowledgeBase instance = TacKnowledgeBase.open(dbFile);

        System.out.print("Entities: ");
        for (Entity entity : instance) {
            System.out.print(entity.getId() + ", ");
        }
        System.out.println();
    }
}
