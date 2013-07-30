package uk.ac.susx.mlcl.erl.tac.io;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import nu.xom.ParsingException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.MutableDateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;
import org.xml.sax.SAXException;
import uk.ac.susx.mlcl.erl.lib.IOUtils;
import uk.ac.susx.mlcl.erl.tac.source.ForumDocument;
import uk.ac.susx.mlcl.erl.tac.source.NewswireDocument;
import uk.ac.susx.mlcl.erl.tac.source.WebDocument;
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
    public void testParseForumUncompressed() throws IOException, ParsingException, SAXException {
        final URL url = getResource("bolt-eng-DF-215.sample");
        final Tac2013ForumIO instance = new Tac2013ForumIO();

        final List<ForumDocument> doc = instance.readAll(Resources.asByteSource(url));
        System.out.println(doc.get(0));
        assertNotNull(doc);
        assertEquals(55, doc.size());
    }

    @Test
    public void testParseForumCompressed() throws IOException, ParsingException, SAXException {
        final URL url = getResource("bolt-eng-DF-215.sample.gz");
        final Tac2013ForumIO instance = new Tac2013ForumIO();

        final List<ForumDocument> doc = instance.readAll(IOUtils.asGzipByteSource(Resources.asByteSource(url)));
        System.out.println(doc.get(0));
        assertNotNull(doc);
        assertEquals(55, doc.size());
    }

    @Test
    public void testParseForum_Example() throws IOException, ParsingException, SAXException {
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
    public void testParseNewswireUncompressed() throws IOException, ParsingException, SAXException {
        final URL url = getResource("AFP_ENG_200905");
        final Tac2013NewswireIO instance = new Tac2013NewswireIO();

        final List<NewswireDocument> doc = instance.readAll(Resources.asByteSource(url));
        System.out.println(doc.get(0));
        assertNotNull(doc);
        assertEquals(484, doc.size());
    }

    @Test
    public void testParseNewswireCompressed() throws IOException, ParsingException, SAXException {
        final URL url = getResource("AFP_ENG_200905.gz");
        final Tac2013NewswireIO instance = new Tac2013NewswireIO();

        final List<NewswireDocument> doc = instance.readAll(IOUtils.asGzipByteSource(Resources.asByteSource(url)));
        System.out.println(doc.get(0));
        assertNotNull(doc);
        assertEquals(484, doc.size());
    }

    @Test
    public void testParseNewswire_Example() throws IOException, ParsingException, SAXException {

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

    @Test
    public void testParseWeb_UsenetUncompressed() throws IOException, ParsingException, SAXException {
        final URL url = getResource("eng-NG-31-9999");
        final Tac2013WebIO instance = new Tac2013WebIO();

        final List<WebDocument> doc = instance.readAll(Resources.asByteSource(url));
        System.out.println(doc.get(0));
        assertNotNull(doc);
        assertEquals(4, doc.size());
    }

    @Test
    public void testParseWeb_UsenetCompressed() throws IOException, ParsingException, SAXException {
        final URL url = getResource("eng-NG-31-9999.gz");
        final Tac2013WebIO instance = new Tac2013WebIO();

        final List<WebDocument> doc = instance.readAll(IOUtils.asGzipByteSource(Resources.asByteSource(url)));
        System.out.println(doc.get(0));
        assertNotNull(doc);
        assertEquals(4, doc.size());
    }

    @Test
    public void testParseWeb_BlogUncompressed() throws IOException, ParsingException, SAXException {
        final URL url = getResource("eng-WL-11-9923");
        System.out.println(url);
        final Tac2013WebIO instance = new Tac2013WebIO();

        final List<WebDocument> doc = instance.readAll(Resources.asByteSource(url));
        System.out.println(doc.get(0));
        assertNotNull(doc);
        assertEquals(243, doc.size());
    }

    @Test
    public void testParseWeb_BlogCompressed() throws IOException, ParsingException, SAXException {
        final URL url = getResource("eng-WL-11-9923.gz");
        final Tac2013WebIO instance = new Tac2013WebIO();

        final List<WebDocument> doc = instance.readAll(IOUtils.asGzipByteSource(Resources.asByteSource(url)));
        System.out.println(doc.get(0));
        assertNotNull(doc);
        assertEquals(243, doc.size());
    }

    @Test
    public void testParseWeb_Example() throws IOException, ParsingException, SAXException {

        final String xmlData =
                "<DOC>\n" +
                        "<DOCID> eng-NG-31-150703-10646134 </DOCID>\n" +
                        "<DOCTYPE SOURCE=\"usenet\"> USENET TEXT </DOCTYPE>\n" +
                        "<DATETIME> 2008-04-06T16:08:59 </DATETIME>\n" +
                        "<BODY>\n" +
                        "<HEADLINE>\n" +
                        "more energy\n" +
                        "</HEADLINE>\n" +
                        "<TEXT>\n" +
                        "<POST>\n" +
                        "<POSTER> \"Bryant &amp; Kathy Murray\" &lt;wellnessresto...@gmail.com&gt; </POSTER>\n" +
                        "<POSTDATE> 2008-04-06T16:08:59 </POSTDATE>\n" +
                        "show details 10:52 AM (1 hour ago) Reply\n" +
                        "\n" +
                        "Access Bars or Drinks In April!\n" +
                        "\n" +
                        "Have you tried Melaleuca's Access Bars or Drinks yet?  The Access\n" +
                        "products are a great benefit to helping you reach your fitness goals.\n" +
                        "Add the them to your April order!\n" +
                        "Only Melaleuca has it - patented food technology that gives you quick\n" +
                        "food energy and better utilization of fat.\n" +
                        "Developed by top researcher Dr. Larry Wang, Access helps you get more\n" +
                        "from your workout. You'll have more energy, recover faster, and feel\n" +
                        "the difference even with modest activity such as mowing the lawn,\n" +
                        "walking, or doing everyday household chores.\n" +
                        "Enjoy your favorite flavor at least 15 minutes before any physical\n" +
                        "activity, and you'll really notice the difference!\n" +
                        "Take an Access Bar to baseball practice, to the gym, or on a bike\n" +
                        "ride. After eating one, you'll notice that you have more energy. No\n" +
                        "candy bar or sports drink can do that! With Access Bars, you get the\n" +
                        "best performance - guaranteed!\n" +
                        "\n" +
                        "Discover\n" +
                        "\n" +
                        "our solution to enhance total wellness in almost every aspect of a\n" +
                        "person's life.\n" +
                        "\n" +
                        "1/2 Price Memberships in April From April 1 until April 22nd!\n" +
                        "\n" +
                        "Share all the benefits of being a Preferred Customer for ONLY $14.50 -\n" +
                        "(U.S. &amp; Canada)\n" +
                        "1/2 Price Memberships in April From April 1 until April 22nd!\n" +
                        "\n" +
                        "Join our wellness group @ wellnessrestored@googlegroups.com\n" +
                        "http://www.melaleuca.com/PS/pdf_info/us_pib/US_AccessPIB0107.pdf\n" +
                        "\n" +
                        "Bryant &amp; Kathy Murray\n" +
                        "WellnessRestored Executives\n" +
                        "</POST>\n" +
                        "</TEXT>\n" +
                        "</BODY>\n" +
                        "</DOC>";

        WebDocument expected = new WebDocument(
                "eng-NG-31-150703-10646134",
                Optional.of("more energy"),
                "USENET TEXT",
                WebDocument.Source.usenet,
                new DateTime("2008-04-06T16:08:59"),
                ImmutableList.<WebDocument.Post>of(
                        new WebDocument.Post(
                                "\"Bryant &amp; Kathy Murray\" &lt;wellnessresto...@gmail.com&gt;",
                                Optional.of(new DateTime("2008-04-06T16:08:59")),
                                "show details 10:52 AM (1 hour ago) Reply\n" +
                                        "\n" +
                                        "Access Bars or Drinks In April!\n" +
                                        "\n" +
                                        "Have you tried Melaleuca's Access Bars or Drinks yet?  The Access\n" +
                                        "products are a great benefit to helping you reach your fitness goals.\n" +
                                        "Add the them to your April order!\n" +
                                        "Only Melaleuca has it - patented food technology that gives you quick\n" +
                                        "food energy and better utilization of fat.\n" +
                                        "Developed by top researcher Dr. Larry Wang, Access helps you get more\n" +
                                        "from your workout. You'll have more energy, recover faster, and feel\n" +
                                        "the difference even with modest activity such as mowing the lawn,\n" +
                                        "walking, or doing everyday household chores.\n" +
                                        "Enjoy your favorite flavor at least 15 minutes before any physical\n" +
                                        "activity, and you'll really notice the difference!\n" +
                                        "Take an Access Bar to baseball practice, to the gym, or on a bike\n" +
                                        "ride. After eating one, you'll notice that you have more energy. No\n" +
                                        "candy bar or sports drink can do that! With Access Bars, you get the\n" +
                                        "best performance - guaranteed!\n" +
                                        "\n" +
                                        "Discover\n" +
                                        "\n" +
                                        "our solution to enhance total wellness in almost every aspect of a\n" +
                                        "person's life.\n" +
                                        "\n" +
                                        "1/2 Price Memberships in April From April 1 until April 22nd!\n" +
                                        "\n" +
                                        "Share all the benefits of being a Preferred Customer for ONLY $14.50 -\n" +
                                        "(U.S. &amp; Canada)\n" +
                                        "1/2 Price Memberships in April From April 1 until April 22nd!\n" +
                                        "\n" +
                                        "Join our wellness group @ wellnessrestored@googlegroups.com\n" +
                                        "http://www.melaleuca.com/PS/pdf_info/us_pib/US_AccessPIB0107.pdf\n" +
                                        "\n" +
                                        "Bryant &amp; Kathy Murray\n" +
                                        "WellnessRestored Executives\n"))
        );

        final Tac2013WebIO instance = new Tac2013WebIO();

        final List<WebDocument> doc = instance.readAll(ByteStreams.asByteSource(xmlData.getBytes("UTF-8")));

        System.out.println(doc.get(0));

        assertNotNull(doc);
        assertEquals(1, doc.size());
        assertEquals(expected, doc.get(0));
    }

    @Test
    public void testParseWeb_RawAmpersand() throws IOException, ParsingException, SAXException {

        final String xmlData =
                "<DOC>\n" +
                        "<DOCID> eng-NG-31-150703-10646134 </DOCID>\n" +
                        "<DOCTYPE SOURCE=\"usenet\"> USENET TEXT </DOCTYPE>\n" +
                        "<DATETIME> 2008-04-06T16:08:59 </DATETIME>\n" +
                        "<BODY>\n" +
                        "<HEADLINE>\n" +
                        "more energy\n" +
                        "</HEADLINE>\n" +
                        "<TEXT>\n" +
                        "<POST>\n" +
                        "<POSTER> \"Bryant & Kathy Murray\" &lt;wellnessresto...@gmail.com&gt; </POSTER>\n" +
                        "Have you tried Melaleuca's Access Bars or Drinks yet?  The Access\n" +
                        "products are a great benefit to helping you reach your fitness goals.\n" +
                        "Add the them to your April order!\n" +
                        "</POST>\n" +
                        "</TEXT>\n" +
                        "</BODY>\n" +
                        "</DOC>";

        System.out.println(xmlData);
        WebDocument expected = new WebDocument(
                "eng-NG-31-150703-10646134",
                Optional.of("more energy"),
                "USENET TEXT",
                WebDocument.Source.usenet,
                new DateTime("2008-04-06T16:08:59"),
                ImmutableList.<WebDocument.Post>of(
                        new WebDocument.Post(
                                "\"Bryant & Kathy Murray\" &lt;wellnessresto...@gmail.com&gt;",
                                Optional.of(new DateTime("2008-04-06T16:08:59")),
                                "Have you tried Melaleuca's Access Bars or Drinks yet?  The Access\n" +
                                        "products are a great benefit to helping you reach your fitness goals.\n" +
                                        "Add the them to your April order!\n"))
        );

        final Tac2013WebIO instance = new Tac2013WebIO();

        final List<WebDocument> doc = instance.readAll(ByteStreams.asByteSource(xmlData.getBytes()));

        System.out.println(doc.get(0));

        assertNotNull(doc);
        assertEquals(1, doc.size());
        assertEquals(expected, doc.get(0));
    }

    @Test
    public void testParseWeb_RawLessThanSign() throws IOException, ParsingException, SAXException {

        final String xmlData = "<DOC>\n" +
                "    <DOCID> eng-WL-11-99234-11875223 </DOCID>\n" +
                "    <DOCTYPE SOURCE=\"blog\"> BLOG TEXT </DOCTYPE>\n" +
                "    <DATETIME> 2009-03-31T00:00:00 </DATETIME>\n" +
                "    <BODY>\n" +
                "    <HEADLINE>\n" +
                "    Israeli Nationality and Drug Abuse\n" +
                "            </HEADLINE>\n" +
                "    <TEXT>\n" +
                "    <POST>\n" +
                "    <POSTER> andie531 </POSTER>\n" +
                "    <POSTDATE> 2009-03-31T00:00:00 </POSTDATE>\n" +
                "    Segev L , Paz A , Potasman I .\n" +
                "    http://www.ncbi.nlm.nih.gov/pubmed/16086895\n" +
                "\n" +
                "    Infectious Diseases and Travel Clinic, Bnai ion Medical Centre, Haifa, Israel.\n" +
                "\n" +
                "    BACKGROUND: Drug abuse constitutes a major sociomedical problem throughout the world. A unique subgroup with a higher potential of drug abuse are young travelers to Southeast Asia.\n" +
                "\n" +
                "    Less than a handful of studies have focused on this population, and even fewer have been carried out on site. Our aim was to characterize the phenomenon of drug abuse among Israelis and other nationals, and to define risk factors that would predict which travelers are prone to abusing drugs.\n" +
                "\n" +
                "            METHODS: Data was collected through questionnaires that were distributed in Southeast Asia to 430 travelers. Medical students administered the questionnaires across India, Thailand, Nepal, Vietnam, and Laos during 2002 and 2003.\n" +
                "\n" +
                "    RESULTS: Questionnaires from 231 Israelis and 199 other nationals (mostly from the United Kingdom, Sweden, Australia, and Germany) were analyzed. These travelers had a mean age of 25.3 years. We found that 54.3% of the travelers abused drugs during the trip.\n" +
                "\n" +
                "    Israelis (66.2%) abused drugs more frequently than did non-Israelis (40.7%, p < .001).\n" +
                "\n" +
                "    Males abused drugs significantly more than females did, as did secular more than religious people; however, those with an academic degree abused drugs less than others.\n" +
                "\n" +
                "    For 23.5% of the Israelis, the trip was their first encounter with drugs. Of the entire cohort, 72% abused cannabis products, and most of them (49.6%) did it on daily basis. The use of \"hard\" drugs (eg, lysergic acid diethylamide) was more common among non-Israelis than among the Israelis (37% and 20%, respectively; p < .006).\n" +
                "\n" +
                "    Much higher rates of drug abuse (70.1%) were found in India than in other Southeast Asian countries.\n" +
                "\n" +
                "    Logistic regression identified that prior use of drugs, Israeli nationality, travel to India, cigarette smoking, and traveling alone were significant predictors of drug abuse.\n" +
                "\n" +
                "    CONCLUSIONS: There is a disturbingly high rate of drug abuse in travelers to certain Southeast Asian countries, both among Israeli and other nationals. For many youngsters, this is their first encounter with drugs, and many plan to continue abusing drugs upon their repatriation. Travelers to Southeast Asia should be a major target group for primary, preventive, antidrug campaigns worldwide.\n" +
                "    </POST>\n" +
                "    </TEXT>\n" +
                "    </BODY>\n" +
                "    </DOC>";

        System.out.println(xmlData);
        WebDocument expected = new WebDocument(
                "eng-WL-11-99234-11875223",
                Optional.of("Israeli Nationality and Drug Abuse"),
                "BLOG TEXT",
                WebDocument.Source.blog,
                new DateTime("2009-03-31T00:00:00"),
                ImmutableList.<WebDocument.Post>of(
                        new WebDocument.Post(
                                "andie531",
                                Optional.of(new DateTime("2009-03-31T00:00:00")),
                                "Segev L , Paz A , Potasman I .\n" +
                                        "    http://www.ncbi.nlm.nih.gov/pubmed/16086895\n" +
                                        "\n" +
                                        "    Infectious Diseases and Travel Clinic, Bnai ion Medical Centre, Haifa, Israel.\n" +
                                        "\n" +
                                        "    BACKGROUND: Drug abuse constitutes a major sociomedical problem throughout the world. A unique subgroup with a higher potential of drug abuse are young travelers to Southeast Asia.\n" +
                                        "\n" +
                                        "    Less than a handful of studies have focused on this population, and even fewer have been carried out on site. Our aim was to characterize the phenomenon of drug abuse among Israelis and other nationals, and to define risk factors that would predict which travelers are prone to abusing drugs.\n" +
                                        "\n" +
                                        "            METHODS: Data was collected through questionnaires that were distributed in Southeast Asia to 430 travelers. Medical students administered the questionnaires across India, Thailand, Nepal, Vietnam, and Laos during 2002 and 2003.\n" +
                                        "\n" +
                                        "    RESULTS: Questionnaires from 231 Israelis and 199 other nationals (mostly from the United Kingdom, Sweden, Australia, and Germany) were analyzed. These travelers had a mean age of 25.3 years. We found that 54.3% of the travelers abused drugs during the trip.\n" +
                                        "\n" +
                                        "    Israelis (66.2%) abused drugs more frequently than did non-Israelis (40.7%, p &lt; .001).\n" +
                                        "\n" +
                                        "    Males abused drugs significantly more than females did, as did secular more than religious people; however, those with an academic degree abused drugs less than others.\n" +
                                        "\n" +
                                        "    For 23.5% of the Israelis, the trip was their first encounter with drugs. Of the entire cohort, 72% abused cannabis products, and most of them (49.6%) did it on daily basis. The use of \"hard\" drugs (eg, lysergic acid diethylamide) was more common among non-Israelis than among the Israelis (37% and 20%, respectively; p < .006).\n" +
                                        "\n" +
                                        "    Much higher rates of drug abuse (70.1%) were found in India than in other Southeast Asian countries.\n" +
                                        "\n" +
                                        "    Logistic regression identified that prior use of drugs, Israeli nationality, travel to India, cigarette smoking, and traveling alone were significant predictors of drug abuse.\n" +
                                        "\n" +
                                        "    CONCLUSIONS: There is a disturbingly high rate of drug abuse in travelers to certain Southeast Asian countries, both among Israeli and other nationals. For many youngsters, this is their first encounter with drugs, and many plan to continue abusing drugs upon their repatriation. Travelers to Southeast Asia should be a major target group for primary, preventive, antidrug campaigns worldwide."))
        );

        final Tac2013WebIO instance = new Tac2013WebIO();

        final List<WebDocument> doc = instance.readAll(ByteStreams.asByteSource(xmlData.getBytes()));

        System.out.println(doc.get(0));

        assertNotNull(doc);
        assertEquals(1, doc.size());
        assertEquals(expected, doc.get(0));
    }

    @Test
    public void testParseWeb_ApersandExampleFile() throws IOException, ParsingException, SAXException {
        final URL url = getResource("eng-NG-ampersand-example.xml");
        final Tac2013WebIO instance = new Tac2013WebIO();

        final List<WebDocument> doc = instance.readAll(Resources.asByteSource(url));
        System.out.println(doc.get(0));
        assertNotNull(doc);
        assertEquals(1, doc.size());
    }

    @Test
    public void testPaseBadDate() {
        //  Cannot parse "2009-03-29T01:00:51": Illegal instant due to time zone offset transition (Europe/London)
        String dateString = "2009-03-29T01:00:51";


        DateTime expected =  new DateTime(2009,3,29,1,0,51, DateTimeZone.UTC);

        DateTime actual = ISODateTimeFormat.dateTimeParser().parseLocalDateTime(dateString).toDateTime(DateTimeZone.UTC);



//        DateTime actual =  DateTime.parse(dateString);
        assertEquals(expected, actual);

    }





}
