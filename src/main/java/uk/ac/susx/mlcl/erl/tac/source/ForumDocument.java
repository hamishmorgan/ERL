package uk.ac.susx.mlcl.erl.tac.source;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import org.joda.time.DateTime;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 23/07/2013
 * Time: 15:59
 * To change this template use File | Settings | File Templates.
 */
public class ForumDocument extends SourceDocument {


    //    3.2  Discussion Forum Data
    //
    //    Chinese and English discussion forum files use the following markup
    //    framework:
    //
    //    <doc id="{doc_id_string}">
    //    <headline>
    //    ...
    //    </headline>
    //    <post ...>
    //            ...
    //    <quote ...>
    //            ...
    //    </quote>
    //            ...
    //    </post>
    //            ...
    //    </doc>
    //
    //    where there may be arbitrarily deep nesting of quote elements, and
    //    other elements may be present (e.g. "<a...>...</a>" anchor tags).  As
    //    mentioned in section 2 above, each <doc> unit contains at least five
    //    post elements.
    //
    //    If a suitable "container" or "root" tag is applied at the beginning
    //    and end of each *.gz stream, all the discussion forum files are
    //    parseable as XML.
    //
    //    <post author="bitterlyclingin" datetime="2012-04-03T06:25:00" id="p1">


    private final List<Post> posts;

    public ForumDocument(String id, Optional<String> headline, List<Post> posts) {
        super(id, headline);
        this.posts = posts;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public Objects.ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this)
                .add("posts", posts);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ForumDocument that = (ForumDocument) o;

        if (!posts.equals(that.posts)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + posts.hashCode();
        return result;
    }

    public static class Post {
        private final String id;
        private final String author;
        private final DateTime date;
        private final List<Block> paragraphs;

        public Post(String id, String author, DateTime date, List<Block> paragraphs) {
            this.id = id;
            this.author = author;
            this.date = date;
            this.paragraphs = paragraphs;
        }

        public String getId() {
            return id;
        }

        public String getAuthor() {
            return author;
        }

        public DateTime getDate() {
            return date;
        }

        public List<Block> getParagraphs() {
            return paragraphs;
        }

        public Objects.ToStringHelper toStringHelper() {
            return Objects.toStringHelper(this)
                    .add("id", id)
                    .add("author", author)
                    .add("date", date)
                    .add("paragraphs", paragraphs);
        }

        @Override
        public String toString() {
            return toStringHelper().toString();
        }

    }

    public static class Block {

        private final String text;

        public Block(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public Objects.ToStringHelper toStringHelper() {
            return Objects.toStringHelper(this).add("text", text);
        }

        @Override
        public String toString() {
            return toStringHelper().toString();
        }
    }

    public static class Quote extends Block {

        private final String originalAuthor;

        public Quote(String text, String originalAuthor) {
            super(text);
            this.originalAuthor = originalAuthor;
        }

        public String getOriginalAuthor() {
            return originalAuthor;
        }

        public Objects.ToStringHelper toStringHelper() {
            return super.toStringHelper().add("originalAuthor", originalAuthor);
        }
    }
}
