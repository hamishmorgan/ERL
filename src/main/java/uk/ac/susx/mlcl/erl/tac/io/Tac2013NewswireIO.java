package uk.ac.susx.mlcl.erl.tac.io;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Text;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.erl.tac.source.NewswireDocument;
import uk.ac.susx.mlcl.erl.xml.XomUtil;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 23/07/2013
 * Time: 16:18
 * To change this template use File | Settings | File Templates.
 */
public class Tac2013NewswireIO extends AbstractTac2013SourceIO<NewswireDocument> {
    private static final Log LOG = LogFactory.getLog(Tac2013NewswireIO.class);
    private static final String ID_ATTRIBUTE_NAME = "id";
    private static final String TYPE_ATTRIBUTE_NAME = "type";
    private static final String HEADLINE_ELEMENT_NAME = "HEADLINE";
    private static final String DATELINE_ELEMENT_NAME = "DATELINE";
    private static final String TEXT_ELEMENT_NAME = "TEXT";
    private static final String PARAGRAPH_ELEMENT_NAME = "P";

    //
    //    <DOC id="AFP_ENG_20090531.0001" type="story" >
    //    <HEADLINE>
    //    Chile swine flu cases jump to 276
    //    </HEADLINE>
    //    <DATELINE>
    //    Santiago, May 31, 2009 (AFP)
    //    </DATELINE>
    //    <TEXT>
    //    <P>
    //    Chilean health authorities confirmed 26 new cases of swine flu on Sunday,
    //    raising the number of patients with A(H1N1) virus in the country to 276, the
    //    highest number on the continent.
    //    </P>
    //    <P>
    //    The sometimes-deadly disease continued its rise across Latin America meanwhile
    //    with Argentina, Brazil, Bolivia, Peru and the Dominican Republic all reporting
    //    new cases.
    //    </P>
    //    <P>
    //    At its last count at the end of last week Mexican authorities said 97 people had
    //    died from the disease and that 4,932 had been infected. Officials however
    //    maintained that the epidemic was on the wane there.
    //    </P>
    //    </TEXT>
    //    </DOC>
    //
    @Nonnull
    @Override
    NewswireDocument parseDocElement(@Nonnull Element doc) {

        assert doc.getAttributeCount() == 2 : "Expected exactly 2 attributes but found " + doc.getAttributeCount();
        final String id = doc.getAttribute(ID_ATTRIBUTE_NAME).getValue();
        final NewswireDocument.Type type = NewswireDocument.Type.valueOf(doc.getAttribute(TYPE_ATTRIBUTE_NAME).getValue());

        LOG.debug("Processing document id = " + id + ", type = " + type);

        final Optional<String> headline = parseHeadlineElement(doc);

        final Optional<String> date = parseDatelineElement(doc);

        final List<String> paragraphs = parseText(doc);

        return new NewswireDocument(id, type, headline, date, paragraphs);
    }

    private Optional<String> parseHeadlineElement(@Nonnull final Element doc) {
        final Element headlineElement = getFirstChildElementsWhere(
                doc, nameEqualsIgnoreCase(HEADLINE_ELEMENT_NAME));
        final Optional<String> headline = headlineElement != null
                ? Optional.of(XomUtil.getPrintableText(headlineElement).trim())
                : Optional.<String>absent();
        LOG.debug("headline: " + headline);
        return headline;
    }

    private Optional<String> parseDatelineElement(@Nonnull final Element doc) {
        final Element datelineElement = getFirstChildElementsWhere(
                doc, nameEqualsIgnoreCase(DATELINE_ELEMENT_NAME));
        return datelineElement == null
                ? Optional.<String>absent()
                : Optional.of(XomUtil.getPrintableText(datelineElement).trim());
    }

    private List<String> parseText(@Nonnull final Element doc) {
        final Element textElement = doc.getFirstChildElement(TEXT_ELEMENT_NAME);
        final ImmutableList.Builder<String> listBuilder = ImmutableList.builder();
        for (final Node child : childrenOf(textElement)) {
            if (child instanceof Text) {
                final String para = child.getValue().trim();
                if(!para.isEmpty())
                    listBuilder.add(para);
            } else if (child instanceof Element) {
                final Element element = (Element) child;
                if (element.getLocalName().equalsIgnoreCase(PARAGRAPH_ELEMENT_NAME)) {
                    final String para = XomUtil.getPrintableText(element).trim();
                    if(!para.isEmpty())
                        listBuilder.add(para);
                } else {
                    throw new AssertionError("Unexpected element name: " + element.getLocalName());
                }
            } else {
                throw new AssertionError("Unexpected child node type: " + child);
            }
        }
        return listBuilder.build();
    }
}
