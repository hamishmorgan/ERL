package uk.ac.susx.mlcl.erl.tac.io;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import uk.ac.susx.mlcl.lib.C14nCache;
import uk.ac.susx.mlcl.erl.tac.kb.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.Stack;

import static java.text.MessageFormat.format;


/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 19/07/2013
 * Time: 13:07
 * To change this template use File | Settings | File Templates.
 */
public class Tac2009KnowledgeBaseIO {

    private static final Log LOG = LogFactory.getLog(Tac2009KnowledgeBaseIO.class);

    /**
     * Create a new knowledge base from the given raw source XML file(s).
     * <p/>
     * Path can be a single file, or a directory in which case every file inside is parsed.
     *
     * @param dbFile
     * @param dataPath
     * @return
     * @throws javax.xml.parsers.ParserConfigurationException
     *
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     */
    @Nonnull
    public static TacKnowledgeBase create(File dbFile, @Nonnull File dataPath) throws ParserConfigurationException, SAXException, IOException {

        LOG.info(format("Creating knowledge-base DB, from XML resource {0}, to {1}", dataPath, dbFile));
        LOG.debug("Initializing database.");
        final DB db = openDB(dbFile);
        final HTreeMap<String, Entity> idIndex = db.createHashMap("entity-id-index").keepCounter(true).make();
        final HTreeMap<String, String> nameIndex = db.createHashMap("entity-name-index").keepCounter(true).make();
        final SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        final SAXParser saxParser = saxFactory.newSAXParser();
        final DefaultHandler handler = new Tac2009SaxHandler(new TacEntryHandler() {
            private int count = 0;
            private final StopWatch tic = new StopWatch();

            {
                tic.start();
            }

            @Override
            public void entry(@Nonnull Entity entry) {
                Preconditions.checkNotNull(entry, "entry");

                final String id = entry.getId();
                final String name = entry.getName();
                Preconditions.checkNotNull(id, "id");
                Preconditions.checkNotNull(name, "name");

//                assert idIndex.size() == nameIndex.size();
//                assert idIndex.isEmpty() == nameIndex.isEmpty();
//
//                assert !idIndex.containsKey(id);
//                assert !nameIndex.containsKey(name);

                final Entity idiPutResult = idIndex.put(id, entry);
                final String niPutResult = nameIndex.put(name, id);

//                assert idiPutResult == null;
//                assert niPutResult == null;
//
//                assert idIndex.size() == nameIndex.size();
//                assert idIndex.isEmpty() == nameIndex.isEmpty();

                if (count % 1000 == 0) {
                    db.commit();
                }
                if (count % 10000 == 0) {
                    LOG.info(String.format("Processed %d entities. (%f e/s)%n", count,
                            count / ((tic.getTime() / 1000.0))));
                }
                count++;
            }
        });


        if (!dataPath.exists()) {
            throw new IOException("Data path does not exist: " + dataPath);
        } else if (!dataPath.canRead()) {
            throw new IOException("Data path is not readable: " + dataPath);
        }

        final File[] parts;
        if (dataPath.isDirectory()) {
            LOG.info("Parsing all files in directory: " + dataPath);
            parts = dataPath.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, @Nonnull String name) {
                    return name.matches("kb_part-\\d+\\.xml");
                }
            });
        } else {
            LOG.info("Parsing single file: " + dataPath);
            parts = new File[]{dataPath};
        }

        for (File part : parts) {
            LOG.info("Processing file: " + part);
            saxParser.parse(part, handler);
        }

        LOG.debug("Committing changes.");
        db.commit();

        LOG.debug("Closing database.");
        db.close();

        LOG.debug("All done.");

        return TacKnowledgeBase.open(dbFile);
    }

    public static DB openDB(@Nonnull URL url) throws URISyntaxException {
        if (url.getProtocol().equalsIgnoreCase("file"))
            return openDB(new File(url.toURI()));
        else
            throw new IllegalArgumentException(format("Unsupported protocol in url '{1}'", url));
    }

    public static DB openDB(File dbFile) {
        return DBMaker.newFileDB(dbFile)
                .asyncWriteDisable()
                .writeAheadLogDisable()
                .closeOnJvmShutdown()
                .make();
    }

    /**
     * @author hiam20
     */
    public interface TacEntryHandler {

        void entry(Entity entry);

    }

    /**
     * Instances Tac2009SaxHandler receive SAX events to produce... nothing as yet (A KnowledgeBase
     * builder is instantiated internally, but this is never returned.)
     *
     * @author hiam20
     */
    static class Tac2009SaxHandler extends DefaultHandler {

        /**
         * Produces only unique instances of CharSequence. Note that this probably isn't entirely safe
         * since CharSequence makes no guarantee of immutability.
         */
        private static final C14nCache<CharSequence> INTERNER = new C14nCache<CharSequence>();
        /**
         *
         */
        private static final Log LOG = LogFactory.getLog(Tac2009SaxHandler.class);
        private final Stats stats = new Stats();
        //
        private final ContentHandler rootState = new RootHandler();
        private Stack<ContentHandler> states;
        private final TacEntryHandler inner;

        Tac2009SaxHandler(TacEntryHandler inner) {
            this.inner = inner;
            states = new Stack<ContentHandler>();
            states.push(rootState);
        }

        private static void checkAttributes(@Nonnull Attributes attributes, @Nonnull Set<String> required,
                                            @Nonnull Set<String> optional) throws SAXException {
            if (attributes.getLength() == 0 && required.isEmpty() && optional.isEmpty()) {
                return;
            }
            final Set<String> actualAttrNames = Sets.newHashSet();
            for (int i = 0; i < attributes.getLength(); i++) {
                actualAttrNames.add(attributes.getQName(i).toLowerCase());
            }
            Set<String> expectedAttrNames = Sets.newHashSet();
            for (String req : required) {
                expectedAttrNames.add(req.toLowerCase());
            }
            Set<String> optionalAttrNames = Sets.newHashSet();
            for (String opt : optional) {
                optionalAttrNames.add(opt.toLowerCase());
            }
            if (Sets.intersection(actualAttrNames, expectedAttrNames).size() < expectedAttrNames.size()) {
                throw new SAXException("Not all required attributes found");
            }
            if (Sets.union(optionalAttrNames, expectedAttrNames).size() < actualAttrNames.size()) {
                throw new SAXException("Unhandled attributes.");
            }
        }

        private static void checkQname(@Nonnull String actual, @Nonnull String... expected) throws SAXException {
            for (String e : expected) {
                if (actual.equalsIgnoreCase(e)) {
                    return;
                }
            }
            throw new SAXException("Expected element qName in " + Arrays.toString(expected)
                    + ", but found \"" + actual + "\"");
        }

        public Stack<ContentHandler> getStates() {
            return states;
        }

        @Override
        public void warning(SAXParseException e) throws SAXException {
            LOG.warn("", e);
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            LOG.error("", e);
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            LOG.fatal("", e);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            states.peek().startElement(uri, localName, qName, attributes);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            states.peek().endElement(uri, localName, qName);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            states.peek().characters(ch, start, length);
        }

        public static class Stats {

            private long entityCount = 0;
            private long factCount = 0;

            public long getEntityCount() {
                return entityCount;
            }

            public long getFactCount() {
                return factCount;
            }

            protected void incrementEntityCount() {
                ++entityCount;
            }

            protected void incrementFactCount() {
                ++factCount;
            }
        }

        private class RootHandler extends DefaultHandler {

            @Override
            public void startElement(String uri, String localName, @Nonnull String qName, @Nonnull Attributes attributes) throws SAXException {
                checkQname(qName, "knowledge_base");
                // we can allow knowledge base building to already be open
                checkAttributes(attributes, Collections.<String>emptySet(), Collections.<String>emptySet());

                states.push(new KnowledgeBaseHandler());
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                throw new IllegalStateException();
            }
        }

        private class KnowledgeBaseHandler extends DefaultHandler {

            public KnowledgeBaseHandler() {
            }

            @Override
            public void startElement(String uri, String localName, @Nonnull String qName, @Nonnull Attributes attributes) throws SAXException {
                checkQname(qName, "entity");
                checkAttributes(attributes, Sets.newHashSet("name", "id", "type"), Sets.newHashSet("wiki_title"));

                final String name = attributes.getValue("name");
                final String id = attributes.getValue("id");
                final EntityType type = EntityType.valueOf(attributes.getValue("type"));
                final String wikiTitle = attributes.getValue("wiki_title");

                states.push(new EntryHandler(id, name, type, wikiTitle));
            }

            @Override
            public void endElement(String uri, String localName, @Nonnull String qName) throws SAXException {
                checkQname(qName, "knowledge_base");

                states.pop();
            }
        }

        private class EntryHandler extends DefaultHandler {

            @Nonnull
            private final Entity.Builder entityBuilder;

            public EntryHandler(String id, String name, EntityType type, String wikiTitle) {
                entityBuilder = new Entity.Builder(id, name, type);
                entityBuilder.setWikiTitle(wikiTitle);
            }

            @Override
            public void startElement(String uri, String localName, @Nonnull String qName, @Nonnull Attributes attributes) throws SAXException {
                checkQname(qName, "facts", "wiki_text");
                if (qName.equalsIgnoreCase("facts")) {
                    final String factsClass = attributes.getValue("class");
                    if (factsClass != null) {
                        entityBuilder.setFactsClass(INTERNER.cached(factsClass));
                    }
                    states.push(new FactsHandler(entityBuilder));

                } else if (qName.equalsIgnoreCase("wiki_text")) {
                    states.push(new WikiTextHandler(entityBuilder));
                } else {
                    throw new AssertionError();
                }
            }

            @Override
            public void endElement(String uri, String localName, @Nonnull String qName) throws SAXException {
                checkQname(qName, "entity");

                inner.entry(entityBuilder.build());
                stats.incrementEntityCount();
                states.pop();
            }
        }

        private class FactsHandler extends DefaultHandler {

            private final Entity.Builder entityBuilder;

            public FactsHandler(Entity.Builder entityBuilder) {
                this.entityBuilder = entityBuilder;
            }

            @Override
            public void startElement(String uri, String localName, @Nonnull String qName, @Nonnull Attributes attributes) throws SAXException {
                checkQname(qName, "fact");
                checkAttributes(attributes, Sets.newHashSet("name"), Collections.<String>emptySet());
                final String name = attributes.getValue("name");
                states.push(new FactHandler(entityBuilder, name));
            }

            @Override
            public void endElement(String uri, String localName, @Nonnull String qName) throws SAXException {
                checkQname(qName, "facts");
                states.pop();
            }
        }

        private class FactHandler extends DefaultHandler {

            private final Entity.Builder entityBuilder;
            @Nonnull
            private final Fact.Builder factBuilder;

            public FactHandler(Entity.Builder entityBuilder, String name) {
                this.entityBuilder = entityBuilder;
                this.factBuilder = new Fact.Builder(name);
            }

            @Override
            public void startElement(String uri, String localName, @Nonnull String qName, @Nonnull Attributes attributes) throws SAXException {
                checkQname(qName, "link");
                checkAttributes(attributes, Collections.<String>emptySet(), Sets.newHashSet("entity_id"));

                final String linkEntityId = attributes.getValue("entity_id");
                states.push(new LinkHandler(linkEntityId, factBuilder));
            }

            @Override
            public void endElement(String uri, String localName, @Nonnull String qName) throws SAXException {
                checkQname(qName, "fact");
                entityBuilder.addFact(factBuilder.build());
                stats.incrementFactCount();
                states.pop();
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                factBuilder.appendData(ch, start, length);
            }
        }

        private class LinkHandler extends DefaultHandler {

            private final Fact.Builder factBuilder;
            @Nonnull
            private final Link.Builder linkBuilder;

            public LinkHandler(@Nullable String linkEntityId, Fact.Builder factBuilder) {
                this.factBuilder = factBuilder;
                linkBuilder = new Link.Builder();
                if (linkEntityId != null) {
                    linkBuilder.setEntityId(linkEntityId);
                }
            }

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                throw new SAXException("Unexpected element qname: " + qName);
            }

            @Override
            public void endElement(String uri, String localName, @Nonnull String qName) throws SAXException {
                //link is a leaf node so we shouldn't find any elements inside
                checkQname(qName, "link");
                factBuilder.addLink(linkBuilder.build());
                states.pop();
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                linkBuilder.appendData(ch, start, length);
            }
        }

        private class WikiTextHandler extends DefaultHandler {

            private Entity.Builder entityBuilder;
            @Nullable
            private StringBuilder text = new StringBuilder();

            public WikiTextHandler(Entity.Builder entityBuilder) {
                this.entityBuilder = entityBuilder;
            }

            @Override
            public void startElement(String uri, String localName, String qName,
                                     Attributes attributes) throws SAXException {
                //wiki_text is a leaf node so we shouldn't find any elements inside
                throw new SAXException("Unexpected element qname: " + qName);
            }

            @Override
            public void endElement(String uri, String localName, @Nonnull String qName) throws SAXException {
                checkQname(qName, "wiki_text");
                entityBuilder.setWikiText(INTERNER.cached(text.toString()));
                text = null;
                states.pop();
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                text.append(ch, start, length);
            }
        }
    }


}
