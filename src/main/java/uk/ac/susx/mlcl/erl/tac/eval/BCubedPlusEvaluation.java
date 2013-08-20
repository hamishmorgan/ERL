package uk.ac.susx.mlcl.erl.tac.eval;

import uk.ac.susx.mlcl.erl.tac.queries.Output;
import uk.ac.susx.mlcl.erl.tac.queries.OutputSet;

import java.util.Map;

/**
* Created with IntelliJ IDEA.
* User: hiam20
* Date: 20/08/2013
* Time: 16:37
* To change this template use File | Settings | File Templates.
*/
public class BCubedPlusEvaluation extends BCubedEvaluation {


    public BCubedPlusEvaluation(OutputSet sys, OutputSet gold, OutputSet focus) {
        super(sys, gold, focus);
    }

    private static final boolean sameLinking(String el_a, String el_b, OutputSet sys, OutputSet gold) {

        Map<String, Output> system_el2kbid = sys.getMentionIndex();
        Map<String, Output> gold_el2kbid = gold.getMentionIndex();
        String sys_el_a_id = system_el2kbid.get(el_a).getKbId();
        String sys_el_b_id = system_el2kbid.get(el_b).getKbId();
        String gol_el_a_id = gold_el2kbid.get(el_a).getKbId();
        String gol_el_b_id = gold_el2kbid.get(el_b).getKbId();

        if (sys_el_a_id.startsWith(NIL_LINK_PREFIX)) sys_el_a_id = NIL_LINK_PREFIX;
        if (sys_el_b_id.startsWith(NIL_LINK_PREFIX)) sys_el_b_id = NIL_LINK_PREFIX;
        if (gol_el_a_id.startsWith(NIL_LINK_PREFIX)) gol_el_a_id = NIL_LINK_PREFIX;
        if (gol_el_b_id.startsWith(NIL_LINK_PREFIX)) gol_el_b_id = NIL_LINK_PREFIX;

        return sys_el_a_id.equals(sys_el_b_id)
                && sys_el_b_id.equals(gol_el_a_id)
                && gol_el_a_id.equals(gol_el_b_id);
    }

    @Override
    protected boolean correctness(String el_a, String el_b, OutputSet sys, OutputSet gold) {
        return inSameSet(el_a, el_b, sys.getMentionIndex())
                && inSameSet(el_a, el_b, gold.getMentionIndex())
                && sameLinking(el_a, el_b, sys, gold);
    }
}
