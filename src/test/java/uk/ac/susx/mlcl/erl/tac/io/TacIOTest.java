package uk.ac.susx.mlcl.erl.tac.io;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import nu.xom.ParsingException;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.ac.susx.mlcl.erl.tac.EntityType;
import uk.ac.susx.mlcl.erl.tac.Genre;
import uk.ac.susx.mlcl.erl.tac.Link;
import uk.ac.susx.mlcl.erl.tac.Query;
import uk.ac.susx.mlcl.erl.test.AbstractTest;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
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

    //    private static final File DATA_DIR = new File("/Volumes/LocalScratchHD/LocalHome/Projects/NamedEntityLinking/repo/src/test/resources/uk/ac/susx/mlcl/erl/tac/io");
    private static final File OUTPUT_DIR = new File("/Volumes/LocalScratchHD/LocalHome/Projects/NamedEntityLinking/Data/out");

    static {
    }

    /**
     * Focused regression tests that check specific instance of links.
     */
    @RunWith(Parameterized.class)
    public static class LinkIORegressionInstances extends AbstractTest {

        private final Class<? extends LinkIO> cls;
        private final String data;
        private final Link link;

        public LinkIORegressionInstances(Class<? extends LinkIO> cls, String data, Link link) {
            this.cls = cls;
            this.data = data;
            this.link = link;
        }

        @Parameterized.Parameters(name = "{index}: {0}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {
                            Tac2010LinkIO.class,
                            "EL000281\tNIL0001\tGPE\tNO\tWL",
                            new Link("EL000281", "NIL0001", EntityType.GPE, false, Genre.WB)
                    },
                    {
                            Tac2010LinkIO.class,
                            "EL001344\tE0374684\tGPE\tYES\tWL",
                            new Link("EL001344", "E0374684", EntityType.GPE, true, Genre.WB)
                    },
                    {
                            Tac2009LinkIO.class,
                            "EL1\tNIL0001\tPER",
                            new Link("EL1", "NIL0001", EntityType.PER, true, Genre.NW)
                    },
                    {
                            Tac2009LinkIO.class,
                            "EL05306\tE0421536\tORG",
                            new Link("EL05306", "E0421536", EntityType.ORG, true, Genre.NW)
                    },
                    {
                            Tac2012LinkIO.class,
                            "EL_ENG_00001\tE0800145\tPER\tWB\tNO",
                            new Link("EL_ENG_00001", "E0800145", EntityType.PER, false, Genre.WB)
                    },
            });
        }

        @Test
        public void testDetectFormat() throws ParsingException, IOException, IllegalAccessException, InstantiationException {
            assertEquals(cls, LinkIO.detectFormat(new StringReader(data)).getClass());
        }

        @Test
        public void testRead() throws ParsingException, IOException, IllegalAccessException, InstantiationException {
            final LinkIO instance = cls.newInstance();
            final List<Link> links = instance.readAll(new StringReader(data));
            assertTrue(links.size() == 1);
            assertEquals(link, links.get(0));
        }

        @Test
        public void testWrite() throws ParsingException, IOException, IllegalAccessException, InstantiationException {
            final LinkIO instance = cls.newInstance();
            final StringWriter writer = new StringWriter();
            instance.writeAll(writer, ImmutableList.of(link));
            assertEquals(data.trim(), writer.toString().trim());
        }

    }

    /**
     * Focused regression tests that check specific instance of links.
     */
    @RunWith(Parameterized.class)
    public static class QueryIORegressionInstances {

        private final Class<? extends QueryIO> cls;
        private final String data;
        private final Query query;

        public QueryIORegressionInstances(Class<? extends QueryIO> cls, String data, Query query) {
            this.cls = cls;
            this.data = data;
            this.query = query;
        }

        @Parameterized.Parameters(name = "{index}: {0}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {
                            Tac2009QueryIO.class,
                            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                    "<kbpentlink xml:base=\"\">\n" +
                                    "  <query id=\"EL1\">\n" +
                                    "    <name>Abbas Moussawi</name>\n" +
                                    "    <docid>LTW_ENG_19960311.0047.LDC2007T07</docid>\n" +
                                    "  </query>\n" +
                                    "</kbpentlink>\n",
                            new Query("EL1", "Abbas Moussawi", "LTW_ENG_19960311.0047.LDC2007T07")
                    },
                    {
                            Tac2010QueryIO.class,
                            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                    "<kbpentlink xml:base=\"\">\n" +
                                    "   <query id=\"EL001125\">\n" +
                                    "    <name>El Salvador</name>\n" +
                                    "    <docid>eng-NG-31-142693-10076185</docid>\n" +
                                    "  </query>\n" +
                                    "</kbpentlink>\n",
                            new Query("EL001125", "El Salvador", "eng-NG-31-142693-10076185")
                    },
                    {
                            Tac2010GoldQueryIO.class,
                            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                    "<kbpentlink xml:base=\"\">\n" +
                                    "   <query id=\"EL10345\">\n" +
                                    "    <name>FDIC</name>\n" +
                                    "    <docid>eng-WL-11-174606-12978493</docid>\n" +
                                    "    <entity>E0348440</entity>\n" +
                                    "  </query>\n" +
                                    "</kbpentlink>\n",
                            new Query("EL10345", "FDIC", "eng-WL-11-174606-12978493", "E0348440")
                    },
                    {
                            Tac2012QueryIO.class,
                            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                    "<kbpentlink xml:base=\"\">\n" +
                                    "   <query id=\"EL_ENG_00126\">\n" +
                                    "    <name>New Haven</name>\n" +
                                    "    <docid>APW_ENG_20070120.0744.LDC2009T13</docid>\n" +
                                    "    <beg>745</beg>\n" +
                                    "    <end>753</end>\n" +
                                    "  </query>\n" +
                                    "</kbpentlink>\n",
                            new Query("EL_ENG_00126", "New Haven", "APW_ENG_20070120.0744.LDC2009T13", 745, 753)
                    },
            });
        }

        @Test
        public void testDetectFormat() throws ParsingException, IOException, IllegalAccessException, InstantiationException {
            assertEquals(cls, QueryIO.detectFormat(new StringReader(data)).getClass());
        }

        @Test
        public void testDetectAndRead() throws ParsingException, IOException, IllegalAccessException, InstantiationException {
            final QueryIO instance = QueryIO.detectFormat(new StringReader(data));
            final List<Query> links = instance.readAll(new StringReader(data));
            assertTrue(links.size() == 1);
            assertEquals(query, links.get(0));
        }

        @Test
        public void testRead() throws ParsingException, IOException, IllegalAccessException, InstantiationException {
            final QueryIO instance = cls.newInstance();
            final List<Query> links = instance.readAll(new StringReader(data));
            assertTrue(links.size() == 1);
            assertEquals(query, links.get(0));
        }

        @Test
        public void testWrite() throws ParsingException, IOException, IllegalAccessException, InstantiationException {
            final QueryIO instance = cls.newInstance();
            final StringWriter writer = new StringWriter();
            instance.writeAll(writer, ImmutableList.of(query));
            assertEquals(
                    data.replaceAll("\\s+", " ").trim(),
                    writer.toString().replaceAll("\\s+", " ").trim());
        }

    }

    /**
     * Tests that operate on entire files of data.
     */
    @RunWith(Parameterized.class)
    public static class DataFileTests extends AbstractTest {
        private final Class<? extends QueryIO> queryIoClass;
        private final Class<? extends LinkIO> linkIoClass;
        private final String queriesFileName;
        private final String linksFileName;
        private final int expectedSize;

        public DataFileTests(Class<? extends QueryIO> queryIoClass,
                             Class<? extends LinkIO> linkIoClass,
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
                            Tac2012QueryIO.class,
                            Tac2012LinkIO.class,
                            "tac_2012_kbp_english_evaluation_entity_linking_queries.xml",
                            "tac_2012_kbp_english_evaluation_entity_linking_query_types.tab",
                            2226
                    },
                    {
                            Tac2009QueryIO.class,
                            Tac2009LinkIO.class,
                            "tac_2009_kbp_entity_linking_queries.xml",
                            "tac_2009_kbp_entity_linking_query_types.tab",
                            3904
                    },
                    {
                            Tac2010QueryIO.class,
                            Tac2010LinkIO.class,
                            "tac_2010_kbp_evaluation_entity_linking_queries.xml",
                            "tac_2010_kbp_evaluation_entity_linking_query_types.tab",
                            2250
                    },
                    {
                            Tac2010GoldQueryIO.class,
                            Tac2009LinkIO.class,
                            "tac_2010_kbp_training_entity_linking_queries.xml",
                            "tac_2010_kbp_training_entity_linking_query_types.tab",
                            1500
                    },
            });
        }

        private static List<Link> doReadLinks(LinkIO instance, URL linksURL) throws ParsingException, IOException {
            final List<Link> links = instance.readAll(linksURL);
            assertNotNull(links);
            assertFalse(links.isEmpty());
            assertThat(ImmutableSet.copyOf(links).size(), is(equalTo(links.size())));
            return links;
        }

        private static List<Link> doReadLinks(LinkIO instance, File linksFile) throws ParsingException, IOException {
            final List<Link> links = instance.readAll(linksFile);
            assertNotNull(links);
            assertFalse(links.isEmpty());
            assertThat(ImmutableSet.copyOf(links).size(), is(equalTo(links.size())));
            return links;
        }

        private static List<Query> doReadQueries(QueryIO instance, URL queriesUrl) throws ParsingException, IOException {
            final List<Query> queries = instance.readAll(queriesUrl);
            assertNotNull(queries);
            assertFalse(queries.isEmpty());
            assertThat(ImmutableSet.copyOf(queries).size(), is(equalTo(queries.size())));
            return queries;
        }

        private static List<Query> doReadQueries(QueryIO instance, File queriesFile) throws ParsingException, IOException {
            final List<Query> queries = instance.readAll(queriesFile);
            assertNotNull(queries);
            assertFalse(queries.isEmpty());
            assertThat(ImmutableSet.copyOf(queries).size(), is(equalTo(queries.size())));
            return queries;
        }

        public File getQueryFile() throws URISyntaxException {
            return new File(getQueryUrl().toURI());
        }

        public URL getQueryUrl() {
            return Resources.getResource(TacIOTest.class, queriesFileName);
        }

        public File getLinksFile() throws URISyntaxException {
            return new File(getLinksUrl().toURI());
        }

        public URL getLinksUrl() {
            return Resources.getResource(TacIOTest.class, linksFileName);
        }

        @Test
        public void testReadLinksUrl() throws ParsingException, IOException, IllegalAccessException, InstantiationException {
            final List<Link> links = doReadLinks(linkIoClass.newInstance(), getLinksUrl());
            assertThat(links.size(), is(equalTo(expectedSize)));
        }

        @Test
        public void testDetectAndReadLinksUrl() throws ParsingException, IOException, IllegalAccessException, InstantiationException {
            final List<Link> links = doReadLinks(LinkIO.detectFormat(getLinksUrl()), getLinksUrl());
            assertThat(links.size(), is(equalTo(expectedSize)));
        }

        @Test
        public void testWriteLinksUrl() throws ParsingException, IOException, IllegalAccessException, InstantiationException, URISyntaxException {
            final List<Link> links = doReadLinks(linkIoClass.newInstance(), getLinksUrl());
            final File dstFile = new File(OUTPUT_DIR, linksFileName);
            dstFile.deleteOnExit();
            final URL dstURL = dstFile.toURI().toURL();

            linkIoClass.newInstance().writeAll(dstURL, links);

            assertTrue(dstFile.exists());
            assertTrue(dstFile.length() > 0);

            final List<Link> links2 = doReadLinks(linkIoClass.newInstance(), dstURL);
            assertEquals(links, links2);
        }


        @Test
        public void testReadLinksFile() throws ParsingException, IOException, IllegalAccessException, InstantiationException, URISyntaxException {
            final List<Link> links = doReadLinks(linkIoClass.newInstance(), getLinksFile());
            assertThat(links.size(), is(equalTo(expectedSize)));
        }

        @Test
        public void testDetectAndReadLinksFile() throws ParsingException, IOException, IllegalAccessException, InstantiationException, URISyntaxException {
            final List<Link> links = doReadLinks(LinkIO.detectFormat(getLinksFile()), getLinksFile());
            assertThat(links.size(), is(equalTo(expectedSize)));
        }

        @Test
        public void testWriteLinksFile() throws ParsingException, IOException, IllegalAccessException, InstantiationException, URISyntaxException {
            final List<Link> links = doReadLinks(linkIoClass.newInstance(), getLinksFile());
            final File dstFile = new File(OUTPUT_DIR, linksFileName);
            dstFile.deleteOnExit();

            linkIoClass.newInstance().writeAll(dstFile, links);

            assertTrue(dstFile.exists());
            assertTrue(dstFile.length() > 0);

            final List<Link> links2 = doReadLinks(linkIoClass.newInstance(), dstFile);
            assertEquals(links, links2);
        }

        @Test
        public void testReadQueriesUrl() throws IllegalAccessException, InstantiationException, ParsingException, IOException {
            final List<Query> queries = doReadQueries(queryIoClass.newInstance(), getQueryUrl());
            assertThat(queries.size(), is(equalTo(expectedSize)));
        }

        @Test
        public void testDetectAndReadQueriesUrl() throws IllegalAccessException, InstantiationException, ParsingException, IOException {
            final List<Query> queries = doReadQueries(QueryIO.detectFormat(getQueryUrl()), getQueryUrl());
            assertThat(queries.size(), is(equalTo(expectedSize)));
        }

        @Test
        public void testWriteQueriesUrl() throws ParsingException, IOException, IllegalAccessException, InstantiationException, URISyntaxException {
            final List<Query> queries = doReadQueries(queryIoClass.newInstance(), getQueryUrl());
            final File dstFile = new File(OUTPUT_DIR, queriesFileName);
            dstFile.deleteOnExit();
            final URL dstURL = dstFile.toURI().toURL();

            queryIoClass.newInstance().writeAll(dstURL, queries);

            assertTrue(dstFile.exists());
            assertTrue(dstFile.length() > 0);

            final List<Query> queries2 = doReadQueries(queryIoClass.newInstance(), dstURL);
            assertEquals(queries, queries2);
        }

        @Test
        public void testReadQueriesFile() throws IllegalAccessException, InstantiationException, ParsingException, IOException, URISyntaxException {
            final List<Query> queries = doReadQueries(queryIoClass.newInstance(), getQueryFile());
            assertThat(queries.size(), is(equalTo(expectedSize)));
        }

        @Test
        public void testDetectAndReadQueriesFile() throws IllegalAccessException, InstantiationException, ParsingException, IOException, URISyntaxException {
            final List<Query> queries = doReadQueries(QueryIO.detectFormat(getQueryFile()), getQueryFile());
            assertThat(queries.size(), is(equalTo(expectedSize)));
        }

        @Test
        public void testWriteQueriesFile() throws ParsingException, IOException, IllegalAccessException, InstantiationException, URISyntaxException {
            final List<Query> queries = doReadQueries(queryIoClass.newInstance(), getQueryFile());
            final File dstFile = new File(OUTPUT_DIR, queriesFileName);
            dstFile.deleteOnExit();

            queryIoClass.newInstance().writeAll(dstFile, queries);

            assertTrue(dstFile.exists());
            assertTrue(dstFile.length() > 0);

            final List<Query> queries2 = doReadQueries(queryIoClass.newInstance(), dstFile);
            assertEquals(queries, queries2);
        }

        @Test
        public void testDetectFormatFromQueriesUrl() throws ParsingException, IOException {
            assertEquals(queryIoClass, QueryIO.detectFormat(getQueryUrl()).getClass());
        }

        @Test
        public void testDetectFormatFromQueriesFile() throws ParsingException, IOException, URISyntaxException {
            assertEquals(queryIoClass, QueryIO.detectFormat(getQueryFile()).getClass());
        }

        @Test
        public void testDetectFormatFromLinksUrl() throws ParsingException, IOException {
            assertEquals(linkIoClass, LinkIO.detectFormat(getLinksUrl()).getClass());
        }

        @Test
        public void testDetectFormatFromLinksFile() throws ParsingException, IOException, URISyntaxException {
            assertEquals(linkIoClass, LinkIO.detectFormat(getLinksFile()).getClass());
        }

    }
}
