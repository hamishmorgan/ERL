package uk.ac.susx.mlcl.erl.tac.eval;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.ejml.simple.SimpleMatrix;
import uk.ac.susx.mlcl.erl.reduce.Reducer;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 05/08/2013
 * Time: 14:39
 * To change this template use File | Settings | File Templates.
 */
public class ConfusionMatrix<T> {

    protected final BiMap<T, Integer> labelIndexMap;
    protected final Comparator<T> labelOrder;
    protected final Function<T, String> labelFormatter;
    protected final SimpleMatrix mat;

    ConfusionMatrix(BiMap<T, Integer> labelIndexMap, SimpleMatrix mat, Comparator<T> labelOrder, Function<T, String> labelFormatter) {
        this.labelIndexMap = checkNotNull(labelIndexMap, "labelIndexMap");
        this.mat = checkNotNull(mat, "mat");
        checkArgument(mat.numRows() == mat.numCols());
        this.labelOrder = checkNotNull(labelOrder, "labelOrder");
        this.labelFormatter = checkNotNull(labelFormatter, "labelFormatter");
    }

    public Collection<T> getLabels() {
        return labelIndexMap.keySet();
    }

    public int size() {
        return labelIndexMap.size();
    }

    public double getPredictedCountFor(T label) {
        final int x = labelIndexMap.get(label);
        double sum = 0;
        for (int y = 0; y < mat.numRows(); y++)
            sum += mat.get(y, x);
        return sum;
    }

    public double getActualCountFor(T label) {
        final int y = labelIndexMap.get(label);
        double sum = 0;
        for (int x = 0; x < mat.numRows(); x++)
            sum += mat.get(y, x);
        return sum;
    }

    public double getTrueCount() {
        return mat.extractDiag().elementSum();
    }

    public double getFalseCount() {
        return getGrandTotal() - getTrueCount();
    }

    public double getTrueCountFor(T label) {
        final int i = labelIndexMap.get(label);
        return mat.get(i, i);
    }

    public double getFalseCountFor(T label) {
        return getPredictedCountFor(label) - getTrueCountFor(label);
    }

    public double getGrandTotal() {
        return mat.elementSum();
    }

    public double getAccuracy() {
        return getTrueCount() / getGrandTotal();
    }

    /**
     * Get the proportion of instances with the given label that where correctly identified.
     * <p/>
     * In the case of the positive label in a binary confusion matrix, this statistic is often known as "recall". For
     * the negative label, this statistic is sometimes called "specificity".
     *
     * @param label
     * @return
     */
    public double getTrueRateFor(T label) {
        return getTrueCountFor(label) / getActualCountFor(label);
    }

    /**
     * Get the proportion of instances with the given label that where incorrectly identified.
     *
     * @param label
     * @return
     */
    public double getFalseRateFor(T label) {
        return getFalseCountFor(label) / getActualCountFor(label);
    }

    /**
     * Get the proportion of instances, predicted as having the label, which where correct.
     * <p/>
     * In the case of the positive label in a binary confusion matrix, this statistic is commonly known as "precision".
     *
     * @param label
     * @return
     */
    public double getPredictiveValueFor(T label) {
        return getTrueCountFor(label) / getPredictedCountFor(label);
    }

    /**
     * Get the proportion of instances, predicted as having the label, which where incorrect.
     *
     * @param label
     * @return
     */
    public double getFalseDiscoveryRate(T label) {
        return getFalseCountFor(label) / getPredictedCountFor(label);
    }

    /**
     * Calculate the F-Beta score, measuring the precision and recall of the given label vs all other labels.
     *
     * @param label
     * @param beta
     * @return
     */
    public double getFScoreFor(T label, double beta) {
        final double precision = getPredictiveValueFor(label);
        final double recall = getTrueRateFor(label);
        return (1.0 + beta * beta) * precision * recall / (beta * beta * precision + recall);
    }

    public <D> BinaryConfusionMatrix<D> mapAllVersus(final Predicate<T> positive,
                                                     final D positiveLabel,
                                                     final D negativeLabel,
                                                     final Function<D, String> labelFormatter,
                                                     final Reducer<Double, Double> reducer) {
        final Comparator<D> labelOrder = new Comparator<D>() {
            @Override
            public int compare(final D o1, final D o2) {
                return Integer.compare(
                        o1.equals(positiveLabel) ? 0 : 1,
                        o2.equals(positiveLabel) ? 0 : 1);
            }
        };

        final ConfusionMatrix<D> dstMatrix = mapLabels(new Function<T, D>() {
            @Nullable
            @Override
            public D apply(final @Nullable T input) {
                return positive.apply(input) ? positiveLabel : negativeLabel;
            }
        }, reducer, labelOrder, labelFormatter);

        final BiMap<D, Integer> dstLabelIndexMap = ImmutableBiMap.copyOf(
                EvalUtil.indexMap(ImmutableList.of(positiveLabel, negativeLabel), labelOrder));

        return new BinaryConfusionMatrix<D>(dstLabelIndexMap, dstMatrix.mat, labelOrder, labelFormatter);
    }

