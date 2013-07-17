package uk.ac.susx.mlcl.erl.tac;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import nu.xom.ParsingException;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.ac.susx.mlcl.erl.test.AbstractTest;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Unit and integrations tests for the TacIO class and associated functionality.
 */
@RunWith(Enclosed.class)
public class TacIOTest {
    private static final File DATA_DIR = new File("/Volumes/LocalScratchHD/LocalHome/Projects/NamedEntityLinking/Data");
    private static final File OUTPUT_DIR = new File("/Volumes/LocalScratchHD/LocalHome/Projects/NamedEntityLinking/Data/out");

    /**
     * Focused regression tests that check specific instance of links.
     */
    @RunWith(Parameterized.class)
    public static class LinkIORegressionInstances {

        private final Class<? extends TacIO.LinkIO> cls;
        private final String data;
        private final Link link;

        public LinkIORegressionInstances(Class<? extends TacIO.LinkIO> cls, String data, Link link) {
            this.cls = cls;
            this.data = data;
            this.link = link;
        }

        @Parameterized.Parameters(name = "{index}: {0}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {
                            TacIO.Tac2010LinkIO.class,
                            "EL000281\tNIL0001\tGPE\tNO\tWL",
                            new Link("EL000281", "NIL0001", EntityType.GPE, false, Genre.WB)
                    },
                    {
                            TacIO.Tac2010LinkIO.class,
                            "EL001344\tE0374684\tGPE\tYES\tWL",
                            new Link("EL001344", "E0374684", EntityType.GPE, true, Genre.WB)
                    },
                    {
                            TacIO.Tac2009LinkIO.class,
                            "EL1\tNIL0001\tPER",
                            new Link("EL1", "NIL0001", EntityType.PER)
                    },
                    {
                            TacIO.Tac2009LinkIO.class,
                            "EL05306\tE0421536\tORG",
                            new Link("EL05306", "E0421536", EntityType.ORG)
                    },
                    {
                            TacIO.Tac2012LinkIO.class,
                            "EL_ENG_00001\tE0800145\tPER\tWB\tNO",
                            new Link("EL_ENG_00001", "E0800145", EntityType.PER, false, Genre.WB)
                    },
            });
        }

        @Test
        public void testReadLink() throws ParsingException, IOException, IllegalAccessException, InstantiationException {
            final TacIO.LinkIO instance = cls.newInstance();
            final List<Link> links = instance.readAll(new StringReader(data));
            assertTrue(links.size() == 1);
            assertEquals(link, links.get(0));
        }

        @Test
        public void testWriteLink() throws ParsingException, IOException, IllegalAccessException, InstantiationException {
            final TacIO.LinkIO instance = cls.newInstance();
            final StringWriter writer = new StringWriter();
            instance.writeAll(writer, ImmutableList.of(link));
            assertEquals(data.trim(), writer.toString().trim());
        }

    }

    /**
     * Tests that operate on entire files of data.
     */
    @RunWith(Parameterized.class)
    public static class DataFileTests extends AbstractTest {
        private final Class<? extends TacIO.QueryIO> queryIoClass;
        private final Class<? extends TacIO.LinkIO> linkIoClass;
        private final String queriesFileName;
        private final String linksFileName;
        private final int expectedSize;

        public DataFileTests(Class<? extends TacIO.QueryIO> queryIoClass,
                             Class<? extends TacIO.LinkIO> linkIoClass,
                             String queriesFileName, String linksFileName, int expectedSize) {
            this.queryIoClass = queryIoClass;
            this.linkIoClass = linkIoClass;
            this.queriesFileName = queriesFileName;
            this.linksFileName = linksFileName;
            this.expectedSize = expectedSize;
        }

        @Parameterized.Parameters(name = "{index}: {2}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {
                            TacIO.Tac2012QueryIO.class,
                            TacIO.Tac2012LinkIO.class,
                            "tac_2012_kbp_english_evaluation_entity_linking_queries.xml",
                            "tac_2012_kbp_english_evaluation_entity_linking_query_types.tab",
                            2226
                    },
                    {
                            TacIO.Tac2009QueryIO.class,
                            TacIO.Tac2009LinkIO.class,
                            "tac_2009_kbp_entity_linking_queries.xml",
                            "tac_2009_kbp_entity_linking_query_types.tab",
                            3904
                    },
                    {
                            TacIO.Tac2010QueryIO.class,
                            TacIO.Tac2010LinkIO.class,
                            "tac_2010_kbp_evaluation_entity_linking_queries.xml",
                            "tac_2010_kbp_evaluation_entity_linking_query_types.tab",
                            2250
                    },
                    {
                            TacIO.Tac2010QueryIO.class,
                            TacIO.Tac2009LinkIO.class,
                            "tac_2010_kbp_training_entity_linking_queries.xml",
                            "tac_2010_kbp_training_entity_linking_query_types.tab",
                            1500
                    },
            });
        }

        private static List<Link> doReadLinks(TacIO.LinkIO instance, File linksFile) throws ParsingException, IOException {
            final List<Link> links = instance.readAll(linksFile);
            assertNotNull(links);
            assertFalse(links.isEmpty());
            assertThat(ImmutableSet.copyOf(links).size(), is(equalTo(links.size())));
            return links;
        }

        public File getQueryFile() {
            return new File(DATA_DIR, queriesFileName);
        }

        public File getLinkFile() {
            return new File(DATA_DIR, linksFileName);
        }

        @Test
        public void testReadLinks() throws ParsingException, IOException, IllegalAccessException, InstantiationException {
            final List<Link> links = doReadLinks(linkIoClass.newInstance(), getLinkFile());
            assertThat(links.size(), is(equalTo(expectedSize)));
        }

        @Test
        public void testWriteLinks() throws ParsingException, IOException, IllegalAccessException, InstantiationException {
            final List<Link> links = doReadLinks(linkIoClass.newInstance(), getLinkFile());
            final File dstFile = new File(OUTPUT_DIR, linksFileName);

            linkIoClass.newInstance().writeAll(dstFile, links);

            assertTrue(dstFile.exists());
            assertTrue(dstFile.length() > 0);

            final List<Link> links2 = doReadLinks(linkIoClass.newInstance(), dstFile);
            assertEquals(links, links2);
        }

        @Test
        public void testReadQueries() throws IllegalAccessException, InstantiationException, ParsingException, IOException {
            final List<Query> result = queryIoClass.newInstance().readAll(getQueryFile());
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertThat(ImmutableSet.copyOf(result).size(), is(equalTo(result.size())));
            assertThat(result.size(), is(equalTo(expectedSize)));
        }

        @Test
        public void testDetectFormatFromQueries() throws ParsingException, IOException {
            assertEquals(queryIoClass, TacIO.detectFormatFromQueries(getQueryFile()).getClass());
        }

        @Test
        public void testDetectFormatFromLinks() throws ParsingException, IOException {
            assertEquals(linkIoClass, TacIO.detectFormatFromLinks(getLinkFile()).getClass());
        }

    }
}
