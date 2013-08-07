/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.tac.kb;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author hiam20
 */
public class Entity implements Serializable {
    private static final long serialVersionUID = 1L;

    // Elements
    /**
     * This corresponds to the template name used in the infobox on the entity's wiki page
     */
    private final Optional<String> factsClass;
    /**
     *
     */
    private final Collection<Fact> facts; // 1-or-more
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
    private final EntityType type; // #REQUIRED
    // #REQUIRED
    /**
     * This is the canonical title for the entity's Wikipedia article in the October 2008
     * snapshot.
     */
    private final Optional<String> wikiTitle; // #IMPLIED
    // #IMPLIED

    public Entity(Optional<String> factsClass, Collection<Fact> facts, Optional<String> wikiText, String id, String name, EntityType type, Optional<String> wikiTitle) {
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

    public EntityType getType() {
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
    public boolean equals(@Nullable Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Entity other = (Entity) obj;
        return !(this.factsClass != other.factsClass
                && (this.factsClass == null || !this.factsClass.equals(other.factsClass)))
                && !(this.facts != other.facts && (this.facts == null || !this.facts.equals(other.facts)))
                && !(this.wikiText != other.wikiText
                && (this.wikiText == null || !this.wikiText.equals(other.wikiText)))
                && !((this.id == null) ? (other.id != null) : !this.id.equals(other.id))
                && !((this.name == null) ? (other.name != null) : !this.name.equals(other.name))
                && this.type == other.type && !(this.wikiTitle != other.wikiTitle
                && (this.wikiTitle == null || !this.wikiTitle.equals(other.wikiTitle)));
    }

    @Nonnull
    @Override
    public String toString() {
        return "Entity{" + "factsClass=" + factsClass + ", facts=" + facts + ", wikiText=" + wikiText + ", id=" + id + ", name=" + name + ", type=" + type + ", wikiTitle=" + wikiTitle + '}';
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {

        final String id; // #REQUIRED
        // #REQUIRED
        final String name; // #REQUIRED
        // #REQUIRED
        final EntityType type; // #REQUIRED
        // #REQUIRED
        Optional<String> factsClass;
        Optional<String> wikiText; // 0-or-1
        // 0-or-1
        Optional<String> wikiTitle; // #IMPLIED
        // #IMPLIED
        private List<Fact> facts; // 1-or-more
        // 1-or-more

        public Builder(String id, String name, EntityType type) {
            this.id = id;
            this.name = name;
            this.type = type;
            factsClass = Optional.absent();
            wikiText = Optional.absent();
            wikiTitle = Optional.absent();
            facts = Lists.newArrayList();
        }

        @Nonnull
        public Entity build() {
            return new Entity(factsClass, facts, wikiText, id, name, type, wikiTitle);
        }

        @Nonnull
        public Builder setFactsClass(String factsClass) {
            this.factsClass = Optional.of(factsClass);
            return this;
        }

        @Nonnull
        public Builder setWikiText(String wiki_text) {
            this.wikiText = Optional.of(wiki_text);
            return this;
        }

        @Nonnull
        public Builder setWikiTitle(String wiki_title) {
            this.wikiTitle = Optional.of(wiki_title);
            return this;
        }

        @Nonnull
        public Builder addFact(Fact fact) {
            facts.add(fact);
            return this;
        }
    }

}
