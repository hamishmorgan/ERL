package uk.ac.susx.mlcl.erl.tac.source;

import com.google.common.base.Optional;

import java.util.List;

/**
* Created with IntelliJ IDEA.
* User: hiam20
* Date: 23/07/2013
* Time: 16:10
* To change this template use File | Settings | File Templates.
*/
class NewswireDocument extends SourceDocument {

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



    // E.g: Santiago, May 31, 2009 (AFP)
    private final Optional<String> dateline;
    private final List<String> paragraphs;
    private final Type type;

    NewswireDocument(String id, Optional<String> headline, Optional<String> dateline, List<String> paragraphs, Type type) {
        super(id, headline);
        this.dateline = dateline;
        this.paragraphs = paragraphs;
        this.type = type;
    }

    enum Type {
        story
    }

}
