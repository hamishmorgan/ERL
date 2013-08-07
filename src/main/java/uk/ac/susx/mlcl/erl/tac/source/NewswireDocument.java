package uk.ac.susx.mlcl.erl.tac.source;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 23/07/2013
 * Time: 16:10
 * To change this template use File | Settings | File Templates.
 */
@Immutable
public class NewswireDocument extends SourceDocument {

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
    private final Type type;
    private final Optional<String> dateline;
    private final List<String> paragraphs;

    public NewswireDocument(@Nonnull String id, Type type, @Nonnull Optional<String> headline, Optional<String> dateline, List<String> paragraphs) {
        super(id, headline);
        this.dateline = dateline;
        this.paragraphs = paragraphs;
        this.type = type;
    }

    public Objects.ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this)
                .add("type", type)
                .add("dateline", dateline)
                .add("paragraphs", paragraphs);
    }

    public enum Type {
        story,
        advis,
        multi,
        other
    }


    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        NewswireDocument that = (NewswireDocument) o;

        if (!dateline.equals(that.dateline)) return false;
        if (!paragraphs.equals(that.paragraphs)) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + dateline.hashCode();
        result = 31 * result + paragraphs.hashCode();
        return result;
    }
}
