/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.linker;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * @author hiam20
 */
public interface CandidateRanker<Q,L> {

    List<L> ranked(Q query, Collection<L> candidates) throws IOException;
}
