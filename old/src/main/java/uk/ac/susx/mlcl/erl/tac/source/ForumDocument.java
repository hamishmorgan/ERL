package uk.ac.susx.mlcl.erl.tac.source;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import javax.annotation.Nullable;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 23/07/2013
 * Time: 15:59
 * To change this template use File | Settings | File Templates.
 */
@Immutable
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


    @Nonnull
    private final List<Post> posts;

    public ForumDocument(@Nonnull final String id, @Nonnull final Optional<String> headline,
                         @Nonnull final List<Post> posts) {
        super(id, headline);
        this.posts = checkNotNull(posts, "posts");
    }

    @Nonnull
    public List<Post> getPosts() {
        return posts;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", getId())
                .add("headline", isHeadlineSet() ? getHeadline() : "<none>")
                .add("posts", getPosts())
                .toString();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final ForumDocument that = (ForumDocument) o;
        return posts.equals(that.posts);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + posts.hashCode();
        return result;
    }

    @Immutable
    public static class Post {
        @Nonnull
        private final String id;
        @Nonnull
        private final String author;
        @Nonnull
        private final DateTime date;
        @Nonnull
        private final List<Block> paragraphs;

        public Post(@Nonnull final String id, @Nonnull final String author,
                    @Nonnull final DateTime date, @Nonnull final List<Block> paragraphs) {
            this.id = checkNotNull(id, "id");
            this.author = checkNotNull(author, "author");
            this.date = checkNotNull(date, "date");
            this.paragraphs = checkNotNull(paragraphs, "paragraphs");
        }

        @Nonnull
        public String getId() {
            return id;
        }

        @Nonnull
        public String getAuthor() {
            return author;
        }

        @Nonnull
        public DateTime getDate() {
            return date;
        }

        @Nonnull
        public List<Block> getParagraphs() {
            return paragraphs;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Post post = (Post) o;
            return author.equals(post.author) && date.equals(post.date)
                    && id.equals(post.id) && paragraphs.equals(post.paragraphs);
        }

        @Override
        public int hashCode() {
            int result = id.hashCode();
            result = 31 * result + author.hashCode();
            result = 31 * result + date.hashCode();
            result = 31 * result + paragraphs.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("id", getId())
                    .add("author", getAuthor())
                    .add("date", getDate())
                    .add("paragraphs", getParagraphs())
                    .toString();
        }

    }

    @Immutable
    public static class Block {

        @Nonnull
        private final String text;

        public Block(@Nonnull String text) {
            this.text = checkNotNull(text, "text");
        }

        @Nonnull
        public String getText() {
            return text;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Block block = (Block) o;
            return text.equals(block.text);
        }

        @Override
        public int hashCode() {
            return text.hashCode();
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("text", getText())
                    .toString();
        }

    }

    @Immutable
    public static class Quote extends Block {

        @Nonnull
        private final String originalAuthor;

        public Quote(@Nonnull String originalAuthor, @Nonnull String text) {
            super(text);
            this.originalAuthor = checkNotNull(originalAuthor, "originalAuthor");
        }

        @Nonnull
        public String getOriginalAuthor() {
            return originalAuthor;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            final Quote quote = (Quote) o;
            return originalAuthor.equals(quote.originalAuthor);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + originalAuthor.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("originalAuthor", getOriginalAuthor())
                    .add("text", getText())
                    .toString();
        }
    }
}
