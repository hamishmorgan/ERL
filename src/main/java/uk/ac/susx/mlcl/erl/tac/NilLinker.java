package uk.ac.susx.mlcl.erl.tac;

import uk.ac.susx.mlcl.erl.tac.kb.EntityType;
import uk.ac.susx.mlcl.erl.tac.queries.Link;
import uk.ac.susx.mlcl.erl.tac.queries.Query;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;

/**
* Created with IntelliJ IDEA.
* User: hiam20
* Date: 01/08/2013
* Time: 16:52
* To change this template use File | Settings | File Templates.
*/
public class NilLinker implements Linker {

    private final AtomicInteger nextNilId = new AtomicInteger(1);

    @Nonnull
    @Override
    public Link link(@Nonnull Query query) {
        final String linkId = String.format("NIL%d", nextNilId.getAndIncrement());
        return new Link(query.getId(), linkId, EntityType.UKN, false,
                Genre.forDocumentId(query.getDocId()));
    }
}
