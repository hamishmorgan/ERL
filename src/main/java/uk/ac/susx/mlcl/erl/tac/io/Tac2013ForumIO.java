package uk.ac.susx.mlcl.erl.tac.io;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import nu.xom.Element;
import nu.xom.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import uk.ac.susx.mlcl.erl.tac.source.ForumDocument;
import uk.ac.susx.mlcl.erl.xml.XomUtil;

/**
 *
 */
public class Tac2013ForumIO extends AbstractTac2013SourceIO<ForumDocument> {

    static final String HEADLINE_ELEMENT_NAME = "headline";
    static final String ID_ATTRIBUTE_NAME = "id";
    static final String POST_ELEMENT_NAME = "post";
    static final String QUOTE_ELEMENT_NAME = "quote";
    private static final Log LOG = LogFactory.getLog(Tac2013ForumIO.class);
    private static final String POST_AUTHOR_ATTRIBUTE_NAME = "author";
    private static final String POST_ID_ATTRIBUTE_NAME = "id";
    private static final String POST_DATE_ATTRIBUTE_NAME = "datetime";
    private static final String QUOTE_ORIGINAL_AUTHOR_ATTRIBUTE_NAME = "orig_author";

    @Override
    protected ForumDocument parseDocElement(Element doc) {
        assert doc.getLocalName().equalsIgnoreCase(DOC_ELEMENT_NAME);

        // read the id attribute
        assert doc.getAttributeCount() == 1;
        final String id = doc.getAttribute(ID_ATTRIBUTE_NAME).getValue();
        LOG.debug("id: " + id);

        // read the headline element

        final Element headlineElement = getFirstChildElementsWhere(doc, elementNameEqualsIgnoreCase(HEADLINE_ELEMENT_NAME));
        final Optional<String> headline = headlineElement != null
                ? Optional.of(XomUtil.getPrintableText(headlineElement))
                : Optional.<String>absent();
        LOG.debug("headline: " + headline);


        final ImmutableList.Builder<ForumDocument.Post> posts = ImmutableList.builder();
        for (int i = 0; i < doc.getChildCount(); i++) {
            final Node child = doc.getChild(i);
            if (!child.getClass().equals(Element.class))
                continue;
            if (((Element) child).getLocalName().equalsIgnoreCase(HEADLINE_ELEMENT_NAME))
                continue;

            if (((Element) child).getLocalName().equalsIgnoreCase(POST_ELEMENT_NAME)) {
                posts.add(parsePost((Element) child));
            } else {
                throw new AssertionError();
            }
        }

        return new ForumDocument(id, headline, posts.build());
    }

    private ForumDocument.Post parsePost(Element post) {
        assert post.getLocalName().equalsIgnoreCase(POST_ELEMENT_NAME);

        assert post.getAttributeCount() == 3;
        final String author = post.getAttribute(POST_AUTHOR_ATTRIBUTE_NAME).getValue();
        final String id = post.getAttribute(POST_ID_ATTRIBUTE_NAME).getValue();
        final DateTime date = new DateTime(post.getAttribute(POST_DATE_ATTRIBUTE_NAME).getValue());

        final ImmutableList.Builder<ForumDocument.Block> blocksBuilder = ImmutableList.builder();
        StringBuilder textBuilder = new StringBuilder();
        for (int i = 0; i < post.getChildCount(); i++) {
            final Node child = post.getChild(i);
            if (child.getClass().equals(Element.class)
                    && ((Element) child).getLocalName().equalsIgnoreCase(QUOTE_ELEMENT_NAME)) {
                if (textBuilder.length() > 0) {
                    blocksBuilder.add(new ForumDocument.Block(textBuilder.toString()));
                    textBuilder = new StringBuilder();
                }
                blocksBuilder.add(parseQuote((Element) child));
            } else {
                textBuilder.append(XomUtil.getPrintableText(child));
            }

        }
        if (textBuilder.length() > 0)
            blocksBuilder.add(new ForumDocument.Block(textBuilder.toString()));

        return new ForumDocument.Post(id, author, date, blocksBuilder.build());
    }

    private ForumDocument.Quote parseQuote(Element quote) {
        assert quote.getLocalName().equalsIgnoreCase(QUOTE_ELEMENT_NAME);

        assert quote.getAttributeCount() == 1;
        final String originalAuthor = quote.getAttribute(QUOTE_ORIGINAL_AUTHOR_ATTRIBUTE_NAME).getValue();

        final String text = XomUtil.getPrintableText(quote);

        return new ForumDocument.Quote(text, originalAuthor);
    }

}
