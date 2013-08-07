package uk.ac.susx.mlcl.erl.tac.queries;

import javax.annotation.Nullable;
import uk.ac.susx.mlcl.erl.tac.Genre;
import uk.ac.susx.mlcl.erl.tac.kb.EntityType;

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
    private final Boolean webSearch;
    /**
     * indicates the the source genre of the document for the query.
     */
    @Nonnull
    private final Genre sourceGenre;

    public Link(@Nonnull String queryId,
                @Nonnull String entityNodeId,
                @Nonnull EntityType entityType,
                @Nonnull Boolean webSearch,
                @Nonnull Genre sourceGenre) {
        checkNotNull(queryId, "queryId");
        checkNotNull(entityNodeId, "entityNodeId");
        checkNotNull(entityType, "entityType");
        checkNotNull(webSearch, "webSearch");
        checkNotNull(sourceGenre, "sourceGenre");
        checkArgument(!queryId.isEmpty(), queryId);
        checkArgument(!entityNodeId.isEmpty(), entityNodeId);

        this.queryId = queryId;
        this.entityNodeId = entityNodeId;
        this.entityType = entityType;
        this.webSearch = webSearch;
        this.sourceGenre = sourceGenre;
    }

    @Nonnull
    public String getQueryId() {
        return queryId;
    }

    @Nonnull
    public String getEntityNodeId() {
        return entityNodeId;
    }

    @Nonnull
    public EntityType getEntityType() {
        return entityType;
    }

    public boolean isWebSearch() {
        return webSearch;
    }

    @Nonnull
    public Genre getSourceGenre() {
        return sourceGenre;
    }

    @Override
    public String toString() {
        return format("{0}'{'queryId={1}, entityNodeId={2}, entityType={3}, webSearch={4}, sourceGenre={5}'}'",
                this.getClass().getSimpleName(), getQueryId(), getEntityNodeId(), getEntityType(),
                isWebSearch(), getSourceGenre());
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Link link = (Link) o;

        return entityNodeId.equals(link.entityNodeId)
                && entityType == link.entityType
                && queryId.equals(link.queryId)
                && sourceGenre.equals(link.sourceGenre)
                && webSearch.equals(link.webSearch);

    }

    @Override
    public int hashCode() {
        int result = queryId.hashCode();
        result = 31 * result + entityNodeId.hashCode();
        result = 31 * result + entityType.hashCode();
        result = 31 * result + webSearch.hashCode();
        result = 31 * result + sourceGenre.hashCode();
        return result;
    }
}
