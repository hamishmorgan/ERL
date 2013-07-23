package uk.ac.susx.mlcl.erl.tac.io;

import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import nu.xom.Builder;
import org.joda.time.DateTime;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import static com.google.common.base.CharMatcher.*;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 22/07/2013
 * Time: 12:35
 * To change this template use File | Settings | File Templates.
 */
public class Tac2013SourceDataIO {

    static class Document {
        Optional<String> headline;
        String id;
    }


//    Newswire data in all three languages use the following markup
//    framework:
//
//    <DOC id="{doc_id_string}" type="{doc_type_label}">
//    <HEADLINE>
//    ...
//    </HEADLINE>
//    <DATELINE>
//    ...
//    </DATELINE>
//    <TEXT>
//    <P>
//    ...
//    </P>
//            ...
//    </TEXT>
//    </DOC>
//
//    where the HEADLINE and DATELINE tags are optional (noet always
//                                                       prsent), and the TEXT content may or may not include "<P> ... </P>"
//    tags (depending on whether or not the "doc_type_label" is "story").
//
//    If a suitable "container" or "root" tag is applied at the beginning
//    and end of each *.gz stream, all the newswire files are parseable as
//    XML.

    static class NewsWireDocument extends Document {

        // E.g: Santiago, May 31, 2009 (AFP)
        Optional<String> dateline;
        List<String> paragraphs;
        Type type;

        enum Type {
            story
        }

    }

//
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

    //<post author="bitterlyclingin" datetime="2012-04-03T06:25:00" id="p1">
    static class DiscussionForumDocument extends Document {

        List<Post> posts;

        static class Post {
            String id;
            String author;
            DateTime date;
            List<Block> paragraphs;
        }

        static class Block {

            String text;
        }

        static class Quote extends Block {

            String originalAuthor;
        }


    }
//
//
//    3.3  Web Document Data
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

    static class WebDocument extends Document {

        Type type;
        DateTime datetime;
        List<Post> posts;

        enum Type {
            usenet,
            blog
        }

        static class Post {
            String poster;
            DateTime postDate;
            String text;
        }

    }
}
