package uk.ac.susx.mlcl.erl.tac.source;

import com.google.common.base.Optional;
import org.joda.time.DateTime;

import java.util.List;

/**
* Created with IntelliJ IDEA.
* User: hiam20
* Date: 23/07/2013
* Time: 16:11
* To change this template use File | Settings | File Templates.
*/
class WebDocument extends SourceDocument {

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


    private final Type type;
    private final DateTime datetime;
    private final List<Post> posts;

    WebDocument(String id, Optional<String> headline, Type type, DateTime datetime, List<Post> posts) {
        super(id, headline);
        this.type = type;
        this.datetime = datetime;
        this.posts = posts;
    }

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
