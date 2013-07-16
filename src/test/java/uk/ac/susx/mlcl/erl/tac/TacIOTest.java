package uk.ac.susx.mlcl.erl.tac;

import com.google.common.collect.ImmutableSet;
import nu.xom.ParsingException;
import org.junit.Test;
import uk.ac.susx.mlcl.erl.test.AbstractTest;

import java.io.File;
import java.io.IOException;
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

    private static List<Query> readQueries(TacIO.Format format, File queriesFile) throws ParsingException, IOException {
        System.out.println(queriesFile);
        final List<Query> result = format.readQueries(queriesFile);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertThat(ImmutableSet.copyOf(result).size(), is(equalTo(result.size())));
        return result;
    }

    private static List readLinks(TacIO.Format format, File linksFile) throws ParsingException, IOException {
        System.out.println(linksFile);
        final List links = format.readLinks(linksFile);
        assertNotNull(links);
        assertFalse(links.isEmpty());
        assertThat(ImmutableSet.copyOf(links).size(), is(equalTo(links.size())));
        return links;
    }

    @Test
    public void testReadQueries_Tac2012Evaluation() throws ParsingException, IOException {

        final File queriesFile = new File(DATA_DIR, "tac_2012_kbp_english_evaluation_entity_linking_queries.xml");
        final List<Query> result = readQueries(TacIO.Format.TAC2012, queriesFile);
        assertThat(result.size(), is(equalTo(2226)));
    }

    @Test
    public void testReadQueries_Tac2009Evaluation() throws ParsingException, IOException {

        final File queriesFile = new File(DATA_DIR, "tac_2009_kbp_entity_linking_queries.xml");
        final List<Query> result = readQueries(TacIO.Format.TAC2009, queriesFile);
        assertThat(result.size(), is(equalTo(3904)));
    }

    @Test
    public void testReadQueries_Tac2010Evaluation() throws ParsingException, IOException {

        final File queriesFile = new File(DATA_DIR, "tac_2010_kbp_evaluation_entity_linking_queries.xml");
        final List<Query> result = readQueries(TacIO.Format.TAC2010, queriesFile);
        assertThat(result.size(), is(equalTo(2250)));
    }

    @Test
    public void testReadQueries_Tac2010Training() throws ParsingException, IOException {

        final File queriesFile = new File(DATA_DIR, "tac_2010_kbp_training_entity_linking_queries.xml");
        final List<Query> result = readQueries(TacIO.Format.TAC2010, queriesFile);
        assertThat(result.size(), is(equalTo(1500)));
    }

    @Test
    public void testReadLinks_Tac2009Evaluation() throws ParsingException, IOException {
        final File linksFile = new File(DATA_DIR, "tac_2009_kbp_entity_linking_query_types.tab");
        final List links = readLinks(TacIO.Format.TAC2009, linksFile);
        assertThat(links.size(), is(equalTo(3904)));
    }

    @Test
    public void testReadLinks_Tac2010Evaluation() throws ParsingException, IOException {
        final File linksFile = new File(DATA_DIR, "tac_2010_kbp_evaluation_entity_linking_query_types.tab");
        final List links = readLinks(TacIO.Format.TAC2010, linksFile);
        assertThat(links.size(), is(equalTo(2250)));
    }

    @Test
    public void testReadLinks_Tac2010Training() throws ParsingException, IOException {
        final File linksFile = new File(DATA_DIR, "tac_2010_kbp_training_entity_linking_query_types.tab");
        // Note the format is 2009 for these data!
        final List links = readLinks(TacIO.Format.TAC2009, linksFile);
        assertThat(links.size(), is(equalTo(1500)));
    }

    @Test
    public void testReadLinks_Tac2012Evaluation() throws ParsingException, IOException {
        final File linksFile = new File(DATA_DIR, "tac_2012_kbp_english_evaluation_entity_linking_query_types.tab");
        final List links = readLinks(TacIO.Format.TAC2012, linksFile);
        assertThat(links.size(), is(equalTo(2226)));
    }

    @Test
    public void testDetectFormatFromLinks_Tac2009Evaluation() throws ParsingException, IOException {
        final File linksFile = new File(DATA_DIR, "tac_2009_kbp_entity_linking_query_types.tab");
        assertEquals(TacIO.Format.TAC2009, TacIO.detectFormatFromLinks(linksFile));
    }

    @Test
    public void testDetectFormatFromLinks_Tac2010Evaluation() throws ParsingException, IOException {
        final File linksFile = new File(DATA_DIR, "tac_2010_kbp_evaluation_entity_linking_query_types.tab");
        assertEquals(TacIO.Format.TAC2010, TacIO.detectFormatFromLinks(linksFile));
    }

    @Test
    public void testDetectFormatFromLinks_Tac2010Training() throws ParsingException, IOException {
        final File linksFile = new File(DATA_DIR, "tac_2010_kbp_training_entity_linking_query_types.tab");
        // Note:  the format is 2009 for this file
        assertEquals(TacIO.Format.TAC2009, TacIO.detectFormatFromLinks(linksFile));
    }

    @Test
    public void testDetectFormatFromLinks_Tac2012Evaluation() throws ParsingException, IOException {
        final File linksFile = new File(DATA_DIR, "tac_2012_kbp_english_evaluation_entity_linking_query_types.tab");
        assertEquals(TacIO.Format.TAC2012, TacIO.detectFormatFromLinks(linksFile));
    }

    @Test
    public void testDetectFormatFromQueries_Tac2012Evaluation() throws ParsingException, IOException {
        final File queriesFile = new File(DATA_DIR, "tac_2012_kbp_english_evaluation_entity_linking_queries.xml");
        assertEquals(TacIO.Format.TAC2012, TacIO.detectFormatFromQueries(queriesFile));
    }

    @Test
    public void testDetectFormatFromQueries_Tac2009Evaluation() throws ParsingException, IOException {
        final File queriesFile = new File(DATA_DIR, "tac_2009_kbp_entity_linking_queries.xml");
        assertEquals(TacIO.Format.TAC2009, TacIO.detectFormatFromQueries(queriesFile));
    }

    @Test
    public void testDetectFormatFromQueries_Tac2010Evaluation() throws ParsingException, IOException {
        final File queriesFile = new File(DATA_DIR, "tac_2010_kbp_evaluation_entity_linking_queries.xml");
        assertEquals(TacIO.Format.TAC2010, TacIO.detectFormatFromQueries(queriesFile));
    }

    @Test
    public void testDetectFormatFromQueries_Tac2010Training() throws ParsingException, IOException {
        final File queriesFile = new File(DATA_DIR, "tac_2010_kbp_training_entity_linking_queries.xml");
        assertEquals(TacIO.Format.TAC2010, TacIO.detectFormatFromQueries(queriesFile));
    }

}