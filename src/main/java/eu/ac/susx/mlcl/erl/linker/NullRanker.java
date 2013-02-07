/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ac.susx.mlcl.erl.linker;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author hiam20
 */
public class NullRanker implements CandidateRanker {

    @Override
    public List<String> ranked(List<String> candidates) throws IOException {
        return candidates;
    }
}
