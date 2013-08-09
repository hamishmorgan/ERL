package uk.ac.susx.mlcl.erl.tac.io;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;
import nu.xom.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ccil.cowan.tagsoup.AutoDetector;
import org.ccil.cowan.tagsoup.Parser;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import uk.ac.susx.mlcl.erl.lib.Functions2;
import uk.ac.susx.mlcl.erl.lib.IOUtils;
import uk.ac.susx.mlcl.erl.tac.source.SourceDocument;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.and;
import static com.google.common.collect.Iterators.*;

/**
 * Abstract super class for source corpora IO implementations.
 *
 * @author Hamish Morgan
 */
public abstract class AbstractTac2013SourceIO<T extends SourceDocument> {

    protected static final Charset charset = Charset.forName("UTF-8");
    protected static final String ROOT_ELEMENT_NAME = "root";
    protected static final String DOC_ELEMENT_NAME = "doc";
    private static final Log LOG = LogFactory.getLog(AbstractTac2013SourceIO.class);
    private static final String INPUT_PREFIX = String.format("<?xml version='1.0' encoding='utf8'?>%n<%s>%n", ROOT_ELEMENT_NAME);
    private static final String INPUT_SUFFIX = String.format("%n</%s>", ROOT_ELEMENT_NAME);

    public static Iterable<Node> childrenOf(final Node parent) {
        checkNotNull(parent, "parent");
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
        checkNotNull(parent, "parent");
        checkNotNull(condition, "condition");
        return new Iterable<Node>() {
            @Override
            public Iterator<Node> iterator() {
                return filter(childrenOf(parent).iterator(), condition);
            }
        };
    }

    public static Iterable<Element> childElementsOf(final Node parent) {
        checkNotNull(parent, "parent");
        return new Iterable<Element>() {
            @Override
            public Iterator<Element> iterator() {
                return transform(
                        childrenWhere(parent, isElement()).iterator(),
                        Functions2.cast(Node.class, Element.class)
                );
            }
        };

    }

    public static Iterable<Element> childElementsWhere(final Element parent, final Predicate<Element> condition) {
        checkNotNull(parent, "parent");
        checkNotNull(condition, "condition");
        return new Iterable<Element>() {
            @Override
            public Iterator<Element> iterator() {
                return filter(childElementsOf(parent).iterator(), condition);
            }
        };
    }

    public static List<Node> getChildrenWhere(Node parent, Predicate<Node> condition) {
        checkNotNull(parent, "parent");
        checkNotNull(condition, "condition");
        return ImmutableList.copyOf(childrenWhere(parent, condition));
    }

    public static Node getFirstChildWhere(Node parent, Predicate<Node> condition) {
        checkNotNull(parent, "parent");
        checkNotNull(condition, "condition");
        return getNext(childrenWhere(parent, condition).iterator(), null);
    }

    public static List<Node> getChildrenOf(Node parent) {
        return ImmutableList.copyOf(childrenOf(parent));
    }

    public static List<Element> getChildElementsOf(Node parent) {
        return ImmutableList.copyOf(childElementsOf(parent));
    }


    public static List<Element> getChildElementsWhere(Node parent, Predicate<Node> condition) {
        checkNotNull(parent, "parent");
        checkNotNull(condition, "condition");
        return ImmutableList.copyOf(
                transform(
                        childrenWhere(parent, and(isElement(), condition)).iterator(),
                        Functions2.cast(Node.class, Element.class)));
    }

    public static
    @Nullable
    Element getFirstChildElementsWhere(Element parent, Predicate<Element> condition) {
        checkNotNull(parent, "parent");
        checkNotNull(condition, "condition");
        return getNext(filter(
                transform(childrenWhere(parent, isElement()).iterator(),
                        Functions2.cast(Node.class, Element.class)),
                condition), null);
    }
//
//    static Predicate<Node> elementNameEqualsIgnoreCase(final String string) {
//        return new Predicate<Node>() {
//            @Override
//            public boolean apply(@Nullable Node input) {
//                Preconditions.checkArgument(input instanceof Element);
//                return ((Element) input).getLocalName().equalsIgnoreCase(string);
//            }
//        };
//    }

