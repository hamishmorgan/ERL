/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.t9kb;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author hiam20
 */
public class T9Entity implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type {

        PER, // - PERson
        ORG, // - ORGanization
        GPE, // - GeoPolitical Entity
        UKN // - UnKnowN
        // - UnKnowN
    }
    // Elements
    /**
     * This corresponds to the template name used in the infobox on the entity's wiki page
     */
    private final Optional<String> factsClass;
    /**
     *
     */
    private final Collection<T9Fact> facts; // 1-or-more
    // 1-or-more
    /**
     * wiki_text contains a plain text rendering of the entity's Wikipedia article.
     */
    private final Optional<String> wikiText; // 0-or-1
    // 0-or-1
    // Attributes
    /**
     *
     */
    private final String id; // #REQUIRED
    // #REQUIRED
    /**
     * name is a friendly name used to refer to the entity it should not be assumed to be
     * identical to the Wikipedia name
     */
    private final String name; // #REQUIRED
    // #REQUIRED
    /**
     *
     */
    private final Type type; // #REQUIRED
    // #REQUIRED
    /**
     * This is the canonical title for the entity's Wikipedia article in the October 2008
     * snapshot.
     */
    private final Optional<String> wikiTitle; // #IMPLIED
    // #IMPLIED

    public T9Entity(Optional<String> factsClass, Collection<T9Fact> facts, Optional<String> wikiText, String id, String name, Type type, Optional<String> wikiTitle) {
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

    public Collection<T9Fact> getFacts() {
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
        final T9Entity other = (T9Entity) obj;
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

    public static class Builder {

        String id; // #REQUIRED
        // #REQUIRED
        String name; // #REQUIRED
        // #REQUIRED
        Type type; // #REQUIRED
        // #REQUIRED
        Optional<String> factsClass;
        Optional<String> wikiText; // 0-or-1
        // 0-or-1
        Optional<String> wikiTitle; // #IMPLIED
        // #IMPLIED
        private List<T9Fact> facts; // 1-or-more
        // 1-or-more

        public Builder(String id, String name, Type type) {
            this.id = id;
            this.name = name;
            this.type = type;
            factsClass = Optional.absent();
            wikiText = Optional.absent();
            wikiTitle = Optional.absent();
            facts = Lists.newArrayList();
        }

        public T9Entity build() {
            return new T9Entity(factsClass, facts, wikiText, id, name, type, wikiTitle);
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

        public Builder addFact(T9Fact fact) {
            facts.add(fact);
            return this;
        }
    }

}
