package uk.ac.susx.mlcl.erl.tac;

import com.google.common.base.Optional;
import edu.stanford.nlp.ie.machinereading.structure.Span;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.text.MessageFormat.format;

/**
 * Class representing a single query from a TAC KBP shared task.
 * <p/>
 * A query is the input, or value to be predicted, in the NEL process. Each query consist of a unique ID, a name
 * (the surface text in the source corpora) and the document ID it originated from. From 2012 onwards there is
 * also span associated with each query, which denotes the beginning and end substring offset for the given mention
 * in the source document.
 *
 * @author Hamish Morgan
 */
public class Query {
    @Nonnull
    private final String id;
    @Nonnull
    private final String name;
    @Nonnull
    private final String docId;
    /**
     * The location of the surface-text within the given document.
     */
    @Nonnull
    private final Optional<Span> span;
    /**
     * Gold-standard queries contain the expected output kb id.
     */
    @Nonnull
    private final Optional<String> entityNodeId;

    private Query(@Nonnull final String id,
                  @Nonnull final String name,
                  @Nonnull final String docId,
                  @Nonnull final Optional<Span> span,
                  @Nonnull Optional<String> entityNodeId) {
        checkNotNull(id, "id");
        checkNotNull(name, "name");
        checkNotNull(docId, "docId");
        checkNotNull(span, "span");
        checkNotNull(entityNodeId, "entityNodeId");
        this.id = id;
        this.name = name;
        this.docId = docId;
        this.span = span;
        this.entityNodeId = entityNodeId;
    }


    public Query(final String id, final String name, final String docId, final int beg, final int end) {
        this(id, name, docId, Optional.of(new Span(beg, end)), Optional.<String>absent());
        checkElementIndex(beg, end);
    }

    public Query(final String id, final String name, final String docId, final String entityNodeId) {
        this(id, name, docId, Optional.<Span>absent(), Optional.of(entityNodeId));
    }

    public Query(final String id, final String name, final String docId) {
        this(id, name, docId, Optional.<Span>absent(), Optional.<String>absent());
    }

    @Nonnull
    public String getId() {
        return id;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public String getDocId() {
        return docId;
    }

    public boolean isSpanSet() {
        return span.isPresent();
    }

    public int getBeg() {
        return span.get().start();
    }

    public int getEnd() {
        return span.get().end();
    }

    @Nonnull
    public Span getSpan() {
        return span.get();
    }

    @Nonnull
    public String getEntityNodeId() {
        return entityNodeId.get();
    }

    public boolean isEntityNodeIdSet() {
        return entityNodeId.isPresent();
    }

    @Override
    public String toString() {
        return format("{0}'{'id={1}, name={2}, docId={3}, span={4}'}'",
                this.getClass().getSimpleName(),
                getId(), getName(), getDocId(),
                isSpanSet() ? getSpan() : "<not set>",
                isEntityNodeIdSet() ? getEntityNodeId() : "<not set>");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Query query = (Query) o;

        return docId.equals(query.docId)
                && entityNodeId.equals(query.entityNodeId)
                && id.equals(query.id)
                && name.equals(query.name)
                && span.equals(query.span);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + docId.hashCode();
        result = 31 * result + span.hashCode();
        result = 31 * result + entityNodeId.hashCode();
        return result;
    }
}