    static Predicate<Node> isElement() {
        return new Predicate<Node>() {
            @Override
            public boolean apply(@Nullable Node input) {
                return input instanceof Element;
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

    static Predicate<Element> nameEqualsIgnoreCase(final String string) {
        checkNotNull(string, "string");
        return new Predicate<Element>() {
            @Override
            public boolean apply(@Nullable Element input) {
                return input.getLocalName().equalsIgnoreCase(string);
            }
        };
    }

    /**
     *
     * @param dateString
     * @param resolve
     * @return
     */
    protected static DateTime parseDateString(String dateString, DateTime resolve) {
        dateString = dateString.trim();
        if (dateString.startsWith("????-??-??T")) {
                DateTime result = ISODateTimeFormat
                        .timeParser()
                        .parseLocalTime(dateString.substring(11))
                        .toDateTime(resolve);
                while(result.isBefore(resolve)) {
                    result = result.plusDays(1);
                }
            LOG.warn(MessageFormat.format("Parsing partial date-time {0}; resolving after {1} to produce: {2}",
                    dateString, resolve, result));
            return result;
        } else {
            return ISODateTimeFormat
                    .dateTimeParser()
                    .parseLocalDateTime(dateString.trim())
                    .toDateTime(resolve);
        }
    }

    public List<T> readAll(final ByteSource rawSource) throws IOException, ParsingException, SAXException {

//        final ByteSource joinedSource = rawSource;
//
        final ByteSource joinedSource = IOUtils.join(
                ByteStreams.asByteSource(INPUT_PREFIX.getBytes()),
                rawSource,
                ByteStreams.asByteSource(INPUT_SUFFIX.getBytes()));

        XMLReader tagsoup = XMLReaderFactory.createXMLReader("org.ccil.cowan.tagsoup.Parser");

//        tagsoup.setFeature(Parser.defaultAttributesFeature, false);
//        tagsoup.setFeature(Parser.ignoreBogonsFeature, false);

        tagsoup.setProperty(Parser.schemaProperty, new XmlSoupSchema());
        tagsoup.setProperty(Parser.scannerProperty, new XmlSoupScanner());
        tagsoup.setProperty(Parser.autoDetectorProperty, new AutoDetector() {
            @Override
            public Reader autoDetectingReader(InputStream i) {
                return new InputStreamReader(i, charset);
            }
        });
//
//
//        LOG.debug(Objects.toStringHelper(tagsoup)
//                .add(Parser.bogonsEmptyFeature, tagsoup.getFeature(Parser.bogonsEmptyFeature))
//                .add(Parser.CDATAElementsFeature, tagsoup.getFeature(Parser.CDATAElementsFeature))
//                .add(Parser.defaultAttributesFeature, tagsoup.getFeature(Parser.defaultAttributesFeature))
//                .add(Parser.externalGeneralEntitiesFeature, tagsoup.getFeature(Parser.externalGeneralEntitiesFeature))
//                .add(Parser.externalParameterEntitiesFeature, tagsoup.getFeature(Parser.externalParameterEntitiesFeature))
//                .add(Parser.ignorableWhitespaceFeature, tagsoup.getFeature(Parser.ignorableWhitespaceFeature))
//                .add(Parser.ignoreBogonsFeature, tagsoup.getFeature(Parser.ignoreBogonsFeature))
//                .add(Parser.isStandaloneFeature, tagsoup.getFeature(Parser.isStandaloneFeature))
//                .add(Parser.lexicalHandlerParameterEntitiesFeature, tagsoup.getFeature(Parser.lexicalHandlerParameterEntitiesFeature))
//                .add(Parser.namespacePrefixesFeature, tagsoup.getFeature(Parser.namespacePrefixesFeature))
//                .add(Parser.namespacesFeature, tagsoup.getFeature(Parser.namespacesFeature))
//                .add(Parser.resolveDTDURIsFeature, tagsoup.getFeature(Parser.resolveDTDURIsFeature))
//                .add(Parser.restartElementsFeature, tagsoup.getFeature(Parser.restartElementsFeature))
//                .add(Parser.rootBogonsFeature, tagsoup.getFeature(Parser.rootBogonsFeature))
//                .add(Parser.stringInterningFeature, tagsoup.getFeature(Parser.stringInterningFeature))
//                .add(Parser.translateColonsFeature, tagsoup.getFeature(Parser.translateColonsFeature))
////                .add(Parser.unicodeNormalizationCheckingFeature, tagsoup.getFeature(Parser.unicodeNormalizationCheckingFeature))
//                .add(Parser.useAttributes2Feature, tagsoup.getFeature(Parser.useAttributes2Feature))
//                .add(Parser.useEntityResolver2Feature, tagsoup.getFeature(Parser.useEntityResolver2Feature))
//                .add(Parser.useLocator2Feature, tagsoup.getFeature(Parser.useLocator2Feature))
//                .add(Parser.validationFeature, tagsoup.getFeature(Parser.validationFeature))
//                .add(Parser.XML11Feature, tagsoup.getFeature(Parser.XML11Feature))
//                .add(Parser.xmlnsURIsFeature, tagsoup.getFeature(Parser.xmlnsURIsFeature))
//        );
//
//        LOG.debug(Objects.toStringHelper(tagsoup)
//                .add(Parser.autoDetectorProperty, tagsoup.getProperty(Parser.autoDetectorProperty))
//                .add(Parser.lexicalHandlerProperty, tagsoup.getProperty(Parser.lexicalHandlerProperty))
//                .add(Parser.scannerProperty, tagsoup.getProperty(Parser.scannerProperty))
//                .add(Parser.schemaProperty, tagsoup.getProperty(Parser.schemaProperty))
//        );
//


        final Builder parser = new Builder(tagsoup);

//        final Builder parser = new Builder();
        final Closer closer = Closer.create();
        try {
            LOG.debug("Opening source data xml resource.");
            final InputStream s = closer.register(joinedSource.openBufferedStream());
            LOG.debug("Starting document parse.");
            final Document document = parser.build(s);
//            new Serializer(System.out).write(document);

            LOG.debug("Document parse available for processing.");
            final Element root = document.getRootElement();
            assert root.getLocalName().equalsIgnoreCase(ROOT_ELEMENT_NAME);
            return parseDocuments(root);
        } catch (ParsingException e) {
            LOG.error(MessageFormat.format("Parsing excaption at line {0}, column {1}: ",
                    e.getLineNumber(), e.getColumnNumber()), e);
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
