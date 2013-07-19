package uk.ac.susx.mlcl.erl.tac.io;

import com.google.common.collect.ImmutableList;
import nu.xom.ParsingException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.ac.susx.mlcl.erl.tac.Genre;
import uk.ac.susx.mlcl.erl.tac.kb.EntityType;
import uk.ac.susx.mlcl.erl.tac.queries.Link;
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
 * Focused regression tests that check specific instance of links.
 *
 * @author Hamish Morgam
 */
@RunWith(Parameterized.class)
public class LinkIORegressionInstances extends AbstractTest {

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
                        Tac2009LinkIO.class,
                        "EL1\tNIL0001\tPER",
                        new Link("EL1", "NIL0001", EntityType.PER, true, Genre.NW)
                },
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
                        Tac2010GoldLinkIO.class,
                        "EL05306\tE0421536\tORG",
                        new Link("EL05306", "E0421536", EntityType.ORG, true, Genre.NW)
                },
                {      // 2011
                        Tac2011LinkIO.class,
                        "EL_00001\tNIL290\tPER\tNW\tNO",
                        new Link("EL_00001", "NIL290", EntityType.PER, false, Genre.NW)
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
