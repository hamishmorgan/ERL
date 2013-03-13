/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.linker;

import com.beust.jcommander.internal.Lists;

import static com.google.common.base.Preconditions.*;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * @author hiam20
 */
public final class NullRanker implements CandidateRanker {

    @Override
    public List<String> ranked(Collection<String> candidates) throws IOException {
        return Lists.newArrayList(checkNotNull(candidates, "candidates"));
    }
}
