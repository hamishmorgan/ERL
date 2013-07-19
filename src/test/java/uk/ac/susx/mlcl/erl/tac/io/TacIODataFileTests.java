package uk.ac.susx.mlcl.erl.tac.io;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import nu.xom.ParsingException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.ac.susx.mlcl.erl.tac.Link;
import uk.ac.susx.mlcl.erl.tac.Query;
import uk.ac.susx.mlcl.erl.test.AbstractTest;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Tests that operate on entire files of data.
 *
 * @author Hamish Morgam
 */
@RunWith(Parameterized.class)
public class TacIODataFileTests extends AbstractTest {
    private final Class<? extends QueryIO> queryIoClass;
    private final Class<? extends LinkIO> linkIoClass;
    private final String queriesFileName;
    private final String linksFileName;
    private final int expectedSize;

    public TacIODataFileTests(Class<? extends QueryIO> queryIoClass,
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
                        Tac2010GoldLinkIO.class,
                        "tac_2010_kbp_training_entity_linking_queries.xml",
                        "tac_2010_kbp_training_entity_linking_query_types.tab",
                        1500
                },
                {
                        Tac2011QueryIO.class,
                        Tac2011LinkIO.class,
                        "tac_2011_kbp_english_evaluation_entity_linking_queries.xml",
                        "tac_2011_kbp_english_evaluation_entity_linking_query_types.tab",
                        2250
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
        return Resources.getResource(this.getClass(), queriesFileName);
    }

    public File getLinksFile() throws URISyntaxException {
        return new File(getLinksUrl().toURI());
    }

    public URL getLinksUrl() {
        return Resources.getResource(this.getClass(), linksFileName);
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

    File newTempFile() throws IOException {
        final File tmpFile = File.createTempFile(this.getClass().getName(), ".tmp");
        tmpFile.deleteOnExit();
        return tmpFile;
    }

    URL newTempUrl() throws IOException {
        return newTempFile().toURI().toURL();
    }

    @Test
    public void testWriteLinksUrl() throws ParsingException, IOException, IllegalAccessException, InstantiationException, URISyntaxException {
        final List<Link> links = doReadLinks(linkIoClass.newInstance(), getLinksUrl());
        final URL dstURL = newTempUrl();

        linkIoClass.newInstance().writeAll(dstURL, links);

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
        final File dstFile = newTempFile();

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
        final URL dstURL = newTempUrl();

        queryIoClass.newInstance().writeAll(dstURL, queries);

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
        final File dstFile = newTempFile();

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
