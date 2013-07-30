package uk.ac.susx.mlcl.erl.tac.io;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteSource;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.ParsingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.xml.sax.SAXException;
import uk.ac.susx.mlcl.erl.tac.source.WebDocument;
import uk.ac.susx.mlcl.erl.xml.XomUtil;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 25/07/2013
 * Time: 11:15
 * To change this template use File | Settings | File Templates.
 */
public class Tac2013WebIO extends AbstractTac2013SourceIO<WebDocument> {
    private static final Log LOG = LogFactory.getLog(Tac2013NewswireIO.class);
    private static final String ID_ELEMENT_NAME = "DOCID";
    private static final String TYPE_ELEMENT_NAME = "DOCTYPE";
    private static final String SOURCE_ATTRIBUTE_NAME = "SOURCE";
    private static final String DATE_ELEMENT_NAME = "DATETIME";
    private static final String BODY_ELEMENT_NAME = "BODY";
    private static final String HEADLINE_ELEMENT_NAME = "HEADLINE";
    private static final String TEXT_ELEMENT_NAME = "TEXT";
    private static final String POST_ELEMENT_NAME = "POST";
    private static final String POSTER_ELEMENT_NAME = "POSTER";
    private static final String POST_DATE_ELEMENT_NAME = "POSTDATE";

    @Override
    public List<WebDocument> readAll(final ByteSource rawSource) throws IOException, ParsingException, SAXException {
        final ByteSource entityFixingByteSource = new ByteSource() {

            @Override
            public InputStream openStream() throws IOException {
                InputStream in = rawSource.openStream();

//                in =  new ReplacingInputStream(in, "& ".getBytes("UTF-8"), "&amp; ".getBytes("UTF-8"));
//                in =  new ReplacingInputStream(in, " < ".getBytes("UTF-8"), " &lt; ".getBytes("UTF-8"));
//                in =  new ReplacingInputStream(in, " > ".getBytes("UTF-8"), " &gt; ".getBytes("UTF-8"));
//                in =  new ReplacingInputStream(in, "&&".getBytes("UTF-8"), "&amp;&amp;".getBytes("UTF-8"));
//                in =  new ReplacingInputStream(in, ">>".getBytes("UTF-8"), "&gt;&gt;".getBytes("UTF-8"));
//                in =  new ReplacingInputStream(in, "\n&\n".getBytes("UTF-8"), "\n&amp;\n".getBytes("UTF-8"));
                return in;
            }
        };
//        final ByteSource entityFixingByteSource = new ByteSource() {
//
//            @Override
//            public InputStream openStream() throws IOException {
//                return rawSource.openStream();
//            }
//        };

        return super.readAll(entityFixingByteSource);
    }

    @Override
    WebDocument parseDocElement(Element docElement) {

        final String id = getFirstChildElementsWhere(docElement, nameEqualsIgnoreCase(ID_ELEMENT_NAME)).getValue().trim();

        final Element doctypeElement = getFirstChildElementsWhere(docElement, nameEqualsIgnoreCase(TYPE_ELEMENT_NAME));
        final String type = doctypeElement.getValue().trim();


        final WebDocument.Source source = WebDocument.Source.valueOf(doctypeElement.getAttribute(SOURCE_ATTRIBUTE_NAME.toLowerCase()).getValue().trim());

        final DateTime date = parseDateString(
                getFirstChildElementsWhere(docElement, nameEqualsIgnoreCase(DATE_ELEMENT_NAME)).getValue());

        LOG.debug("Processing document id = " + id + ", source = " + source);

        final Element bodyElement = getFirstChildElementsWhere(docElement, nameEqualsIgnoreCase(BODY_ELEMENT_NAME));

        final Element headlineElement = getFirstChildElementsWhere(
                bodyElement, nameEqualsIgnoreCase(HEADLINE_ELEMENT_NAME));
        final Optional<String> headline = headlineElement != null
                ? Optional.of(XomUtil.getPrintableText(headlineElement).trim())
                : Optional.<String>absent();

        final Element textElement = getFirstChildElementsWhere(bodyElement, nameEqualsIgnoreCase(TEXT_ELEMENT_NAME));
        if (textElement == null)
            throw new AssertionError(MessageFormat.format("Failed to find element {0} in web document children: {1}",
                    TEXT_ELEMENT_NAME, getChildrenOf(bodyElement)));

        ImmutableList.Builder postsBuilder = ImmutableList.builder();
        for (final Element postElement : childElementsWhere(textElement, nameEqualsIgnoreCase(POST_ELEMENT_NAME))) {
            postsBuilder.add(parsePost(postElement));
        }

        return new WebDocument(id, headline, type, source, date, postsBuilder.build());
    }

