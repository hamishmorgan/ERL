package uk.ac.susx.mlcl.erl.tac.source;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import javax.annotation.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;

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
@Immutable
public abstract class SourceDocument {
    @Nonnull
    private final String id;
    @Nonnull
    private final Optional<String> headline;

    public SourceDocument(@Nonnull final String id, @Nonnull final Optional<String> headline) {
        this.id = checkNotNull(id, "id");
        this.headline = checkNotNull(headline, "headline");
    }

    @Nonnull
    public String getId() {
        return id;
    }

    @Nonnull
    public String getHeadline() {
        return headline.get();
    }

    public boolean isHeadlineSet() {
        return headline.isPresent();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", getId())
                .add("headline", isHeadlineSet() ? getHeadline() : "<none>")
                .toString();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceDocument that = (SourceDocument) o;
        return headline.equals(that.headline) && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + headline.hashCode();
        return result;
    }
}

