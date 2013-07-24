package uk.ac.susx.mlcl.erl.tac.io;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;
import nu.xom.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.erl.lib.IOUtils;
import uk.ac.susx.mlcl.erl.tac.source.SourceDocument;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

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

    public static Iterable<Node> childrenOf(final Node parent) {
        return new Iterable<Node>() {
            @Override
            public Iterator<Node> iterator() {
                return new Iterator<Node>() {

                    private int nextIndex = 0;

                    @Override
                    public boolean hasNext() {
                        return nextIndex < parent.getChildCount();
                    }

                    @Override
                    public Node next() {
                        if (!hasNext())
                            throw new NoSuchElementException();
                        final Node prev = parent.getChild(nextIndex);
                        ++nextIndex;
                        return prev;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Children iterator does not support removal");
                    }
                };
            }
        };
    }


    public static Iterable<Node> childrenWhere(final Node parent, final Predicate<Node> condition) {
        return new Iterable<Node>() {
            @Override
            public Iterator<Node> iterator() {
                return Iterators.filter(childrenOf(parent).iterator(), condition);
            }
        };
    }

    public static Iterable<Element> childElementsOf(final Node parent) {
        return new Iterable<Element>() {
            @Override
            public Iterator<Element> iterator() {
                return Iterators.transform(
                        childrenWhere(parent, isElement()).iterator(),
                        cast(Node.class, Element.class)
                );
            }
        };

    }

    public static Iterable<Element> childElementsWhere(final Element parent, final Predicate<Element> condition) {
        return new Iterable<Element>() {
            @Override
            public Iterator<Element> iterator() {
                return Iterators.filter(childElementsOf(parent).iterator(), condition);
            }
        };
    }


    public static List<Node> getChildrenWhere(Node parent, Predicate<Node> condition) {
        return ImmutableList.copyOf(childrenWhere(parent, condition));
    }

    public static Node getFirstChildWhere(Node parent, Predicate<Node> condition) {
        return Iterators.getNext(childrenWhere(parent, condition).iterator(), null);
    }

    public static <F, T extends F> Function<F, T> cast(final Class<F> fromCls, final Class<T> toClass) {
        return new Function<F, T>() {
            @Nullable
            @Override
            public T apply(@Nullable F input) {
                return toClass.cast(input);
            }
        };
    }

    public static List<Element> getChildElementsWhere(Node parent, Predicate<Node> condition) {
        return ImmutableList.copyOf(
                Iterators.transform(
                        childrenWhere(parent, Predicates.and(isElement(), condition)).iterator(),
                        cast(Node.class, Element.class)));
    }

    public static
    @Nullable
    Element getFirstChildElementsWhere(Element parent, Predicate<Node> condition) {
        return (Element) getFirstChildWhere(parent, Predicates.and(isElement(), condition));
    }

    static Predicate<Node> isElement() {
        return new Predicate<Node>() {
            @Override
            public boolean apply(@Nullable Node input) {
                return input instanceof Element;
            }
        };
    }

    static Predicate<Node> elementNameEqualsIgnoreCase(final String string) {
        return new Predicate<Node>() {
            @Override
            public boolean apply(@Nullable Node input) {
                Preconditions.checkArgument(input instanceof Element);
                return ((Element) input).getLocalName().equalsIgnoreCase(string);
            }
        };
    }

//    static Predicate<Element> nameEqualsIgnoreCase(final String string) {
//        return new Predicate<Element>() {
//            @Override
//            public boolean apply(@Nullable Element input) {
//                return input.getLocalName().equalsIgnoreCase(string);
//            }
//        };
//    }

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
