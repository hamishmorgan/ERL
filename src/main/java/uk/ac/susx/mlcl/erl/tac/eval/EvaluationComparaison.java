package uk.ac.susx.mlcl.erl.tac.eval;

import uk.ac.susx.mlcl.erl.tac.queries.OutputSet;

import java.io.IOException;
import java.util.List;

/**
* Created with IntelliJ IDEA.
* User: hiam20
* Date: 20/08/2013
* Time: 16:37
* To change this template use File | Settings | File Templates.
*/
public class EvaluationComparaison {
    private final OutputSet gold;
    private final OutputSet focus;
    private final List<OutputSet> systems;


    public EvaluationComparaison(OutputSet gold, OutputSet focus, List<OutputSet> systems) {
        this.gold = gold;
        this.focus = focus;
        this.systems = systems;
    }

    public void appendEvalationTable(Appendable out) throws IOException {
        out.append("system\tKBP2010 micro-average\tB^3 Precision\tB^3 Recall\tB^3 F1\tB^3+ Precision\tB^3+ Recall\tB^3+ F1");
        out.append(System.getProperty("line.separator"));

        for (OutputSet sys : systems) {

            BCubedEvaluation b2eval = new BCubedEvaluation(sys, gold, focus.getMentionIds());

            final double kbp_micro_aver = b2eval.getAccuracy();

            final double b2_pre_avg = b2eval.getAveragePrecision();
            final double b2_rec_avg = b2eval.getAverageRecall();
//                final double b2_avg_f = b2eval.getMicroAverageF1Score();
            final double f_b2 = b2eval.getMicroAverageFScore(1);

            BCubedPlusEvaluation b3eval = new BCubedPlusEvaluation(sys, gold, focus.getMentionIds());

            final double b3_pre_avg = b3eval.getAveragePrecision();
            final double b3_rec_avg = b3eval.getAverageRecall();
//                final double b3_avg_f = b3eval.getMicroAverageF1Score();
            final double f_b3 = b3eval.getMicroAverageFScore(1);

            out.append(String.format(
                    "%s\t%.3f\t%.3f\t%.3f\t%.3f\t%.3f\t%.3f\t%.3f",
                    sys.getName(), kbp_micro_aver,
                    b2_pre_avg, b2_rec_avg, f_b2,
                    b3_pre_avg, b3_rec_avg, f_b3));
            out.append(System.getProperty("line.separator"));
        }
    }

}
