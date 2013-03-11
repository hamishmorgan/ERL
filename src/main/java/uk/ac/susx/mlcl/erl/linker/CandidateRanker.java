/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.linker;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author hiam20
 */
public interface CandidateRanker {

    List<String> ranked(List<String> candidates) throws IOException;
}
