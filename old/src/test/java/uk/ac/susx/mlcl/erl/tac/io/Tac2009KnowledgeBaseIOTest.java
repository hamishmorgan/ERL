/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.tac.io;

import org.junit.Test;
import org.xml.sax.SAXException;
import uk.ac.susx.mlcl.erl.tac.kb.TacKnowledgeBase;
import uk.ac.susx.mlcl.erl.test.AbstractTest;

import javax.annotation.Nonnull;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;

/**
 * @author hiam20
 */
public class Tac2009KnowledgeBaseIOTest extends AbstractTest {


    private void deleteMapDBIfExists(@Nonnull File path) throws IOException {

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

        File xmlFile = getResourceAsFile("tac09-kb-sample.xml");
        File dbFile = newTempFile();

        deleteMapDBIfExists(dbFile);

        TacKnowledgeBase kb = Tac2009KnowledgeBaseIO.create(dbFile, xmlFile);

        assertFalse(kb.isEmpty());

        kb.close();

    }

}
