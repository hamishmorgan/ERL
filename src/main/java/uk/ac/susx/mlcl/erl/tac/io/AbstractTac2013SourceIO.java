package uk.ac.susx.mlcl.erl.tac.io;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;
import nu.xom.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.erl.lib.IOUtils;
import uk.ac.susx.mlcl.erl.tac.source.SourceDocument;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Abstract super class for source corpora IO implementations.
 *
 * @author Hamish Morgan
 */
public abstract class AbstractTac2013SourceIO<T extends SourceDocument> {

    protected static final String ROOT_ELEMENT_NAME = "root";
    protected static final String DOC_ELEMENT_NAME = "doc";
    private static final Log LOG = LogFactory.getLog(AbstractTac2013SourceIO.class);
    private static final String INPUT_PREFIX = String.format("<?xml version='1.0' encoding='utf8'?>%n<%s>%n", ROOT_ELEMENT_NAME);
    private static final String INPUT_SUFFIX = String.format("%n</%s>", ROOT_ELEMENT_NAME);

    public List<T> readAll(final ByteSource rawSource) throws IOException, ParsingException {
        final ByteSource joinedSource = IOUtils.join(
                ByteStreams.asByteSource(INPUT_PREFIX.getBytes()),
                rawSource,
                ByteStreams.asByteSource(INPUT_SUFFIX.getBytes()));
        final Builder parser = new Builder();
        final Closer closer = Closer.create();
        try {
            LOG.debug("Opening source data xml resource.");
            final InputStream s = closer.register(joinedSource.openBufferedStream());
            LOG.debug("Starting document parse.");
            final Document document = parser.build(s);
            LOG.debug("Document parse available for processing.");
            final Element root = document.getRootElement();
            assert root.getLocalName().equalsIgnoreCase(ROOT_ELEMENT_NAME);
            return parseDocuments(root);
        } catch (ParsingException e) {
            LOG.error(e);
            throw closer.rethrow(e, ParsingException.class);
        } catch (Throwable t) {
            LOG.error(t);
            throw closer.rethrow(t);
        } finally {
            LOG.debug("Closing source xml resource.");
            closer.close();
        }
    }

    private List<T> parseDocuments(final Element root) throws IOException, ParsingException {
        final ImmutableList.Builder<T> listBuilder = ImmutableList.builder();
        for (int i = 0; i < root.getChildCount(); i++) {
            final Node node = root.getChild(i);
            if (node instanceof Element) {
                assert ((Element) node).getLocalName().equalsIgnoreCase(DOC_ELEMENT_NAME);
                listBuilder.add(parseDocElement((Element) node));
            } else if (node instanceof Text) {
                assert node.getValue().trim().isEmpty();
            } else {
                throw new AssertionError("unexpected node: " + node);
            }
        }
        return listBuilder.build();
    }

    abstract T parseDocElement(Element element);


}
