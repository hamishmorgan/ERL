package uk.ac.susx.mlcl.erl.tac;

import com.google.common.base.Optional;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.text.MessageFormat.format;

/**
 * Class that represents a single entity link from the TAC KBP shared-task.
 *
 * @author Hamish Morgan
 */
public class Link {


    /**
     * query ID
     */
    @Nonnull
    private final String queryId;
    /**
     * entity node ID
     */
    @Nonnull
    private final String entityNodeId;
    /**
     * entity type (PER, ORG, or GPE) for that entity
     */
    @Nonnull
    private final EntityType entityType;
    /**
     * indicates if the annotator made use of web searches to make the linking judgment
     */
    @Nonnull
    private final Optional<Boolean> webUsed;
    /**
     * indicates the the source genre of the document for the query.
     */
    @Nonnull
    private final Optional<Genre> sourceGenre;

    private Link(String queryId, String entityNodeId, EntityType entityType,
                 Optional<Boolean> webUsed, Optional<Genre> sourceGenre) {
        checkNotNull(queryId, "queryId");
        checkNotNull(entityNodeId, "entityNodeId");
        checkNotNull(entityType, "entityType");
        checkNotNull(webUsed, "webUsed");
        checkNotNull(sourceGenre, "sourceGenre");
        checkArgument(!queryId.isEmpty(), queryId);
        checkArgument(!entityNodeId.isEmpty(), entityNodeId);

        this.queryId = queryId;
        this.entityNodeId = entityNodeId;
        this.entityType = entityType;
        this.webUsed = webUsed;
        this.sourceGenre = sourceGenre;
    }

    public Link(String queryId, String entityNodeId, EntityType entityType) {
        this(queryId, entityNodeId, entityType, Optional.<Boolean>absent(), Optional.<Genre>absent());
    }

    public Link(String queryId, String entityNodeId, EntityType entityType, boolean webUsed, Genre sourceGenre) {
        this(queryId, entityNodeId, entityType, Optional.of(webUsed), Optional.of(sourceGenre));
    }

    public String getQueryId() {
        return queryId;
    }

    public String getEntityNodeId() {
        return entityNodeId;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public Boolean getWebUsed() {
        return webUsed.get();
    }

    public boolean isWebUsedSet() {
        return webUsed.isPresent();
    }

    public Genre getSourceGenre() {
        return sourceGenre.get();
    }

    public boolean isSourceGenreSet() {
        return sourceGenre.isPresent();
    }

    @Override
    public String toString() {
        return format("{0}'{'queryId={1}, entityNodeId={2}, entityType={3}, webUsed={4}, sourceGenre={5}'}'",
                this.getClass().getSimpleName(), getQueryId(), getEntityNodeId(), getEntityType(),
                webUsed.isPresent() ? webUsed.get() : "<not set>",
                sourceGenre.isPresent() ? sourceGenre.get() : "<not set>");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Link link = (Link) o;

        if (!entityNodeId.equals(link.entityNodeId)) return false;
        if (entityType != link.entityType) return false;
        if (!queryId.equals(link.queryId)) return false;
        if (!sourceGenre.equals(link.sourceGenre)) return false;
        if (!webUsed.equals(link.webUsed)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = queryId.hashCode();
        result = 31 * result + entityNodeId.hashCode();
        result = 31 * result + entityType.hashCode();
        result = 31 * result + webUsed.hashCode();
        result = 31 * result + sourceGenre.hashCode();
        return result;
    }
}
