/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.tac.kb;

import com.google.common.io.Closer;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import uk.ac.susx.mlcl.erl.test.AbstractTest;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author hiam20
 */
public class TacKnowledgeBaseTest extends AbstractTest {

    private final String dbResourceName;
    private final Closer closer = Closer.create();

    protected TacKnowledgeBaseTest(String dbResourceName) {
        this.dbResourceName = dbResourceName;
    }

    public TacKnowledgeBaseTest() {
        this("tac09-kb-sample.mapdb");
    }

    @After
    public void closeDatabase() throws IOException {
        try {
            closer.close();
        } catch (Throwable t) {
            // swallow
        }
    }

    TacKnowledgeBase getInstance() {
        final File dbFile = getResourceAsFile(dbResourceName);
        TacKnowledgeBase instance = TacKnowledgeBase.open(dbFile);
        closer.register(instance);
        return instance;
    }

    /**
     * Test of open method, of class TacKnowledgeBase.
     */
    @Test
    public void testOpen() {
        final TacKnowledgeBase instance = getInstance();
        assertNotNull(instance);
    }

    /**
     * Test of getEntityById method, of class TacKnowledgeBase.
     */
    @Test
    public void testGetEntityById() throws IOException {
        final String entityId = "E0000051";
        final TacKnowledgeBase instance = getInstance();
        final Entity result = instance.getEntityById(entityId);
        assertEquals(entityId, result.getId());
    }

    /**
     * Test of getEntityByName method, of class TacKnowledgeBase.
     */
    @Test
    public void testGetEntityByName() throws IOException {
        final String name = "Panama";
        final TacKnowledgeBase instance = getInstance();
        final Entity result = instance.getEntityByName(name);
        assertEquals(name, result.getName());
    }

    /**
     * Test of getEntityByName method, of class TacKnowledgeBase. This time using a name containing
     * accented characters.
     */
    @Test
    @Ignore(value = "Accent c18n is not currently supported.")
    public void testGetEntityByName2() throws IOException {
        final String name = "Panamá";
        final TacKnowledgeBase instance = getInstance();
        final Entity result = instance.getEntityByName(name);
        assertNotNull(result);
        assertEquals(name, result.getName());
    }

    /**
     * Test of getNameById method, of class TacKnowledgeBase.
     */
    @Test
    public void testGetNameById() throws IOException {
        final String name = "Hardtner, Kansas";
        final TacKnowledgeBase instance = getInstance();
        final String expResult = "E0000053";
        final String result = instance.getNameById(name);
        assertEquals(expResult, result);
    }

    /**
     * Test of getTextForId method, of class TacKnowledgeBase.
     */
    @Test
    public void testGetTextForId() throws IOException {
        final String id = "E0000075";
        final TacKnowledgeBase instance = getInstance();
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
        assertEquals(expResult, result);
    }

    /**
     * Test of close method, of class TacKnowledgeBase.
     */
    @Test
    public void testClose() {
        final TacKnowledgeBase instance = getInstance();
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
        final TacKnowledgeBase instance = getInstance();
        int actualSize = instance.size();
        assertEquals(133, actualSize);
    }

    @Test
    public void testIsEmpty() throws IOException {
        final TacKnowledgeBase instance = getInstance();
        assertFalse("Expected db to be non-empty but found that it was empty.", instance.isEmpty());
    }

    @Test
    public void testIterator() throws IOException {
        final TacKnowledgeBase instance = getInstance();
        System.out.print("Entities: ");
        for (Entity entity : instance) {
            System.out.print(entity.getId() + ", ");
        }
        System.out.println();
    }
}
