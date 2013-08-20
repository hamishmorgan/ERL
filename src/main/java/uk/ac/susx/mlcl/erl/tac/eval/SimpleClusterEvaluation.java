package uk.ac.susx.mlcl.erl.tac.eval;

import uk.ac.susx.mlcl.erl.tac.queries.OutputSet;

/**
* Created with IntelliJ IDEA.
* User: hiam20
* Date: 20/08/2013
* Time: 16:37
* To change this template use File | Settings | File Templates.
*/
public class SimpleClusterEvaluation {

    public static final String NIL_LINK_PREFIX = "NIL";
    protected final OutputSet sys;
    protected final OutputSet gold;
    protected final OutputSet focus;

    public SimpleClusterEvaluation(OutputSet sys, OutputSet gold, OutputSet focus) {
        this.sys = sys;
        this.gold = gold;
        this.focus = focus;
    }

    public int getTrueCount() {
        int num_correct_samples = 0;
        for (String el : gold.getMentionIds()) {
            String gold_kbid = gold.getMentionIndex().get(el).getKbId();
            if (gold_kbid.startsWith(NIL_LINK_PREFIX))
                gold_kbid = NIL_LINK_PREFIX;
            String sys_kbid = sys.getMentionIndex().get(el).getKbId();
            if (sys_kbid.startsWith(NIL_LINK_PREFIX))
                sys_kbid = NIL_LINK_PREFIX;
            if (gold_kbid.equals(sys_kbid))
                ++num_correct_samples;
        }
        return num_correct_samples;
    }

    // kbp2010_microaverage
    public final double accuracy() {
        return getTrueCount() / (double) gold.getMentionCount();
    }

}
