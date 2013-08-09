package uk.ac.susx.mlcl.erl.tac.eval;

import java.util.Locale;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <code>BinaryConfusionMatrix</code> is an adaptor for confusion matrices, adding additional useful methods and
 * functionality that apply to 2x2 matrices only.
 */
public class BinaryConfusionMatrix<T> extends ForwardingConfusionMatrix<T> {

    protected static final int POSITIVE_INDEX = 0;
    protected static final int NEGATIVE_INDEX = 1;

    private final T positiveLabel;
    private final T negativeLabel;

    BinaryConfusionMatrix(final ConfusionMatrix<T> delegate) {
        super(delegate);
        positiveLabel = checkNotNull(delegate.getLabels().get(POSITIVE_INDEX));
        negativeLabel = checkNotNull(delegate.getLabels().get(NEGATIVE_INDEX));
    }

    public final T getPositiveLabel() {
        return positiveLabel;
    }

    public final T getNegativeLabel() {
        return negativeLabel;
    }

    public long getTruePositiveCount() {
        return getTrueCountFor(getPositiveLabel());
    }

    /**
     * aka: Type I error
     *
     * @return
     */
    public long getFalsePositiveCount() {
        return getFalseCountFor(getPositiveLabel());
    }

    public long getTrueNegativeCount() {
        return getTrueCountFor(getNegativeLabel());
    }

    /**
     * aka: Type II error
     *
     * @return
     */
    public long getFalseNegativeCount() {
        return getFalseCountFor(getNegativeLabel());
    }

    public long getPredictedPositiveCount() {
        return getPredictedCountFor(getPositiveLabel());
    }

    public long getPredictedNegativeCount() {
        return getPredictedCountFor(getNegativeLabel());
    }

    public long getActualPositiveCount() {
        return getActualCountFor(getPositiveLabel());
    }

    public long getActualNegativeCount() {
        return getActualCountFor(getNegativeLabel());
    }

    /**
     * aka sensitivity, recall
     *
     * @return
     */
    public double getTruePositiveRate() {
        return getTrueRateFor(getPositiveLabel());
    }

    /**
     * aka: false alarm rate, fall-out
     *
     * @return
     */
    public double getFalsePositiveRate() {
        return getFalseRateFor(getPositiveLabel());
    }

    /**
     * aka negative recall, specificity
     *
     * @return
     */
    public double getTrueNegativeRate() {
        return getTrueRateFor(getNegativeLabel());
    }

    public double getFalseNegativeRate() {
        return getFalseRateFor(getNegativeLabel());
    }

    public double getNegativePredictiveValue() {
        return getPredictiveValueFor(getNegativeLabel());
    }

    /**
     * aka precision
     *
     * @return
     */
    public double getPositivePredictiveValue() {
        return getPredictiveValueFor(getPositiveLabel());
    }

    public double getPositiveFScore(double beta) {
        return getFScoreFor(getPositiveLabel(), beta);
    }

    public double getNegativeFScore(double beta) {
        return getFScoreFor(getNegativeLabel(), beta);
    }


    @Override
    public void appendStats(Appendable dst, Locale locale) {
        super.appendStats(dst, locale);
        appendStatsFor(getPositiveLabel(), dst, locale);
        appendStatsFor(getNegativeLabel(), dst, locale);
    }

}
