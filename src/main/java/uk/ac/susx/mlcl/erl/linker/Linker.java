package uk.ac.susx.mlcl.erl.linker;

import uk.ac.susx.mlcl.erl.tac.queries.Link;
import uk.ac.susx.mlcl.erl.tac.queries.Query;

import java.io.IOException;

/**
 *
 * @param <Q> Query type
 * @param <L> Link type
 */
public interface Linker<Q,L> {

    L link(Q query) throws IOException;

}
