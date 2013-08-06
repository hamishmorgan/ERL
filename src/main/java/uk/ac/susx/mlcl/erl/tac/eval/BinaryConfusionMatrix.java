package uk.ac.susx.mlcl.erl.tac.eval;

import com.google.common.base.Function;
import com.google.common.collect.BiMap;
import org.ejml.simple.SimpleMatrix;

import java.util.Comparator;

import static com.google.common.base.Preconditions.checkArgument;

/**
* Created with IntelliJ IDEA.
* User: hiam20
* Date: 05/08/2013
* Time: 14:39
* To change this template use File | Settings | File Templates.
*/
public class BinaryConfusionMatrix<T> extends ConfusionMatrix<T> {

    protected static final int POSITIVE_INDEX = 0;
    protected static final int NEGATIVE_INDEX = 1;

    BinaryConfusionMatrix(BiMap<T, Integer> labelIndexMap, SimpleMatrix mat, Comparator<T> labelOrder, Function<T, String> labelFormatter) {
        super(labelIndexMap, mat, labelOrder, labelFormatter);
        checkArgument(labelIndexMap.size() == 2);
    }

    public T getPositiveLabel() {
        final int[] idx = indexOrder();
        return labelIndexMap.inverse().get(idx[POSITIVE_INDEX]);
    }

    public T getNegativeLabel() {
        final int[] idx = indexOrder();
        return labelIndexMap.inverse().get(idx[NEGATIVE_INDEX]);
    }



    public double truePositives() {
        return getTrueCountFor(getPositiveLabel());
    }

    /**
     * aka: Type I error
     *
     * @return
     */
    public double falsePositives() {
        return getFalseCountFor(getPositiveLabel());
    }

    public double trueNegatives() {
        return getTrueCountFor(getNegativeLabel());
    }

    /**
     * aka: Type II error
     *
     * @return
     */
    public double falseNegatives() {
        return getFalseCountFor(getNegativeLabel());
    }

    public double predictedPositives() {
        return getPredictedCountFor(getPositiveLabel());
    }

    public double predictedNegatives() {
        return getPredictedCountFor(getNegativeLabel());
    }

    public double actualPositives() {
        return getActualCountFor(getPositiveLabel());
    }

    public double actualNegatives() {
        return getActualCountFor(getNegativeLabel());
    }

    /**
     * aka sensitivity, recall
     *
     * @return
     */
    public double truePositiveRate() {
        return getTrueRateFor(getPositiveLabel());
    }

    /**
     * aka: false alarm rate, fall-out
     *
     * @return
     */
    public double falsePositiveRate() {
        return getFalseRateFor(getPositiveLabel());
    }

    /**
     * aka negative recall, specificity
     *
     * @return
     */
    public double trueNegativeRate() {
        return getTrueRateFor(getNegativeLabel());
    }

    public double falseNegativeRate() {
        return getFalseRateFor(getNegativeLabel());
    }

    public double negativePredictiveValue() {
        return getPredictiveValueFor(getNegativeLabel());
    }

    /**
     * aka precision
     *
     * @return
     */
    public double positivePredictiveValue() {
        return getPredictiveValueFor(getPositiveLabel());
    }

    public double positiveFScore(double beta) {
        return getFScoreFor(getPositiveLabel(), beta);
    }

    public double negativeFScore(double beta) {
        return getFScoreFor(getNegativeLabel(), beta);
    }

    public String getStatsString() {
        final StringBuilder statsBuilder = new StringBuilder();
        statsBuilder.append(String.format("Accuracy: %.0f/%.0f = %.2f%% correct%n",
                getTrueCount(), getGrandTotal(), 100.0 * getAccuracy()));

        statsBuilder.append(String.format("%s Precision: %.0f/%.0f = %.2f%%%n",
                labelFormatter.apply(getPositiveLabel()), truePositives(), predictedPositives(), 100.0 * positivePredictiveValue()));
        statsBuilder.append(String.format("%s Recall: %.0f/%.0f = %.2f%%%n",
                labelFormatter.apply(getPositiveLabel()), truePositives(), actualPositives(), 100.0 * truePositiveRate()));
        statsBuilder.append(String.format("%s F1-Score: %.2f%%%n",
                labelFormatter.apply(getPositiveLabel()), 100.0 * positiveFScore(1.0)));

        statsBuilder.append(String.format("%s Precision: %.0f/%.0f = %.2f%%%n",
                labelFormatter.apply(getNegativeLabel()), trueNegatives(), predictedNegatives(), 100.0 * negativePredictiveValue()));
        statsBuilder.append(String.format("%s Recall: %.0f/%.0f = %.2f%%%n",
                labelFormatter.apply(getNegativeLabel()), trueNegatives(), actualNegatives(), 100.0 * trueNegativeRate()));
        statsBuilder.append(String.format("%s F1-Score: %.2f%%%n",
                labelFormatter.apply(getNegativeLabel()), 100.0 * negativeFScore(1.0)));

        return statsBuilder.toString();
    }

}
