package uk.ac.susx.mlcl.erl.tac.eval;

import com.google.common.collect.ImmutableMap;
import uk.ac.susx.mlcl.erl.tac.queries.Output;
import uk.ac.susx.mlcl.erl.tac.queries.OutputSet;

import java.util.Map;
import java.util.Set;

/**
* Created with IntelliJ IDEA.
* User: hiam20
* Date: 20/08/2013
* Time: 16:37
* To change this template use File | Settings | File Templates.
*/
public class BCubedEvaluation extends SimpleClusterEvaluation {


    public BCubedEvaluation(OutputSet sys, OutputSet gold, OutputSet focus) {
        super(sys, gold, focus);
    }

    protected final boolean inSameSet(String el_a, String el_b, Map<String, Output> el2kbid) {
        return el2kbid.get(el_a).getKbId().equals(el2kbid.get(el_b).getKbId());
    }

    private final Map<String, Double> precision(OutputSet sys, OutputSet gold) {
        ImmutableMap.Builder<String, Double> precisions = ImmutableMap.builder();

        for (String kb_id : sys.getKbIds()) {
            Set<Output> mention_set = sys.getKbIdClusters().get(kb_id);

            for (Output el_a : mention_set) {
                int num_correct = 0;

                for (Output el_b : mention_set) {
                    if (correctness(el_a.getMentionId(), el_b.getMentionId(), sys, gold))
                        num_correct += 1;
                }
                double el_pre = num_correct / (double) mention_set.size();
                precisions.put(el_a.getMentionId(), el_pre);
            }
        }
        return precisions.build();
    }

    protected boolean correctness(String el_a, String el_b, OutputSet sys, OutputSet gold) {
        return inSameSet(el_a, el_b, sys.getMentionIndex())
                && inSameSet(el_a, el_b, gold.getMentionIndex());
    }

    public final double averagePrecision() {
        final Map<String, Double> b2_pre = precision();
        double el_pre_sums = 0.0;
        for (String el_a : focus.getMentionIds())
            el_pre_sums += b2_pre.get(el_a);
        return el_pre_sums / (double) focus.getMentionCount();
    }

    public final double averageRecall() {
        final Map<String, Double> b2_rec = recall();
        double el_rec_sums = 0.0;
        for (String el_a : focus.getMentionIds())
            el_rec_sums += b2_rec.get(el_a);
        return el_rec_sums / (double) focus.getMentionCount();
    }

    public final double microAverageF1Score() {
        final Map<String, Double> b2_pre = precision();
        final Map<String, Double> b2_rec = recall();
        double el_f_sums = 0.0;
        for (String el_a : focus.getMentionIds())
            if (b2_pre.get(el_a) + b2_rec.get(el_a) > 0)
                el_f_sums += 2 * b2_pre.get(el_a) * b2_rec.get(el_a) / (b2_pre.get(el_a) + b2_rec.get(el_a));
        return el_f_sums / (double) focus.getMentionCount();
    }

    public final double macroAverageF1Score() {
        double b2_pre_avg = averagePrecision();
        double b2_rec_avg = averageRecall();
        return (b2_pre_avg + b2_rec_avg > 0)
                ? 2 * b2_pre_avg * b2_rec_avg / (b2_pre_avg + b2_rec_avg)
                : 0.0;
    }

    public final Map<String, Double> precision() {
        return precision(sys, gold);
    }

    public final Map<String, Double> recall() {
        return precision(gold, sys);
    }
}
