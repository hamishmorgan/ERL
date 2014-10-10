/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.hamishmorgan.erl.linker;

import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.List;

/**
 * @author hiam20
 */
public final class NullRanker<Q, L> implements CandidateRanker<Q, L> {

    @Override
    public List<L> rankCandidates(Q query, Iterable<L> candidates) throws IOException {
        return ImmutableList.copyOf(candidates);
    }
}
