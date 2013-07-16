package uk.ac.susx.mlcl.erl.tac;

import nu.xom.ParsingException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 16/07/2013
 * Time: 14:14
 * To change this template use File | Settings | File Templates.
 */
public class MainTest {

    private static final File DATA_DIR = new File("/Volumes/LocalScratchHD/LocalHome/Projects/NamedEntityLinking/Data");
//    final String[] queryFileNames = {"tac_2009_kbp_entity_linking_queries.xml",
//            "tac_2010_kbp_evaluation_entity_linking_queries.xml",
//            "tac_2012_kbp_english_evaluation_entity_linking_queries.xml"};
//
//    tac_2009_kbp_entity_linking_queries.xml
//    tac_2010_kbp_evaluation_entity_linking_queries.xml
//    tac_2010_kbp_training_entity_linking_queries.xml
//    tac_2012_kbp_english_evaluation_entity_linking_queries.xml

    @Test
    public void testTac2012Queries() throws ParsingException, IOException {

        final File queriesFile = new File(DATA_DIR, "tac_2012_kbp_english_evaluation_entity_linking_queries.xml");

        System.out.println(queriesFile);

        final List<Query> query = Main.readQueries(queriesFile);


    }

    @Test
    public void testTac2009Queries() throws ParsingException, IOException {

        final File queriesFile = new File(DATA_DIR, "tac_2009_kbp_entity_linking_queries.xml");

        System.out.println(queriesFile);

        final List<Query> query = Main.readQueries(queriesFile);


    }


    @Test
    public void testTac2010EvaluationQueries() throws ParsingException, IOException {

        final File queriesFile = new File(DATA_DIR, "tac_2010_kbp_evaluation_entity_linking_queries.xml");

        System.out.println(queriesFile);

        final List<Query> query = Main.readQueries(queriesFile);


    }


    @Test
    public void testTac20010TrainingQueries() throws ParsingException, IOException {

        final File queriesFile = new File(DATA_DIR, "tac_2010_kbp_training_entity_linking_queries.xml");

        System.out.println(queriesFile);

        final List<Query> query = Main.readQueries(queriesFile);


    }


}
