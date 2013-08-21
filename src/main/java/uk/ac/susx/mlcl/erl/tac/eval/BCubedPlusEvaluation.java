package uk.ac.susx.mlcl.erl.tac.eval;

import uk.ac.susx.mlcl.erl.tac.queries.OutputSet;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 20/08/2013
 * Time: 16:37
 * To change this template use File | Settings | File Templates.
 */
public class BCubedPlusEvaluation extends BCubedEvaluation {


    public BCubedPlusEvaluation(@Nonnull final OutputSet sys, @Nonnull final OutputSet gold,
                                @Nonnull final Collection<String> focus) {
        super(sys, gold, focus);
    }

    public BCubedPlusEvaluation(@Nonnull final OutputSet systemOutput, @Nonnull final OutputSet goldStandard) {
        super(systemOutput, goldStandard);
    }

    private static final boolean sameLinking(String el_a, String el_b, OutputSet sys, OutputSet gold) {
        final String sys_el_a_id = normaliseNil(sys.getKbIdForMention(el_a));
        final String sys_el_b_id = normaliseNil(sys.getKbIdForMention(el_b));
        final String gol_el_a_id = normaliseNil(gold.getKbIdForMention(el_a));
        final String gol_el_b_id = normaliseNil(gold.getKbIdForMention(el_b));

        return sys_el_a_id.equals(sys_el_b_id)
                && sys_el_b_id.equals(gol_el_a_id)
                && gol_el_a_id.equals(gol_el_b_id);
    }

    @Override
    protected boolean isCorrect(String el_a, String el_b, OutputSet sys, OutputSet gold) {
        return sys.inSameCluster(el_a, el_b) && gold.inSameCluster(el_a, el_b)
                && sameLinking(el_a, el_b, sys, gold);
    }
}
