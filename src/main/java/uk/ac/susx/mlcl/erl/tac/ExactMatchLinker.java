package uk.ac.susx.mlcl.erl.tac;

import com.google.common.collect.ImmutableList;
import uk.ac.susx.mlcl.erl.linker.Linker;
import uk.ac.susx.mlcl.erl.tac.kb.Entity;
import uk.ac.susx.mlcl.erl.tac.kb.TacKnowledgeBase;
import uk.ac.susx.mlcl.erl.tac.queries.Link;
import uk.ac.susx.mlcl.erl.tac.queries.Query;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 *
 */
public class ExactMatchLinker implements Linker<Query, Link> {

    private final TacKnowledgeBase kb;
    private final Linker<Query, Link> backoff = new NilLinker();

    public ExactMatchLinker(final TacKnowledgeBase kb) {
        this.kb = kb;
    }

    @Override
    public Link link(@Nonnull Query query) throws IOException {
        final Entity entity = kb.getEntityByName(query.getName());
        if (entity != null)
            return new Link(query.getId(),
                    entity.getId(),
                    entity.getType(),
                    false,
                    Genre.forDocumentId(query.getDocId()));
        else
            return backoff.link(query);
    }

    @Nonnull
    @Override
    public Iterable<Link> batchLink(@Nonnull Iterable<Query> queries) throws IOException, ExecutionException {
        final ImmutableList.Builder<Link> links = ImmutableList.builder();
        for(Query query : queries)
            links.add(link(query));
        return links.build();
    }
}
