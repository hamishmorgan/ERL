package uk.ac.susx.mlcl.erl.tac.source;

import com.google.common.base.Objects;
import com.google.common.base.Optional;

import javax.annotation.Nonnull;

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
    @Nonnull
    private final String id;
    @Nonnull
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SourceDocument that = (SourceDocument) o;

        if (!headline.equals(that.headline)) return false;
        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + headline.hashCode();
        return result;
    }
}

