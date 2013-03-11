/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.t9kb;

import com.google.common.collect.Sets;
import uk.ac.susx.mlcl.erl.lib.C14nCache;
import uk.ac.susx.mlcl.erl.t9kb.T9Entity.Builder;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Instances Tac2009SaxHandler receive SAX events to produce... nothing as yet (A KnowledgeBase
 * builder is instantiated internally, but this is never returned.)
 *
 * @author hiam20
 */
class T9SaxHandler extends DefaultHandler {

    /**
     * Produces only unique instances of CharSequence. Note that this probably isn't entirely safe
     * since CharSequence makes no guarantee of immutability.
     */
    private static final C14nCache<CharSequence> INTERNER = new C14nCache();
    /**
     *
     */
    private static final Logger LOG = Logger.getLogger(T9SaxHandler.class.getName());
    private final Stats stats = new Stats();
    //
    private final ContentHandler rootState = new RootHandler();
    private Stack<ContentHandler> states;
    private TacEntryHandler inner;

    T9SaxHandler(TacEntryHandler inner) {
        this.inner = inner;
        states = new Stack<>();
        states.push(rootState);
    }

    public Stack<ContentHandler> getStates() {
        return states;
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        LOG.log(Level.WARNING, null, e);
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        LOG.log(Level.SEVERE, null, e);
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        LOG.log(Level.SEVERE, null, e);
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

    private class RootHandler extends DefaultHandler {

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            checkQname(qName, "knowledge_base");
            // we can allow knowledge base building to already be open
            checkAttributes(attributes, Collections.<String>emptySet(), Collections.<String>emptySet());

            states.push(new KnowledgeBaseHandler());
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            throw new IllegalStateException();
        }
    };

    private class KnowledgeBaseHandler extends DefaultHandler {

        public KnowledgeBaseHandler() {
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            checkQname(qName, "entity");
            checkAttributes(attributes, Sets.newHashSet("name", "id", "type"), Sets.newHashSet("wiki_title"));

            final String name = attributes.getValue("name");
            final String id = attributes.getValue("id");
            final T9Entity.Type type = T9Entity.Type.valueOf(attributes.getValue("type"));
            final String wikiTitle = attributes.getValue("wiki_title");

            states.push(new EntryHandler(id, name, type, wikiTitle));
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            checkQname(qName, "knowledge_base");

            states.pop();
        }
    }

    private class EntryHandler extends DefaultHandler {

        private final T9Entity.Builder entityBuilder;

        public EntryHandler(String id, String name, T9Entity.Type type, String wikiTitle) {
            entityBuilder = new T9Entity.Builder(id, name, type);
            entityBuilder.setWikiTitle(wikiTitle);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            checkQname(qName, "facts", "wiki_text");
            switch (qName.toLowerCase()) {
                case "facts":
                    final String factsClass = attributes.getValue("class");
                    if (factsClass != null) {
                        entityBuilder.setFactsClass(INTERNER.cached(factsClass));
                    }
                    states.push(new FactsHandler(entityBuilder));
                    break;
                case "wiki_text":
                    states.push(new WikiTextHandler(entityBuilder));
                    break;
                default:
                    throw new AssertionError();
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            checkQname(qName, "entity");

            inner.entry(entityBuilder.build());
            stats.incrementEntityCount();
            states.pop();
        }
    }

    private class FactsHandler extends DefaultHandler {

        private final T9Entity.Builder entityBuilder;

        public FactsHandler(Builder entityBuilder) {
            this.entityBuilder = entityBuilder;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            checkQname(qName, "fact");
            checkAttributes(attributes, Sets.newHashSet("name"), Collections.<String>emptySet());
            final String name = attributes.getValue("name");
            states.push(new FactHandler(entityBuilder, name));
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            checkQname(qName, "facts");
            states.pop();
        }
    };

    private class FactHandler extends DefaultHandler {

        private final T9Entity.Builder entityBuilder;
        private final T9Fact.Builder factBuilder;

        public FactHandler(T9Entity.Builder entityBuilder, String name) {
            this.entityBuilder = entityBuilder;
            this.factBuilder = new T9Fact.Builder(name);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            checkQname(qName, "link");
            checkAttributes(attributes, Collections.<String>emptySet(), Sets.newHashSet("entity_id"));

            final String linkEntityId = attributes.getValue("entity_id");
            states.push(new LinkHandler(linkEntityId, factBuilder));
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            checkQname(qName, "fact");
            entityBuilder.addFact(factBuilder.build());
            stats.incrementFactCount();
            states.pop();
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            factBuilder.appendData(ch, start, length);
        }
    };

    private class LinkHandler extends DefaultHandler {

        private final T9Fact.Builder factBuilder;
        private final T9Link.Builder linkBuilder;

        public LinkHandler(String linkEntityId, T9Fact.Builder factBuilder) {
            this.factBuilder = factBuilder;
            linkBuilder = new T9Link.Builder();
            if (linkEntityId != null) {
                linkBuilder.setEntityId(linkEntityId);
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            throw new SAXException("Unexpected element qname: " + qName);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            //link is a leaf node so we shouldn't find any elements inside
            checkQname(qName, "link");
            factBuilder.addLink(linkBuilder.build());
            states.pop();
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            linkBuilder.appendData(ch, start, length);
        }
    };

    private class WikiTextHandler extends DefaultHandler {

        private T9Entity.Builder entityBuilder;
        private StringBuilder text = new StringBuilder();

        public WikiTextHandler(Builder entityBuilder) {
            this.entityBuilder = entityBuilder;
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {
            //wiki_text is a leaf node so we shouldn't find any elements inside
            throw new SAXException("Unexpected element qname: " + qName);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            checkQname(qName, "wiki_text");
            entityBuilder.setWikiText(INTERNER.cached(text.toString()));
            text = null;
            states.pop();
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            text.append(ch, start, length);
        }
    };

    private static void checkAttributes(Attributes attributes, Set<String> required,
                                        Set<String> optional) throws SAXException {
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

    private static void checkQname(String actual, String... expected) throws SAXException {
        for (String e : expected) {
            if (actual.equalsIgnoreCase(e)) {
                return;
            }
        }
        throw new SAXException("Expected element qName in " + Arrays.toString(expected)
                + ", but found \"" + actual + "\"");
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
}
