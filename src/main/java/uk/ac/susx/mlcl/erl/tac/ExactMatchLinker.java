package uk.ac.susx.mlcl.erl.tac;

import uk.ac.susx.mlcl.erl.tac.kb.Entity;
import uk.ac.susx.mlcl.erl.tac.kb.TacKnowledgeBase;
import uk.ac.susx.mlcl.erl.tac.queries.Link;
import uk.ac.susx.mlcl.erl.tac.queries.Query;

import java.io.IOException;

/**
 *
 */
public class ExactMatchLinker implements Linker {

    private final TacKnowledgeBase kb;
    private final Linker backoff = new NilLinker();

    public ExactMatchLinker(final TacKnowledgeBase kb) {
        this.kb = kb;
    }

    @Override
    public Link link(Query query) throws IOException {
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
}
