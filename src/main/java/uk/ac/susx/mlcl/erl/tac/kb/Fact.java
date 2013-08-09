/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.tac.kb;

import com.google.common.collect.Lists;
import javax.annotation.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author hiam20
 */
@Immutable
public class Fact implements Serializable {

    private static final long serialVersionUID = 1L;
    private final Collection<CharSequence> links; // 0-or-more
    // 0-or-more
    /**
     * the fact name attribute is identical to the named parameter found in the Wikipedia markup for
     * the infobox on the entity's Wikipedia page
     */
    private final String name; // #REQUIRED
    // #REQUIRED

    public Fact(Collection<CharSequence> links, String name) {
        this.links = checkNotNull(links, "links");
        this.name = checkNotNull(name, "name");
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {

        final String name;
        private List<CharSequence> links;

        public Builder(String name) {
            this.name = checkNotNull(name);
            links = Lists.newArrayList();
        }

        @Nonnull
        public Builder addLink(String entity_id, String data) {
            return addLink(new Link.Builder().setEntityId(entity_id).appendData(data).build());
        }

        @Nonnull
        public Builder addLink(String data) {
            return addLink(new Link.Builder().appendData(data).build());
        }

        @Nonnull
        public Builder addLink(Link link) {
            links.add(link);
            return this;
        }

        @Nonnull
        public Builder appendData(char[] ch, int start, int length) {
            links.add(String.valueOf(ch, start, length));
            return this;
        }

        @Nonnull
        public Builder appendData(String str) {
            links.add(str);
            return this;
        }

        @Nonnull
        public Fact build() {
            return new Fact(links, name);
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
    public boolean equals(@Nullable Object obj) {
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

    @Nonnull
    @Override
    public String toString() {
        return "Fact{" + "links=" + links + ", name=" + name + '}';
    }
}
