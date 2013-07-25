package uk.ac.susx.mlcl.erl.tac.io;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import nu.xom.ParsingException;
import org.joda.time.DateTime;
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
    public void testParseForum_Example() throws IOException, ParsingException {
        final String xmlData = "<doc id=\"bolt-eng-DF-170-181103-8881756\">\n" +
                "<headline>\n" +
                "The next president of the United States - Newt Gingrich\n" +
                "</headline>\n" +
                "<post author=\"Amelia\" datetime=\"2012-03-13T20:58:00\" id=\"p1\">\n" +
                "No really! That's how they announced him just now at his concession speech for his defeats in MS &amp; AL!\n" +
                "\n" +
                "<img src=\"http://www.usmessageboard.com/images/smilies/rofl.gif\"/> " +
                "<img src=\"http://www.usmessageboard.com/images/smilies/rofl.gif\"/> " +
                "<img src=\"http://www.usmessageboard.com/images/smilies/rofl.gif\"/> " +
                "<img src=\"http://www.usmessageboard.com/images/smilies/new/lmao.gif\"/> " +
                "<img src=\"http://www.usmessageboard.com/images/smilies/rofl.gif\"/>\n" +
                "</post>\n" +
                "<post author=\"Avatar4321\" datetime=\"2012-03-13T20:59:00\" id=\"p2\">\n" +
                "Okay. That's funny lol\n" +
                "</post>\n" +
                "<post author=\"syrenn\" datetime=\"2012-03-13T21:01:00\" id=\"p3\">\n" +
                "gggrrr.... they got rid of the puke smilie!\n" +
                "</post>\n" +
                "<post author=\"Charles_Main\" datetime=\"2012-03-13T21:02:00\" id=\"p4\">\n" +
                "<quote orig_author=\"Amelia\">\n" +
                "No really! That's how they announced him just now at his concession speech for his defeats in MS &amp; AL!\n" +
                "\n" +
                "<img src=\"http://www.usmessageboard.com/images/smilies/rofl.gif\"/> " +
                "<img src=\"http://www.usmessageboard.com/images/smilies/rofl.gif\"/> " +
                "<img src=\"http://www.usmessageboard.com/images/smilies/rofl.gif\"/> " +
                "<img src=\"http://www.usmessageboard.com/images/smilies/new/lmao.gif\"/> " +
                "<img src=\"http://www.usmessageboard.com/images/smilies/rofl.gif\"/>\n" +
                "\n" +
                "</quote>\n" +
                "\n" +
                "LOL - It's not completely Impossible, but it just became Extremely Unlikely. A slight change from " +
                "Very Unlikely which it was before tonight.\n" +
                "\n" +
                "lol\n" +
                "</post>\n" +
                "<post author=\"Avatar4321\" datetime=\"2012-03-13T21:02:00\" id=\"p5\">\n" +
                "<quote orig_author=\"syrenn\">\n" +
                "gggrrr.... they got rid of the puke smilie!\n" +
                "\n" +
                "</quote>\n" +
                "\n" +
                "Then laugh instead <img src=\"http://www.usmessageboard.com/images/smilies/wink.gif\"/>\n" +
                "</post>\n" +
                "<post author=\"Amelia\" datetime=\"2012-03-13T22:03:00\" id=\"p6\">\n" +
                "<img src=\"http://www.usmessageboard.com/images/smilies/lol.gif\"/>\n" +
                "\n" +
                "Gotta laugh. What else is there to do?\n" +
                "</post>\n" +
                "</doc>";

        ForumDocument expected = new ForumDocument(
                "bolt-eng-DF-170-181103-8881756",
                Optional.of("The next president of the United States - Newt Gingrich"),
                ImmutableList.<ForumDocument.Post>of(
                        new ForumDocument.Post("p1", "Amelia", new DateTime("2012-03-13T20:58:00"),
                                ImmutableList.<ForumDocument.Block>of(
                                        new ForumDocument.Block("No really! That's how they announced him just now " +
                                                "at his concession speech for his defeats in MS & AL!"))),
                        new ForumDocument.Post("p2", "Avatar4321", new DateTime("2012-03-13T20:59:00"),
                                ImmutableList.<ForumDocument.Block>of(
                                        new ForumDocument.Block("Okay. That's funny lol"))),
                        new ForumDocument.Post("p3", "syrenn", new DateTime("2012-03-13T21:01:00"),
                                ImmutableList.<ForumDocument.Block>of(
                                        new ForumDocument.Block("gggrrr.... they got rid of the puke smilie!"))),
                        new ForumDocument.Post("p4", "Charles_Main", new DateTime("2012-03-13T21:02:00"),
                                ImmutableList.<ForumDocument.Block>of(
                                        new ForumDocument.Quote("Amelia", "No really! That's how they announced him " +
                                                "just now at his concession speech for his defeats in MS & AL!"),
                                        new ForumDocument.Block("LOL - It's not completely Impossible, but it just became Extremely Unlikely. A slight change from Very Unlikely which it was before tonight.\n\nlol"))),
                        new ForumDocument.Post("p5", "Avatar4321", new DateTime("2012-03-13T21:02:00"),
                                ImmutableList.<ForumDocument.Block>of(
                                        new ForumDocument.Quote("syrenn", "gggrrr.... they got rid of the puke smilie!"),
                                        new ForumDocument.Block("Then laugh instead"))),
                        new ForumDocument.Post("p6", "Amelia", new DateTime("2012-03-13T22:03:00"),
                                ImmutableList.<ForumDocument.Block>of(
                                        new ForumDocument.Block("Gotta laugh. What else is there to do?")))
                ));

        final Tac2013ForumIO instance = new Tac2013ForumIO();

        final List<ForumDocument> doc = instance.readAll(ByteStreams.asByteSource(xmlData.getBytes()));

        System.out.println("Expected: " + expected);
        System.out.println("Actual:   " + doc.get(0));

        assertNotNull(doc);
        assertEquals(1, doc.size());
        assertEquals(expected, doc.get(0));

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

        final Tac2013NewswireIO instance = new Tac2013NewswireIO();

        final List<NewswireDocument> doc = instance.readAll(ByteStreams.asByteSource(xmlData.getBytes()));

        System.out.println(doc.get(0));

        assertNotNull(doc);
        assertEquals(1, doc.size());
        assertEquals(expected, doc.get(0));
    }


}
