package uk.ac.susx.mlcl.erl.tac.eval;

import com.google.common.collect.ImmutableMap;
import uk.ac.susx.mlcl.erl.tac.queries.Output;
import uk.ac.susx.mlcl.erl.tac.queries.OutputSet;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;


public class BCubedEvaluation extends SimpleClusterEvaluation {

    public BCubedEvaluation(@Nonnull final OutputSet sys, @Nonnull final OutputSet gold,
                            @Nonnull final Collection<String> focus) {
        super(sys, gold, focus);
    }

    public BCubedEvaluation(@Nonnull final OutputSet systemOutput, @Nonnull final OutputSet goldStandard) {
        super(systemOutput, goldStandard);
    }

    /**
     * Calculate the f-beta measure from the given precision, recall, and beta arguments.
     * <p/>
     * The <tt>beta</tt> weights the relative importance of precision and recall to the measure. A <tt>beta</tt> of 1.0
     * indicates that precision and recall are weighted equally. Values of <tt>beta</tt> less that 1.0 given increasing
     * weight to precision, to the limit of <tt>beta = 0.0</tt> where the fscore is equal to precision. Conversly,
     * values
     * of <tt>beta</tt> greater than 1.0 given increasing weight recall, to the limit of <tt>beta = +Infinity</tt>
     * where
     * the fscore is equal to recall.
     *
     * @param precision the proportion of positive predictions which where correct
     * @param recall    the positive results which where correctly predicted
     * @param beta      the weighting of precision and recall in [0, +infity].
     * @return the f-beta measure
     * @throws IllegalArgumentException if precision or recall are outside the range [0,1], or if beta is outside the
     *                                  range [0, +infinity], or if any argument is NaN.
     */
    @Nonnegative
    private static double fscore(@Nonnegative final double precision, @Nonnegative final double recall,
                                 @Nonnegative final double beta) {
        checkArgument(precision >= 0 && precision <= 1.0,
                "Expected precision in range 0 to 1; but found " + precision);
        checkArgument(recall >= 0 && recall <= 1.0,
                "Expected recall in range 0 to 1; but found " + recall);
        checkArgument(beta >= 0 && beta <= Double.POSITIVE_INFINITY,
                "Expected beta in range 0 to +infinity; but found " + beta);
        if (beta == Double.POSITIVE_INFINITY)
            return recall;
        else if (beta <= 0)
            return precision;
        else if (precision <= 0 && recall <= 0)
            return 0.0;
        else
            return (1.0 + beta * beta) * precision * recall / (beta * beta * precision + recall);
    }

    @Nonnull
    private final Map<String, Double> buildPrecisionMap(
            final @Nonnull OutputSet predicted, final @Nonnull OutputSet groundTruth) {
        final ImmutableMap.Builder<String, Double> precisionMapBuilder = ImmutableMap.builder();
        for (String kbId : predicted.getKbIds()) {
            final Set<Output> cluster = predicted.getKbIdClusters().get(kbId);
            for (Output a : cluster) {
                int correctCount = 0;
                for (Output b : cluster)
                    if (isCorrect(a.getMentionId(), b.getMentionId(), predicted, groundTruth))
                        ++correctCount;
                precisionMapBuilder.put(a.getMentionId(), correctCount / (double) cluster.size());
            }
        }
        return precisionMapBuilder.build();
    }

    @Nonnull
    protected boolean isCorrect(String el_a, String el_b, OutputSet sys, OutputSet gold) {
        return sys.inSameCluster(el_a, el_b) && gold.inSameCluster(el_a, el_b);
    }

    @Nonnull
    protected final Map<String, Double> precisionMap() {
        return buildPrecisionMap(getSystemOutput(), getGoldStandard());
    }

    @Nonnull
    protected final Map<String, Double> recallMap() {
        return buildPrecisionMap(getGoldStandard(), getSystemOutput());
    }

    @Nonnegative
    public final double getAveragePrecision() {
        final Map<String, Double> b2_pre = precisionMap();
        double el_pre_sums = 0.0;
        for (String el_a : getFocusMentions())
            el_pre_sums += b2_pre.get(el_a);
        return el_pre_sums / (double) getFocusMentions().size();
    }

    @Nonnegative
    public final double getAverageRecall() {
        final Map<String, Double> b2_rec = recallMap();
        double el_rec_sums = 0.0;
        for (String el_a : getFocusMentions())
            el_rec_sums += b2_rec.get(el_a);
        return el_rec_sums / (double) getFocusMentions().size();
    }

    @Nonnegative
    public final double getMacroAverageFScore(@Nonnegative final double beta) {
        final Map<String, Double> b2_pre = precisionMap();
        final Map<String, Double> b2_rec = recallMap();
        double el_f_sums = 0.0;
        for (String el_a : getFocusMentions())
            el_f_sums += fscore(b2_pre.get(el_a), b2_rec.get(el_a), beta);
        return el_f_sums / (double) getFocusMentions().size();
    }

    @Nonnegative
    public final double getMicroAverageFScore(@Nonnegative final double beta) {
        return fscore(getAveragePrecision(), getAverageRecall(), beta);
    }

}
