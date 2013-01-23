/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ac.susx.mlcl.xml.tac2009;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import eu.ac.susx.mlcl.lib.C14nCache;
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
public class Tac2009SaxHandler extends DefaultHandler {

    /**
     * Produces only unique instances of CharSequence. Note that this probably isn't entirely safe
     * since CharSequence makes no guarantee of immutability.
     */
    private static final C14nCache<CharSequence> INTERNER = new C14nCache();
    /**
     *
     */
    private static final Logger LOG = Logger.getLogger(Tac2009SaxHandler.class.getName());
    //
    private final DefaultCollectionFactory factory;
    private final Stats stats = new Stats();
    //
    private Stack<ContentHandler> states;

    public Tac2009SaxHandler(DefaultCollectionFactory dataFactory) {
        this.factory = dataFactory;
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
    //
    private final ContentHandler rootState = new DefaultHandler() {
        private Optional<Tac2009.KnowledgeBase.Builder> kbBuilder = Optional.absent();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (!qName.equalsIgnoreCase("knowledge_base")) {
                throw new SAXException("Unexpected element name: " + qName);
            }
            // we can allow knowledge base building to already be open
            checkAttributes(attributes, Collections.<String>emptySet(), Collections.<String>emptySet());
            if (!kbBuilder.isPresent()) {
                kbBuilder = Optional.of(new Tac2009.KnowledgeBase.Builder(factory));
            }
            states.push(kbState);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            throw new IllegalStateException();
        }
        private final ContentHandler kbState = new DefaultHandler() {
            private Optional<Tac2009.Entity.Builder> entityBuilder = Optional.absent();

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                if (!qName.equalsIgnoreCase("entity")) {
                    throw new SAXException("Unexpected element name: " + qName);
                }
                checkAttributes(attributes, Sets.newHashSet("name", "id", "type"), Sets.newHashSet("wiki_title"));
                final String name = attributes.getValue("name");
                final String id = attributes.getValue("id");
                final Tac2009.Entity.Type type = Tac2009.Entity.Type.valueOf(attributes.getValue("type"));
                entityBuilder = Optional.of(new Tac2009.Entity.Builder(factory, INTERNER.cached(id), INTERNER.cached(name), type));
                final String wikiTitle = attributes.getValue("wiki_title");
                if (wikiTitle != null) {
                    entityBuilder.get().setWikiTitle(INTERNER.cached(wikiTitle));
                }
                states.push(entityState);
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                if (!qName.equalsIgnoreCase("knowledge_base")) {
                    throw new SAXException("Unexpected element name: " + qName);
                }
                states.pop();
            }
            private final ContentHandler entityState = new DefaultHandler() {
                private Optional<Tac2009.Fact.Builder> factBuilder = Optional.absent();
                private Optional<StringBuilder> wikiTextBuilder = Optional.absent();

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    switch (qName.toLowerCase()) {
                        case "facts":
                            final String factsClass = attributes.getValue("class");
                            if (factsClass != null) {
                                entityBuilder.get().setFactsClass(INTERNER.cached(factsClass));
                            }
                            states.push(factsState);
                            break;
                        case "wiki_text":
                            wikiTextBuilder = Optional.of(new StringBuilder());
                            states.push(wikiTextHandler);
                            break;
                        default:
                            throw new SAXException("Unexpected element name: " + qName);
                    }
                }

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
                    if (qName.equalsIgnoreCase("entity")) {
                        kbBuilder.get().addEntity(entityBuilder.get().build());
                        entityBuilder = Optional.absent();
                        stats.incrementEntityCount();
                        states.pop();
                    } else {
                        throw new SAXException("Unexpected element name: " + qName);
                    }
                }
                private final ContentHandler factsState = new DefaultHandler() {
                    @Override
                    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                        if (qName.equalsIgnoreCase("fact")) {
                            checkAttributes(attributes, Sets.newHashSet("name"), Collections.<String>emptySet());
                            final String name = attributes.getValue("name");
                            factBuilder = Optional.of(new Tac2009.Fact.Builder(factory, INTERNER.cached(name)));
                            states.push(factState);
                        } else {
                            throw new SAXException("Unexpected element name: " + qName);
                        }
                    }

