package uk.ac.susx.mlcl.erl.tac.source;

import com.google.common.base.Objects;
import com.google.common.base.Optional;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 23/07/2013
 * Time: 13:08
 * To change this template use File | Settings | File Templates.
 */ //
//
//    void parseDocElement(Element element) {
//        System.out.println(element);
//    }
//
//
//
public abstract class SourceDocument {
    private final String id;
    private final Optional<String> headline;

    public SourceDocument(String id, Optional<String> headline) {
        this.id = id;
        this.headline = headline;
    }

    public String getId() {
        return id;
    }

    public String getHeadline() {
        return headline.get();
    }

    public boolean isHeadlineSet() {
        return headline.isPresent();
    }

    public Objects.ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("headline", headline);
    }

    @Override
    public String toString() {
        return toStringHelper().toString();
    }
}

