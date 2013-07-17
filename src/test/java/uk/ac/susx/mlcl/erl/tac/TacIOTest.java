package uk.ac.susx.mlcl.erl.tac;

import com.google.common.collect.ImmutableSet;
import nu.xom.ParsingException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.ac.susx.mlcl.erl.test.AbstractTest;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;


/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 16/07/2013
 * Time: 14:14
 * To change this template use File | Settings | File Templates.
 */
public class TacIOTest extends AbstractTest {

    private static final File DATA_DIR = new File("/Volumes/LocalScratchHD/LocalHome/Projects/NamedEntityLinking/Data");

//    private static List<Query> readQueries(TacIO.Format format, File queriesFileName) throws ParsingException, IOException {
//        System.out.println(queriesFileName);
//        final List<Query> result = format.readQueries(queriesFileName);
//        assertNotNull(result);
//        assertFalse(result.isEmpty());
//        assertThat(ImmutableSet.copyOf(result).size(), is(equalTo(result.size())));
//        return result;
//    }
//
//    private static List readLinks(TacIO.Format format, File linksFile) throws ParsingException, IOException {
//        System.out.println(linksFile);
//        final List links = format.readLinks(linksFile);
//        assertNotNull(links);
//        assertFalse(links.isEmpty());
//        assertThat(ImmutableSet.copyOf(links).size(), is(equalTo(links.size())));
//        return links;
//    }

    @Test
    public void testReadQueries_Tac2012Evaluation() throws ParsingException, IOException {

        final File queriesFile = new File(DATA_DIR, "tac_2012_kbp_english_evaluation_entity_linking_queries.xml");
        final List<Query> result = new TacIO.Tac2012QueryIO().readAll(queriesFile);
        assertThat(result.size(), is(equalTo(2226)));
    }

    @Test
    public void testReadQueries_Tac2009Evaluation() throws ParsingException, IOException {

        final File queriesFile = new File(DATA_DIR, "tac_2009_kbp_entity_linking_queries.xml");
        final List<Query> result = new TacIO.Tac2009QueryIO().readAll(queriesFile);
        assertThat(result.size(), is(equalTo(3904)));
    }

    @Test
    public void testReadQueries_Tac2010Evaluation() throws ParsingException, IOException {

        final File queriesFile = new File(DATA_DIR, "tac_2010_kbp_evaluation_entity_linking_queries.xml");
        final List<Query> result = new TacIO.Tac2010QueryIO().readAll(queriesFile);
        assertThat(result.size(), is(equalTo(2250)));
    }

    @Test
    public void testReadQueries_Tac2010Training() throws ParsingException, IOException {

        final File queriesFile = new File(DATA_DIR, "tac_2010_kbp_training_entity_linking_queries.xml");
        final List<Query> result = new TacIO.Tac2010QueryIO().readAll(queriesFile);
        assertThat(result.size(), is(equalTo(1500)));
    }

    @Test
    public void testReadLinks_Tac2009Evaluation() throws ParsingException, IOException {
        final File linksFile = new File(DATA_DIR, "tac_2009_kbp_entity_linking_query_types.tab");
        final List<Link> links = new TacIO.Tac2009LinkIO().readAll(linksFile);
        assertThat(links.size(), is(equalTo(3904)));
    }

    @Test
    public void testReadLinks_Tac2010Evaluation() throws ParsingException, IOException {
        final File linksFile = new File(DATA_DIR, "tac_2010_kbp_evaluation_entity_linking_query_types.tab");
        final List<Link> links = new TacIO.Tac2010LinkIO().readAll(linksFile);
        assertThat(links.size(), is(equalTo(2250)));
    }

    @Test
    public void testReadLinks_Tac2010Training() throws ParsingException, IOException {
        final File linksFile = new File(DATA_DIR, "tac_2010_kbp_training_entity_linking_query_types.tab");
        // Note the format is 2009 for these data!
        final List<Link> links = new TacIO.Tac2009LinkIO().readAll(linksFile);
        assertThat(links.size(), is(equalTo(1500)));
    }

    @Test
    public void testReadLinks_Tac2012Evaluation() throws ParsingException, IOException {
        final File linksFile = new File(DATA_DIR, "tac_2012_kbp_english_evaluation_entity_linking_query_types.tab");
        final List<Link> links = new TacIO.Tac2012LinkIO().readAll(linksFile);
        assertThat(links.size(), is(equalTo(2226)));
    }

    @Test
    public void testDetectFormatFromLinks_Tac2009Evaluation() throws ParsingException, IOException {
        final File linksFile = new File(DATA_DIR, "tac_2009_kbp_entity_linking_query_types.tab");
        assertEquals(TacIO.Tac2009LinkIO.class, TacIO.detectFormatFromLinks(linksFile).getClass());
    }

    @Test
    public void testDetectFormatFromLinks_Tac2010Evaluation() throws ParsingException, IOException {
        final File linksFile = new File(DATA_DIR, "tac_2010_kbp_evaluation_entity_linking_query_types.tab");
        assertEquals(TacIO.Tac2010LinkIO.class, TacIO.detectFormatFromLinks(linksFile).getClass());
    }

    @Test
    public void testDetectFormatFromLinks_Tac2010Training() throws ParsingException, IOException {
        final File linksFile = new File(DATA_DIR, "tac_2010_kbp_training_entity_linking_query_types.tab");
        // Note:  the format is 2009 for this file
        assertEquals(TacIO.Tac2009LinkIO.class, TacIO.detectFormatFromLinks(linksFile).getClass());
    }

    @Test
    public void testDetectFormatFromLinks_Tac2012Evaluation() throws ParsingException, IOException {
        final File linksFile = new File(DATA_DIR, "tac_2012_kbp_english_evaluation_entity_linking_query_types.tab");
        assertEquals(TacIO.Tac2012LinkIO.class, TacIO.detectFormatFromLinks(linksFile).getClass());
    }

    @Test
    public void testDetectFormatFromQueries_Tac2012Evaluation() throws ParsingException, IOException {
        final File queriesFile = new File(DATA_DIR, "tac_2012_kbp_english_evaluation_entity_linking_queries.xml");
        assertEquals(TacIO.Tac2012QueryIO.class, TacIO.detectFormatFromQueries(queriesFile).getClass());
    }

    @Test
    public void testDetectFormatFromQueries_Tac2009Evaluation() throws ParsingException, IOException {
        final File queriesFile = new File(DATA_DIR, "tac_2009_kbp_entity_linking_queries.xml");
        assertEquals(TacIO.Tac2009QueryIO.class, TacIO.detectFormatFromQueries(queriesFile).getClass());
    }

    @Test
    public void testDetectFormatFromQueries_Tac2010Evaluation() throws ParsingException, IOException {
        final File queriesFile = new File(DATA_DIR, "tac_2010_kbp_evaluation_entity_linking_queries.xml");
        assertEquals(TacIO.Tac2010QueryIO.class, TacIO.detectFormatFromQueries(queriesFile).getClass());
    }

    @Test
    public void testDetectFormatFromQueries_Tac2010Training() throws ParsingException, IOException {
        final File queriesFile = new File(DATA_DIR, "tac_2010_kbp_training_entity_linking_queries.xml");
        assertEquals(TacIO.Tac2010QueryIO.class, TacIO.detectFormatFromQueries(queriesFile).getClass());
    }

}