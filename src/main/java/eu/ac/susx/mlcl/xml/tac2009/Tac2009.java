/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ac.susx.mlcl.xml.tac2009;

import eu.ac.susx.mlcl.lib.C14nCache;
import com.google.common.base.Optional;
import static com.google.common.base.Preconditions.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Abstraction Singularity The Good Intentions Singularity
 *
 * The exact instant, during software development, at which your elegantly designed framework starts
 * to degenerate towards the unmaintainable corrosive mess it was destined to become.
 *
 * Abstraction Singularity: The exact instant, during software development, at which your elegantly
 * designed framework starts to degenerate towards the unmaintainable corrosive mess it was destined
 * to become.
 *
 * @author hiam20
 */
@Nonnull
public class Tac2009 {

    private static final C14nCache<CharSequence> INTERNER = new C14nCache();

    public static void main(String[] args)
            throws ParserConfigurationException, SAXException, IOException {


        File dataDir = new File("/Volumes/LocalScratchHD/LocalHome/Data/TAC 2009 KBP Evaluation Reference/data");

        File mapDbFile = new File("testdb");

        convertXml2Mapdb(dataDir, mapDbFile);


    }

    public static void convertXml2Mapdb(File xmlDir, File mapDb)
            throws ParserConfigurationException, SAXException, IOException {

        final SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        final SAXParser saxParser = saxFactory.newSAXParser();

        DefaultCollectionFactory dataFactory = new MapDBCollectionFactory(mapDb);

        final DefaultHandler handler = new Tac2009SaxHandler(dataFactory);

        final String[] parts = xmlDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches("kb_part-\\d+\\.xml");
            }
        });

        for (String part : parts) {
            System.out.println(part);
            saxParser.parse(new File(xmlDir, part), handler);
        }
    }

    static abstract class AbstractBuilder {

        protected static final DefaultCollectionFactory DEFAULT_FACTORY =
                new DefaultCollectionFactory();
        private final DefaultCollectionFactory factory;

        AbstractBuilder(DefaultCollectionFactory factory) {
            this.factory = factory;
        }

        AbstractBuilder() {
            this(DEFAULT_FACTORY);
        }

        public DefaultCollectionFactory getFactory() {
            return factory;
        }
    }

    public static class KnowledgeBase extends AbstractCollection<Entity> {

        /**
         *
         */
        private final Map<String, Entity> idIndex;

        public KnowledgeBase(Map<String, Entity> idIndex) {
            this.idIndex = checkNotNull(idIndex, "idIndex");
        }

        @Override
        public Iterator<Entity> iterator() {
            return idIndex.values().iterator();
        }

        @Override
        public int size() {
            return idIndex.size();
        }

        @Override
        public int hashCode() {
            return 97 * 7 + (this.idIndex != null ? this.idIndex.hashCode() : 0);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final KnowledgeBase other = (KnowledgeBase) obj;
            return this.idIndex == other.idIndex
                    && (this.idIndex != null && this.idIndex.equals(other.idIndex));
        }

        @Override
        public String toString() {
            return "KnowledgeBase{" + "idIndex=" + idIndex + '}';
        }

        public static class Builder extends AbstractBuilder {

            private Map<String, Entity> idIndex;

            Builder() {
                this(DEFAULT_FACTORY);
            }

            Builder(DefaultCollectionFactory factory) {
                super(factory);
                idIndex = getFactory().newHashMap();
            }

            public Builder addEntity(Entity entity) {
//                System.out.println(entity.getId());
                idIndex.put(entity.getId(), entity);
                return this;
            }

            public KnowledgeBase build() {
                return new KnowledgeBase(idIndex);
            }
        }
    }

    public static class Entity implements Serializable {

        private static final long serialVersionUID = 1L;

        public enum Type {

            PER,// - PERson
            ORG,// - ORGanization
            GPE,// - GeoPolitical Entity
            UKN// - UnKnowN 
        }
        // Elements
        /**
         * This corresponds to the template name used in the infobox on the entity's wiki page
         */
        private final Optional<String> factsClass;
        /**
         *
         */
        private final Collection<Fact> facts; // 1-or-more
        /**
         * wiki_text contains a plain text rendering of the entity's Wikipedia article.
         */
        private final Optional<String> wikiText; // 0-or-1
        // Attributes
        /**
         *
         */
        private final String id; // #REQUIRED
        /**
         * name is a friendly name used to refer to the entity it should not be assumed to be
         * identical to the Wikipedia name
         */
        private final String name; // #REQUIRED
        /**
         *
         */
        private final Type type; // #REQUIRED
        /**
         * This is the canonical title for the entity's Wikipedia article in the October 2008
         * snapshot.
         */
        private final Optional<String> wikiTitle; // #IMPLIED

        public Entity(Optional<String> factsClass, Collection<Fact> facts, Optional<String> wikiText, String id, String name, Type type, Optional<String> wikiTitle) {
            this.factsClass = checkNotNull(factsClass, "factsClass");
            this.facts = checkNotNull(facts, "facts");
            this.wikiText = checkNotNull(wikiText, "wikiText");
            this.id = checkNotNull(id, "id");
            this.name = checkNotNull(name, "name");
            this.type = checkNotNull(type, "type");
            this.wikiTitle = checkNotNull(wikiTitle, "wikiTitle");
        }

        public Optional<String> getFactsClass() {
            return factsClass;
        }

        public Collection<Fact> getFacts() {
            return facts;
        }

        public Optional<String> getWikiText() {
            return wikiText;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Type getType() {
            return type;
        }

        public Optional<String> getWikiTitle() {
            return wikiTitle;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + (this.factsClass != null ? this.factsClass.hashCode() : 0);
            hash = 37 * hash + (this.facts != null ? this.facts.hashCode() : 0);
            hash = 37 * hash + (this.wikiText != null ? this.wikiText.hashCode() : 0);
            hash = 37 * hash + (this.id != null ? this.id.hashCode() : 0);
            hash = 37 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 37 * hash + (this.type != null ? this.type.hashCode() : 0);
            hash = 37 * hash + (this.wikiTitle != null ? this.wikiTitle.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Entity other = (Entity) obj;
            if (this.factsClass != other.factsClass && (this.factsClass == null || !this.factsClass.equals(other.factsClass))) {
                return false;
            }
            if (this.facts != other.facts && (this.facts == null || !this.facts.equals(other.facts))) {
                return false;
            }
            if (this.wikiText != other.wikiText && (this.wikiText == null || !this.wikiText.equals(other.wikiText))) {
                return false;
            }
            if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
                return false;
            }
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            if (this.type != other.type) {
                return false;
            }
            if (this.wikiTitle != other.wikiTitle && (this.wikiTitle == null || !this.wikiTitle.equals(other.wikiTitle))) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Entity{" + "factsClass=" + factsClass + ", facts=" + facts + ", wikiText=" + wikiText + ", id=" + id + ", name=" + name + ", type=" + type + ", wikiTitle=" + wikiTitle + '}';
        }

        public static class Builder extends AbstractBuilder {

            private String id; // #REQUIRED
            private String name; // #REQUIRED
            private Type type; // #REQUIRED
            private Optional<String> factsClass;
            private Optional<String> wikiText; // 0-or-1
            private Optional<String> wikiTitle; // #IMPLIED
            private List<Fact> facts; // 1-or-more

            public Builder(String id, String name, Type type) {
                this(DEFAULT_FACTORY, id, name, type);
            }

            public Builder(DefaultCollectionFactory factory, String id, String name, Type type) {
                super(factory);
                this.id = id;
                this.name = name;
                this.type = type;
                factsClass = Optional.absent();
                wikiText = Optional.absent();
                wikiTitle = Optional.absent();
                facts = getFactory().newArrayList();
            }

            public Entity build() {
                return new Entity(factsClass,
                                  facts,
                                  wikiText,
                                  id,
                                  name,
                                  type,
                                  wikiTitle);
            }

            public Builder setFactsClass(String factsClass) {
                this.factsClass = Optional.of(factsClass);
                return this;
            }

            public Builder setWikiText(String wiki_text) {
                this.wikiText = Optional.of(wiki_text);
                return this;
            }

            public Builder setWikiTitle(String wiki_title) {
                this.wikiTitle = Optional.of(wiki_title);
                return this;
            }

            public Builder addFact(Fact fact) {
                facts.add(fact);
                return this;
            }
        }
    }

    @Immutable
    public static class Fact implements Serializable {

        private static final long serialVersionUID = 1L;
        private final Collection<CharSequence> links; // 0-or-more
        /**
         * the fact name attribute is identical to the named parameter found in the Wikipedia markup
         * for the infobox on the entity's Wikipedia page
         */
        private final String name; // #REQUIRED

        public Fact(Collection<CharSequence> links, String name) {
            this.links = checkNotNull(links, "links");
            this.name = checkNotNull(name, "name");
        }

        public static class Builder extends AbstractBuilder {

            private String name;
            private List<CharSequence> links;

            public Builder(String name) {
                this(DEFAULT_FACTORY, name);
            }

            public Builder(DefaultCollectionFactory factory, String name) {
                super(factory);
                this.name = checkNotNull(name);
                links = getFactory().newArrayList();
            }

            public Builder addLink(String entity_id, String data) {
                return addLink(new Link.Builder().setEntityId(entity_id).appendData(data).build());
            }

            public Builder addLink(String data) {
                return addLink(new Link.Builder().appendData(data).build());
            }

            public Builder addLink(Link link) {
                links.add(link);
                return this;
            }

            public Builder appendData(char[] ch, int start, int length) {
                links.add(String.valueOf(ch, start, length));
                return this;
            }

            public Builder appendData(String str) {
                links.add(str);
                return this;
            }

            public Fact build() {
                return new Fact(
                        links,
                        name);
            }
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 17 * hash + (this.links != null ? this.links.hashCode() : 0);
            hash = 17 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Fact other = (Fact) obj;
            if (this.links != other.links && (this.links == null || !this.links.equals(other.links))) {
                return false;
            }
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Fact{" + "links=" + links + ", name=" + name + '}';
        }
    }

    @Immutable
    public static class Link implements CharSequence, Serializable {

        private static final long serialVersionUID = 1L;
        /**
         * if the entity_id attribute is present on a link element, it indicates that the Wikipedia
         * infobox value linked to an entity that's contained in the knowledge base.
         */
        private final Optional<String> entity_id; // IMPLIED
        /**
         * link elements indicate that a given text string was linked to a Wikipedia page
         */
        private final String data;

        Link(Optional<String> entity_id, String data) {
            this.entity_id = checkNotNull(entity_id, "entity_id");
            this.data = checkNotNull(data, "data");;
        }

        public String getData() {
            return data;
        }

        public Optional<String> getEntityId() {
            return entity_id;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + (this.entity_id != null ? this.entity_id.hashCode() : 0);
            hash = 97 * hash + (this.data != null ? this.data.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final Link other = (Link) obj;
            if (this.entity_id != other.entity_id && (this.entity_id == null || !this.entity_id.equals(other.entity_id))) {
                return false;
            }
            if ((this.data == null) ? (other.data != null) : !this.data.equals(other.data)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return data.toString();
        }

        @Override
        public int length() {
            return data.length();
        }

        @Override
        public char charAt(int index) {
            return data.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return data.subSequence(start, end);
        }

        static class Builder extends AbstractBuilder {

            private Optional<String> entity_id; // IMPLIED
            private StringBuilder data;

            Builder() {
                this(DEFAULT_FACTORY);
            }

            public Builder(DefaultCollectionFactory factory) {
                super(factory);
                entity_id = Optional.absent();
                data = new StringBuilder();
            }

            public Builder setEntityId(String entity_id) {
                this.entity_id = Optional.of(entity_id);
                return this;
            }

            public Builder appendData(char[] ch, int start, int length) {
                data.append(ch, start, length);
                return this;
            }

            public Builder appendData(String str) {
                data.append(str);
                return this;
            }

            public Link build() {
                return new Link(entity_id, INTERNER.cached(data.toString()));
            }
        }
    }
}
