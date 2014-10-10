package uk.ac.susx.mlcl.erl.tac.cmd;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.xml.sax.SAXException;
import uk.ac.susx.mlcl.erl.tac.io.Tac2009KnowledgeBaseIO;
import uk.ac.susx.mlcl.erl.tac.kb.TacKnowledgeBase;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * A tools for converting a tac Knowledge base (in XML format) into a MapDB database.
 *
 * @author Hamish Morgan
 */
public class ConvertKB implements Runnable {

    @Parameter(names={"-i"}, required = true, description = "Input file / directory")
    File srcXmlFiles;
    @Parameter(names={"-o"}, required = true, description = "Output MapDB file")
    File dstDatabaseFile;



    public static void main(String[] args) {

        ConvertKB instance = new ConvertKB();

        final JCommander jc = new JCommander();
        jc.setProgramName("nel");
        jc.addObject(instance);

        jc.parse(args);

        instance.run();

    }

    @Override
    public void run() {
        try {


            TacKnowledgeBase kb = Tac2009KnowledgeBaseIO.create(dstDatabaseFile, srcXmlFiles);
            kb.close();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
