package uk.ac.susx.mlcl.erl.tac;

import com.google.common.collect.ImmutableList;
import io.github.hamishmorgan.erl.linker.Linker;
import uk.ac.susx.mlcl.erl.tac.kb.EntityType;
import uk.ac.susx.mlcl.erl.tac.queries.Link;
import uk.ac.susx.mlcl.erl.tac.queries.Query;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 01/08/2013
 * Time: 16:52
 * To change this template use File | Settings | File Templates.
 */
public class NilLinker implements Linker<Query, Link> {

    private final AtomicInteger nextNilId = new AtomicInteger(1);

    @Nonnull
    @Override
    public Link link(@Nonnull Query query) {
        final String linkId = String.format("NIL%d", nextNilId.getAndIncrement());
        return new Link(query.getId(), linkId, EntityType.UKN, false,
                Genre.forDocumentId(query.getDocId()));
    }

    @Nonnull
    @Override
    public Iterable<Link> batchLink(@Nonnull Iterable<Query> queries) throws IOException, ExecutionException {
        final ImmutableList.Builder<Link> links = ImmutableList.builder();
        for (Query query : queries)
            links.add(link(query));
        return links.build();
    }
}
