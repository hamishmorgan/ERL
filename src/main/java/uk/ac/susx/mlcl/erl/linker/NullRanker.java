/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.linker;

import com.beust.jcommander.internal.Lists;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author hiam20
 */
public final class NullRanker<Q,L> implements CandidateRanker<Q,L> {

    @Override
    public List<L> ranked(Q query, Collection<L> candidates) throws IOException {
        return Lists.newArrayList(checkNotNull(candidates, "candidates"));
    }
}