                    @Override
                    public void endElement(String uri, String localName, String qName) throws SAXException {
                        if (qName.equalsIgnoreCase("facts")) {
                            states.pop();
                            // Nada
                        } else {
                            throw new SAXException("Unexpected element name: " + qName);
                        }
                    }
                    private final ContentHandler factState = new DefaultHandler() {
                        private Optional<Tac2009.Link.Builder> linkBuilder = Optional.absent();

                        @Override
                        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                            if (qName.equalsIgnoreCase("link")) {
                                checkAttributes(attributes, Collections.<String>emptySet(), Sets.newHashSet("entity_id"));
                                linkBuilder = Optional.of(new Tac2009.Link.Builder(factory));
                                final String linkEntityId = attributes.getValue("entity_id");
                                if (linkEntityId != null) {
                                    linkBuilder.get().setEntityId(INTERNER.cached(linkEntityId));
                                }
                                states.push(linkState);
                            } else {
                                throw new SAXException("Unexpected element name: " + qName);
                            }
                        }

                        @Override
                        public void endElement(String uri, String localName, String qName) throws SAXException {
                            if (qName.equalsIgnoreCase("fact")) {
                                entityBuilder.get().addFact(factBuilder.get().build());
                                factBuilder = Optional.absent();
                                stats.incrementFactCount();
                                states.pop();
                            } else {
                                throw new SAXException("Unexpected element name: " + qName);
                            }
                        }

                        @Override
                        public void characters(char[] ch, int start, int length) throws SAXException {
                            factBuilder.get().appendData(ch, start, length);
                        }
                        private final ContentHandler linkState = new DefaultHandler() {
                            @Override
                            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                                throw new SAXException("Unexpected element name: " + qName);
                            }

                            @Override
                            public void endElement(String uri, String localName, String qName) throws SAXException {
                                if (qName.equalsIgnoreCase("link")) {
                                    factBuilder.get().addLink(linkBuilder.get().build());
                                    linkBuilder = Optional.absent();
                                    states.pop();
                                } else {
                                    throw new SAXException("Unexpected element name: " + qName);
                                }
                            }

                            @Override
                            public void characters(char[] ch, int start, int length) throws SAXException {
                                linkBuilder.get().appendData(ch, start, length);
                            }
                        };
                    };
                };
                private final ContentHandler wikiTextHandler = new DefaultHandler() {
                    @Override
                    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                        throw new SAXException("Unexpected element name: " + qName);
                    }

                    @Override
                    public void endElement(String uri, String localName, String qName) throws SAXException {
                        if (qName.equalsIgnoreCase("wiki_text")) {
                            entityBuilder.get().setWikiText(INTERNER.cached(wikiTextBuilder.get().toString()));
                            wikiTextBuilder = Optional.absent();
                            states.pop();
                        } else {
                            throw new SAXException("Unexpected element name: " + qName);
                        }
                    }

                    @Override
                    public void characters(char[] ch, int start, int length) throws SAXException {
                        wikiTextBuilder.get().append(ch, start, length);
                    }
                };
            };
        };
    };

    private static void checkAttributes(Attributes attributes, Set<String> required, Set<String> optional) throws SAXException {
        if (attributes.getLength() == 0 && required.isEmpty() && optional.isEmpty()) {
            return;
        }
        Set<String> a = Sets.newHashSet();
        for (int i = 0; i < attributes.getLength(); i++) {
            a.add(attributes.getQName(i).toLowerCase());
        }
        Set<String> r = Sets.newHashSet();
        for (String req : required) {
            r.add(req.toLowerCase());
        }
        Set<String> o = Sets.newHashSet();
        for (String opt : optional) {
            o.add(opt.toLowerCase());
        }
        if (Sets.intersection(a, r).size() < r.size()) {
            throw new SAXException("Not all required attributes found");
        }
        if (Sets.union(o, r).size() < a.size()) {
            throw new SAXException("Unhandled attributes.");
        }
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
