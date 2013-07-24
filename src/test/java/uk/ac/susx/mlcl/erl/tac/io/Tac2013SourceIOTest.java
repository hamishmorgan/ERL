package uk.ac.susx.mlcl.erl.tac.io;

import com.google.common.base.Optional;
import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import nu.xom.ParsingException;
import org.junit.Test;
import uk.ac.susx.mlcl.erl.lib.IOUtils;
import uk.ac.susx.mlcl.erl.tac.source.ForumDocument;
import uk.ac.susx.mlcl.erl.tac.source.NewswireDocument;
import uk.ac.susx.mlcl.erl.test.AbstractTest;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Hamish Morgan
 */
public class Tac2013SourceIOTest extends AbstractTest {

    @Test
    public void testParseForumUncompressed() throws IOException, ParsingException {
        final URL url = getResource("bolt-eng-DF-215.sample");
        final Tac2013ForumIO instance = new Tac2013ForumIO();

        final List<ForumDocument> doc = instance.readAll(Resources.asByteSource(url));
        System.out.println(doc.get(0));
        assertNotNull(doc);
        assertEquals(55, doc.size());
    }

    @Test
    public void testParseForumCompressed() throws IOException, ParsingException {
        final URL url = getResource("bolt-eng-DF-215.sample.gz");
        final Tac2013ForumIO instance = new Tac2013ForumIO();

        final List<ForumDocument> doc = instance.readAll(IOUtils.asGzipByteSource(Resources.asByteSource(url)));
        System.out.println(doc.get(0));
        assertNotNull(doc);
        assertEquals(55, doc.size());
    }

    @Test
    public void testParseNewswireUncompressed() throws IOException, ParsingException {
        final URL url = getResource("AFP_ENG_200905");
        final Tac2013NewswireIO instance = new Tac2013NewswireIO();

        final List<NewswireDocument> doc = instance.readAll(Resources.asByteSource(url));
        System.out.println(doc.get(0));
        assertNotNull(doc);
        assertEquals(484, doc.size());
    }

    @Test
    public void testParseNewswireCompressed() throws IOException, ParsingException {
        final URL url = getResource("AFP_ENG_200905.gz");
        final Tac2013NewswireIO instance = new Tac2013NewswireIO();

        final List<NewswireDocument> doc = instance.readAll(IOUtils.asGzipByteSource(Resources.asByteSource(url)));
        System.out.println(doc.get(0));
        assertNotNull(doc);
        assertEquals(484, doc.size());
    }

    @Test
    public void testParseNewswire_Example() throws IOException, ParsingException {

        final String xmlData =
                "<DOC id=\"AFP_ENG_20090531.0001\" type=\"story\" >\n" +
                        "<HEADLINE>\n" +
                        "Chile swine flu cases jump to 276\n" +
                        "</HEADLINE>\n" +
                        "<DATELINE>\n" +
                        "Santiago, May 31, 2009 (AFP)\n" +
                        "</DATELINE>\n" +
                        "<TEXT>\n" +
                        "<P>\n" +
                        "Chilean health authorities confirmed 26 new cases of swine flu on Sunday,\n" +
                        "raising the number of patients with A(H1N1) virus in the country to 276, the\n" +
                        "highest number on the continent.\n" +
                        "</P>\n" +
                        "<P>\n" +
                        "The sometimes-deadly disease continued its rise across Latin America meanwhile\n" +
                        "with Argentina, Brazil, Bolivia, Peru and the Dominican Republic all reporting\n" +
                        "new cases.\n" +
                        "</P>\n" +
                        "<P>\n" +
                        "At its last count at the end of last week Mexican authorities said 97 people had\n" +
                        "died from the disease and that 4,932 had been infected. Officials however\n" +
                        "maintained that the epidemic was on the wane there.\n" +
                        "</P>\n" +
                        "</TEXT>\n" +
                        "</DOC>";

        NewswireDocument expected = new NewswireDocument(
                "AFP_ENG_20090531.0001",
                NewswireDocument.Type.story,
                Optional.of("Chile swine flu cases jump to 276"),
                Optional.of("Santiago, May 31, 2009 (AFP)"),
                Arrays.asList(
                        "Chilean health authorities confirmed 26 new cases of swine flu on Sunday,\n" +
                                "raising the number of patients with A(H1N1) virus in the country to 276, the\n" +
                                "highest number on the continent.",
                        "The sometimes-deadly disease continued its rise across Latin America meanwhile\n" +
                                "with Argentina, Brazil, Bolivia, Peru and the Dominican Republic all reporting\n" +
                                "new cases.",
                        "At its last count at the end of last week Mexican authorities said 97 people had\n" +
                                "died from the disease and that 4,932 had been infected. Officials however\n" +
                                "maintained that the epidemic was on the wane there."
                )
        );

        final URL url = getResource("AFP_ENG_200905");
        final Tac2013NewswireIO instance = new Tac2013NewswireIO();

        final List<NewswireDocument> doc = instance.readAll(ByteStreams.asByteSource(xmlData.getBytes()));

        System.out.println(doc.get(0));

        assertNotNull(doc);
        assertEquals(1, doc.size());
        assertEquals(expected, doc.get(0));
    }


}