    public BinaryConfusionMatrix<String> mapAllVersus(final Predicate<T> positive,
                                                      final String positiveLabel,
                                                      final String negativeLabel,
                                                      final Reducer<Double, Double> reducer) {
        return mapAllVersus(positive, positiveLabel, negativeLabel, new

                Function<String, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable String input) {
                        return input;
                    }
                }, reducer);
    }

    /**
     * Remap the matrix labels to produce a new confusion matrix. If source labels are merged (i.e two or more
     * source labels map to one destination label) the counts are summed.
     *
     * @param mapping
     * @return
     */
    public <D> ConfusionMatrix<D> mapLabels(
            Function<T, D> mapping,
            Reducer<Double, Double> reducer,
            Comparator<D> dstLabelOrder,
            Function<D, String> targetLabelFormatter) {

        List<D> dstLabels = Lists.newArrayList(Sets.newHashSet(Lists.transform(Lists.newArrayList(getLabels()), mapping)));
        final BiMap<D, Integer> dstLabelIndexMap = ImmutableBiMap.copyOf(EvalUtil.indexMap(dstLabels, dstLabelOrder));
        final SimpleMatrix targetMat = new SimpleMatrix(dstLabelIndexMap.size(), dstLabelIndexMap.size());

        for (int srcY = 0; srcY < mat.numRows(); srcY++) {
            final int dstY = dstLabelIndexMap.get(mapping.apply(labelIndexMap.inverse().get(srcY)));
            for (int srcX = 0; srcX < mat.numCols(); srcX++) {
                final int dstX = dstLabelIndexMap.get(mapping.apply(labelIndexMap.inverse().get(srcX)));
                targetMat.set(dstY, dstX, reducer.foldIn(targetMat.get(dstY, dstX), mat.get(srcY, srcX)));
            }
        }
        return new ConfusionMatrix<D>(dstLabelIndexMap, targetMat, dstLabelOrder, targetLabelFormatter);
    }

    @Override
    public String toString() {
        return getTableString() + "\n" + getStatsString();
    }

    public String getStatsString() {
        final StringBuilder statsBuilder = new StringBuilder();
        statsBuilder.append(String.format("Accuracy: %.0f/%.0f = %.2f%% correct",
                getTrueCount(), getGrandTotal(), 100.0 * getAccuracy()));
        return statsBuilder.toString();
    }

    public String getTableString() {
        final String elementFormat = "%.0f ";

        // Build a 2d array of all the formatted values and labels
        final int[] idx = indexOrder();
        final String[][] cells = new String[mat.numRows() + 1][mat.numCols() + 1];
        cells[0][0] = "";
        for (int x = 0; x < mat.numCols(); x++)
            cells[0][x + 1] = labelFormatter.apply(labelIndexMap.inverse().get(idx[x]));
        for (int y = 0; y < mat.numCols(); y++)
            cells[y + 1][0] = labelFormatter.apply(labelIndexMap.inverse().get(idx[y]));
        for (int y = 0; y < mat.numRows(); y++)
            for (int x = 0; x < mat.numCols(); x++)
                cells[y + 1][x + 1] = String.format(elementFormat, mat.get(idx[y], idx[x]));

        // Calculate the max widths for each column
        final int[] widths = new int[cells[0].length];
        for (int y = 0; y < cells.length; y++)
            for (int x = 0; x < cells[0].length; x++)
                widths[x] = Math.max(widths[x], cells[y][x].length());

        // Build a formatter for the whole row
        final StringBuilder rowFormatBuilder = new StringBuilder();
        for (int x = 0; x < cells[0].length; x++)
            rowFormatBuilder.append(String.format("%%%ds", widths[x] + 1));
        rowFormatBuilder.append(String.format("%n"));
        final String rowFormat = rowFormatBuilder.toString();

        // Write each row
        final StringBuilder tableBuilder = new StringBuilder();
        for (int y = 0; y < cells.length; y++)
            tableBuilder.append(String.format(rowFormat, cells[y]));

        return tableBuilder.toString();
    }

    protected int[] indexOrder() {
        // Build a label order mapping so we print everything in the correct order

        List<Map.Entry<T, Integer>> labelEntries = Lists.newArrayList(labelIndexMap.entrySet());

        Collections.sort(labelEntries, new Comparator<Map.Entry<T, Integer>>() {
            @Override
            public int compare(Map.Entry<T, Integer> o1, Map.Entry<T, Integer> o2) {
                return labelOrder.compare(o1.getKey(), o2.getKey());
            }
        });
        List<Map.Entry<Integer, Integer>> indexEntries = Lists.transform(labelEntries, new Function<Map.Entry<T, Integer>, Map.Entry<Integer, Integer>>() {
            final AtomicInteger sortedIndex = new AtomicInteger(0);

            @Nullable
            @Override
            public Map.Entry<Integer, Integer> apply(@Nullable Map.Entry<T, Integer> input) {
                return new AbstractMap.SimpleEntry<Integer, Integer>(sortedIndex.getAndIncrement(), input.getValue());
            }
        });
        final int[] idx = new int[indexEntries.size()];
        for (Map.Entry<Integer, Integer> entry : indexEntries)
            idx[entry.getKey()] = entry.getValue();
        return idx;
    }


}
