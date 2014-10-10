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
 * Time: 16:11
 * To change this template use File | Settings | File Templates.
 */
@Immutable
public class WebDocument extends SourceDocument {

    //
    //
    //    3.3  Web SourceDocument Data
    //
    //    Chinese and English "web" files use the following markup framework:
    //
    //    <DOC>
    //    <DOCID> {doc_id_string} </DOCID>
    //    <DOCTYPE> ... </DOCTYPE>
    //    <DATETIME> ... </DATETIME>
    //    <BODY>
    //    <HEADLINE>
    //    ...
    //    </HEADLINE>
    //    <TEXT>
    //    <POST>
    //    <POSTER> ... </POSTER>
    //    <POSTDATE> ... </POSTDATE>
    //            ...
    //    </POST>
    //    </TEXT>
    //    </BODY>
    //    </DOC>
    //
    //    Other kinds of tags may be present ("<QUOTE ...>", "<A ...>", etc).
    //
    //    It turns out that many of the *.gz data streams (64 of the 222 Chinese
    //            files, and 359 of the 363 English files) contain material that
    //    interferes with XML parsing (e.g. unescaped "&", or "<QUOTE>" tags
    //            that lack a corresponding "</QUOTE>").
    //


    @Nonnull
    private final String type;
    @Nonnull
    private final Source source;
    @Nonnull
    private final DateTime datetime;
    @Nonnull
    private final List<Post> posts;

    public WebDocument(final @Nonnull String id, final @Nonnull Optional<String> headline,
                       final @Nonnull String type, final @Nonnull Source source, DateTime datetime,
                       final @Nonnull List<Post> posts) {
        super(id, headline);
        this.type = checkNotNull(type, "type");
        this.source = checkNotNull(source, "source");
        this.datetime = checkNotNull(datetime, "datetime");
        this.posts = checkNotNull(posts, "posts");
    }

    @Nonnull
    public String getType() {
        return type;
    }

    @Nonnull
    public Source getSource() {
        return source;
    }

    @Nonnull
    public DateTime getDatetime() {
        return datetime;
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
                .add("type", getType())
                .add("source", getSource())
                .add("datetime", getDatetime())
                .add("posts", getPosts())
                .toString();
    }

    public enum Source {
        usenet,
        blog
    }

    @Immutable
    public static class Post {
        @Nonnull
        private final String poster;
        @Nonnull
        private final Optional<DateTime> date;
        @Nonnull
        private final String text;

        public Post(@Nonnull String poster, @Nonnull Optional<DateTime> date, @Nonnull String text) {
            this.poster = checkNotNull(poster, "poster");
            this.date = checkNotNull(date, "date");
            this.text = checkNotNull(text, "text");
        }

        @Nonnull
        public String getPoster() {
            return poster;
        }

        @Nonnull
        public DateTime getDate() {
            return date.get();
        }

        public boolean isDateSet() {
            return date.isPresent();
        }

        @Nonnull
        public String getText() {
            return text;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Post post = (Post) o;
            return date.equals(post.date) && poster.equals(post.poster) && text.equals(post.text);
        }

        @Override
        public int hashCode() {
            int result = poster.hashCode();
            result = 31 * result + date.hashCode();
            result = 31 * result + text.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("poster", getPoster())
                    .add("date", isDateSet() ? getDate() : "<none>")
                    .add("text", getText())
                    .toString();
        }


    }


//    <DOC>
//    <DOCID> eng-NG-31-150703-10646134 </DOCID>
//    <DOCTYPE SOURCE="usenet"> USENET TEXT </DOCTYPE>
//    <DATETIME> 2008-04-06T16:08:59 </DATETIME>
//    <BODY>
//    <HEADLINE>
//            more energy
//    </HEADLINE>
//    <TEXT>
//    <POST>
//    <POSTER> "Bryant & Kathy Murray" &lt;wellnessresto...@gmail.com&gt; </POSTER>
//    <POSTDATE> 2008-04-06T16:08:59 </POSTDATE>
//    show details 10:52 AM (1 hour ago) Reply
//
//    Access Bars or Drinks In April!
//
//    Have you tried Melaleuca's Access Bars or Drinks yet?  The Access
//    products are a great benefit to helping you reach your fitness goals.
//    Add the them to your April order!
//    Only Melaleuca has it - patented food technology that gives you quick
//    food energy and better utilization of fat.
//    Developed by top researcher Dr. Larry Wang, Access helps you get more
//    from your workout. You'll have more energy, recover faster, and feel
//    the difference even with modest activity such as mowing the lawn,
//    walking, or doing everyday household chores.
//    Enjoy your favorite flavor at least 15 minutes before any physical
//    activity, and you'll really notice the difference!
//    Take an Access Bar to baseball practice, to the gym, or on a bike
//    ride. After eating one, you'll notice that you have more energy. No
//    candy bar or sports drink can do that! With Access Bars, you get the
//    best performance - guaranteed!
//
//    Discover
//
//    our solution to enhance total wellness in almost every aspect of a
//    person's life.
//
//            1/2 Price Memberships in April From April 1 until April 22nd!
//
//    Share all the benefits of being a Preferred Customer for ONLY $14.50 -
//            (U.S. &amp; Canada)
//            1/2 Price Memberships in April From April 1 until April 22nd!
//
//    Join our wellness group @ wellnessrestored@googlegroups.com
//    http://www.melaleuca.com/PS/pdf_info/us_pib/US_AccessPIB0107.pdf
//
//    Bryant &amp; Kathy Murray
//    WellnessRestored Executives
//    </POST>
//    </TEXT>
//    </BODY>
//    </DOC>

}
