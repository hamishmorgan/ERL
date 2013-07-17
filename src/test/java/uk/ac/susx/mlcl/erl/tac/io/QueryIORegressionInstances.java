package uk.ac.susx.mlcl.erl.tac.io;

import com.google.common.collect.ImmutableList;
import nu.xom.ParsingException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.ac.susx.mlcl.erl.tac.Query;
import uk.ac.susx.mlcl.erl.test.AbstractTest;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Focused regression tests that check specific instance of queries.
 *
 * @author Hamish Morgam
 */
@RunWith(Parameterized.class)
public class QueryIORegressionInstances extends AbstractTest {

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
        // noinspection HardcodedLineSeparator
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
