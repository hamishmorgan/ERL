package uk.ac.susx.mlcl.erl.tac;

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


    @Nonnull
    private final String queryId;
    @Nonnull
    private final String kbId;
    @Nonnull
    private final EntityType entityType;

    public Link(String queryId, String kbId, EntityType entityType) {
        checkNotNull(queryId, "queryId");
        checkNotNull(kbId, "kbId");
        checkNotNull(entityType, "entityType");
        checkArgument(!queryId.isEmpty(), queryId);
        checkArgument(!kbId.isEmpty(), kbId);

        this.queryId = queryId;
        this.kbId = kbId;
        this.entityType = entityType;
    }

    public String getQueryId() {
        return queryId;
    }

    public String getKbId() {
        return kbId;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    @Override
    public String toString() {
        return format("{0}'{'queryId={1}, kbId={2}, entityType={3}'}'",
                this.getClass().getSimpleName(), getQueryId(), getKbId(), getEntityType());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Link link = (Link) o;

        if (!entityType.equals(link.entityType)) return false;
        if (!kbId.equals(link.kbId)) return false;
        if (!queryId.equals(link.queryId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = queryId.hashCode();
        result = 31 * result + kbId.hashCode();
        result = 31 * result + entityType.hashCode();
        return result;
    }
}
