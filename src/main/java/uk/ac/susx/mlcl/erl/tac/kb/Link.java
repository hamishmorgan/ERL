/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.tac.kb;

import com.google.common.base.Optional;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author hiam20
 */
@Immutable
public class Link implements CharSequence, Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * if the entity_id attribute is present on a link element, it indicates that the Wikipedia
     * infobox value linked to an entity that's contained in the knowledge base.
     */
    private final Optional<String> entity_id; // IMPLIED
    // IMPLIED
    /**
     * link elements indicate that a given text string was linked to a Wikipedia page
     */
    private final String data;

    Link(Optional<String> entity_id, String data) {
        this.entity_id = checkNotNull(entity_id, "entity_id");
        this.data = checkNotNull(data, "data");
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
    public boolean equals(@Nullable Object obj) {
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

    @Nonnull
    @Override
    public String toString() {
        return data;
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

    public static class Builder {

        private Optional<String> entity_id; // IMPLIED
        // IMPLIED
        @Nonnull
        private final StringBuilder data;

        public Builder() {
            entity_id = Optional.absent();
            data = new StringBuilder();
        }

        @Nonnull
        public Builder setEntityId(String entity_id) {
            this.entity_id = Optional.of(entity_id);
            return this;
        }

        @Nonnull
        public Builder appendData(char[] ch, int start, int length) {
            data.append(ch, start, length);
            return this;
        }

        @Nonnull
        public Builder appendData(String str) {
            data.append(str);
            return this;
        }

        @Nonnull
        public Link build() {
            return new Link(entity_id, data.toString());
        }
    }
}
