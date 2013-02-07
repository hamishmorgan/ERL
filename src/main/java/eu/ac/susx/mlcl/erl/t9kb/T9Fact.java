/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ac.susx.mlcl.erl.t9kb;

import static com.google.common.base.Preconditions.*;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 *
 * @author hiam20
 */
@Immutable
public class T9Fact implements Serializable {

    private static final long serialVersionUID = 1L;
    private final Collection<CharSequence> links; // 0-or-more
    // 0-or-more
    /**
     * the fact name attribute is identical to the named parameter found in the Wikipedia markup for
     * the infobox on the entity's Wikipedia page
     */
    private final String name; // #REQUIRED
    // #REQUIRED

    public T9Fact(Collection<CharSequence> links, String name) {
        this.links = checkNotNull(links, "links");
        this.name = checkNotNull(name, "name");
    }

    public static class Builder {

        String name;
        private List<CharSequence> links;

        public Builder(String name) {
            this.name = checkNotNull(name);
            links = Lists.newArrayList();
        }

        public Builder addLink(String entity_id, String data) {
            return addLink(new T9Link.Builder().setEntityId(entity_id).appendData(data).build());
        }

        public Builder addLink(String data) {
            return addLink(new T9Link.Builder().appendData(data).build());
        }

        public Builder addLink(T9Link link) {
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

        public T9Fact build() {
            return new T9Fact(links, name);
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
        final T9Fact other = (T9Fact) obj;
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
