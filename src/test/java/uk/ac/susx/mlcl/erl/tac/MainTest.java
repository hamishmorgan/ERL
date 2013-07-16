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
public class MainTest extends AbstractTest {

    private static final File DATA_DIR = new File("/Volumes/LocalScratchHD/LocalHome/Projects/NamedEntityLinking/Data");

    private List<Query> readQueries(File queriesFile) throws ParsingException, IOException {
        System.out.println(queriesFile);
        final List<Query> result = Main.readQueries(queriesFile);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertThat(ImmutableSet.copyOf(result).size(), is(equalTo(result.size())));
        return result;
    }

    //    18 tac_2009_kbp_entity_linking_missing_links_with_wiki_node_ids.tab
//    3904 tac_2009_kbp_entity_linking_query_types.tab
//    2250 tac_2010_kbp_evaluation_entity_linking_query_types.tab
//    1500 tac_2010_kbp_training_entity_linking_query_types.tab
//    2226 tac_2012_kbp_english_evaluation_entity_linking_query_types.tab
//
    @Test
    public void testReadTac2012EvaluationQueries() throws ParsingException, IOException {

        final File queriesFile = new File(DATA_DIR, "tac_2012_kbp_english_evaluation_entity_linking_queries.xml");
        final List<Query> result = readQueries(queriesFile);
        assertThat(result.size(), is(equalTo(2226)));
    }

    @Test
    public void testReadTac2009EvaluationQueries() throws ParsingException, IOException {

        final File queriesFile = new File(DATA_DIR, "tac_2009_kbp_entity_linking_queries.xml");
        final List<Query> result = readQueries(queriesFile);
        assertThat(result.size(), is(equalTo(3904)));
    }

    @Test
    public void testReadTac2010EvaluationQueries() throws ParsingException, IOException {

        final File queriesFile = new File(DATA_DIR, "tac_2010_kbp_evaluation_entity_linking_queries.xml");
        final List<Query> result = readQueries(queriesFile);
        assertThat(result.size(), is(equalTo(2250)));
    }

    @Test
    public void testReadTac2010TrainingQueries() throws ParsingException, IOException {

        final File queriesFile = new File(DATA_DIR, "tac_2010_kbp_training_entity_linking_queries.xml");
        final List<Query> result = readQueries(queriesFile);
        assertThat(result.size(), is(equalTo(1500)));
    }

//
//    tac_2009_kbp_entity_linking_missing_links_with_wiki_node_ids.tab
//    tac_2009_kbp_entity_linking_query_types.tab
//    tac_2010_kbp_evaluation_entity_linking_query_types.tab
//    tac_2010_kbp_training_entity_linking_query_types.tab
//    tac_2012_kbp_english_evaluation_entity_linking_query_types.tab

    private List readLinks(File linksFile) throws ParsingException, IOException {
        System.out.println(linksFile);
        final List links = Main.readLinks(linksFile);
        assertNotNull(links);
        assertFalse(links.isEmpty());
        assertThat(ImmutableSet.copyOf(links).size(), is(equalTo(links.size())));
        return links;
    }

    @Test
    public void testReadTac2009EvaluationLinks() throws ParsingException, IOException {
        final File linksFile = new File(DATA_DIR, "tac_2009_kbp_entity_linking_query_types.tab");
        final List links = readLinks(linksFile);
        assertThat(links.size(), is(equalTo(3904)));
    }

    @Test
    public void testReadTac2010EvaluationLinks() throws ParsingException, IOException {
        final File linksFile = new File(DATA_DIR, "tac_2010_kbp_evaluation_entity_linking_query_types.tab");
        final List links = readLinks(linksFile);
        assertThat(links.size(), is(equalTo(2250)));
    }

    @Test
    public void testReadTac2010TrainingLinks() throws ParsingException, IOException {
        final File linksFile = new File(DATA_DIR, "tac_2010_kbp_training_entity_linking_query_types.tab");
        final List links = readLinks(linksFile);
        assertThat(links.size(), is(equalTo(1500)));
    }


}