    private WebDocument.Post parsePost(Element postElement) {
        final String poster = getFirstChildElementsWhere(postElement, nameEqualsIgnoreCase(POSTER_ELEMENT_NAME)).getValue().trim();

        final Element dateElement = getFirstChildElementsWhere(postElement, nameEqualsIgnoreCase(POST_DATE_ELEMENT_NAME));
//        if(dateElement == null)
//            throw new AssertionError("Failed to find post date element in " + getChildrenOf(postElement));


        final Optional<DateTime> date = dateElement == null
                ? Optional.<DateTime>absent()
                : Optional.of(parseDateString(dateElement.getValue()));

        final StringBuilder textBuilder = new StringBuilder();
        for (final Node node : childrenOf(postElement)) {
            if (node instanceof Element && (((Element) node).getLocalName().equalsIgnoreCase(POST_ELEMENT_NAME)
                    || ((Element) node).getLocalName().equalsIgnoreCase(POST_DATE_ELEMENT_NAME)))
                continue;
            textBuilder.append(XomUtil.getPrintableText(node));

        }

        return new WebDocument.Post(poster, date, textBuilder.toString().trim());
    }


//    <DOC>
//    <DOCID> eng-NG-31-150703-10646134 </DOCID>
//    <DOCTYPE SOURCE="usenet"> USENET TEXT </DOCTYPE>
//    <DATETIME> 2008-04-06T16:08:59 </DATETIME>
//    <BODY>
//    <HEADLINE>
//            more energy
//    </HEADLINE>
//    <TEXT>
//    <POST>
//    <POSTER> "Bryant & Kathy Murray" &lt;wellnessresto...@gmail.com&gt; </POSTER>
//    <POSTDATE> 2008-04-06T16:08:59 </POSTDATE>
//    show details 10:52 AM (1 hour ago) Reply
//
//    Access Bars or Drinks In April!
//
//    Have you tried Melaleuca's Access Bars or Drinks yet?  The Access
//    products are a great benefit to helping you reach your fitness goals.
//    Add the them to your April order!
//    Only Melaleuca has it - patented food technology that gives you quick
//    food energy and better utilization of fat.
//    Developed by top researcher Dr. Larry Wang, Access helps you get more
//    from your workout. You'll have more energy, recover faster, and feel
//    the difference even with modest activity such as mowing the lawn,
//    walking, or doing everyday household chores.
//    Enjoy your favorite flavor at least 15 minutes before any physical
//    activity, and you'll really notice the difference!
//    Take an Access Bar to baseball practice, to the gym, or on a bike
//    ride. After eating one, you'll notice that you have more energy. No
//    candy bar or sports drink can do that! With Access Bars, you get the
//    best performance - guaranteed!
//
//    Discover
//
//    our solution to enhance total wellness in almost every aspect of a
//    person's life.
//
//            1/2 Price Memberships in April From April 1 until April 22nd!
//
//    Share all the benefits of being a Preferred Customer for ONLY $14.50 -
//            (U.S. &amp; Canada)
//            1/2 Price Memberships in April From April 1 until April 22nd!
//
//    Join our wellness group @ wellnessrestored@googlegroups.com
//    http://www.melaleuca.com/PS/pdf_info/us_pib/US_AccessPIB0107.pdf
//
//    Bryant &amp; Kathy Murray
//    WellnessRestored Executives
//    </POST>
//    </TEXT>
//    </BODY>
//    </DOC>
}
