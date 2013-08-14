package uk.ac.susx.mlcl.erl.tac.eval;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.collect.*;
import uk.ac.susx.mlcl.erl.lib.Comparators;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created with IntelliJ IDEA.
 * User: hamish
 * Date: 08/08/2013
 * Time: 16:08
 * To change this template use File | Settings | File Templates.
 */
public class ConfusionMatrixBuilder<T> {

    private final Map<T, Integer> labelIndexMap;
    private final AtomicInteger nextLabelIndex;
    private LabelFormat labelFormat;
    private Optional<Function<T, String>> customLabelFormatter;
    private LabelOrder labelOrder;
    private Optional<Comparator<T>> customLabelComparator;
    private Multiset<Result<T>> results;

    ConfusionMatrixBuilder() {
        labelIndexMap = Maps.newHashMap();
        nextLabelIndex = new AtomicInteger(0);
        labelFormat = LabelFormat.TO_STRING;
        customLabelFormatter = Optional.absent();
        labelOrder = LabelOrder.ADD_INDEX;
        customLabelComparator = Optional.absent();
        results = HashMultiset.create();
    }

    public ConfusionMatrixBuilder<T> addLabel(T label) {
        if (!labelIndexMap.containsKey(label))
            labelIndexMap.put(label, nextLabelIndex.getAndIncrement());
        return this;
    }

    public ConfusionMatrixBuilder<T> addLabel(T firstLabel, T... remainingLabels) {
        addLabel(firstLabel);
        for (T label : remainingLabels)
            addLabel(label);
        return this;
    }

    public ConfusionMatrixBuilder<T> addAllLabels(Iterable<? extends T> labels) {
        return addAllLabels(labels.iterator());
    }

    public ConfusionMatrixBuilder<T> addAllLabels(Iterator<? extends T> labels) {
        while (labels.hasNext())
            addLabel(labels.next());
        return this;
    }

    public ConfusionMatrixBuilder<T> setLabelFormat(LabelFormat format) {
        labelFormat = format;
        customLabelFormatter = Optional.absent();
        return this;
    }

    public ConfusionMatrixBuilder<T> setLabelFormat(Function<T, String> formatter) {
        labelFormat = LabelFormat.CUSTOM;
        customLabelFormatter = Optional.of(formatter);
        return this;
    }

    public ConfusionMatrixBuilder<T> setLabelOrder(LabelOrder order) {
        labelOrder = order;
        customLabelComparator = Optional.absent();
        return this;
    }

    public ConfusionMatrixBuilder<T> setLabelOrder(Comparator<T> comparator) {
        labelOrder = LabelOrder.CUSTOM;
        customLabelComparator = Optional.of(comparator);
        return this;
    }

    public ConfusionMatrixBuilder<T> addResult(T actual, T predicted) {
        return addResults(actual, predicted, 1);
    }

    public ConfusionMatrixBuilder<T> addResults(T actual, T predicted, int occurances) {
        addLabel(actual);
        addLabel(predicted);
        results.add(new Result<T>(actual, predicted), occurances);
        return this;
    }

    public ConfusionMatrix<T> build() {

        BiMap<T, Integer> labelIndex = ImmutableBiMap.copyOf(labelIndexMap);
        checkState(!labelIndex.isEmpty());

        final Function<T, String> labelFormatter;
        switch (labelFormat) {
            case CUSTOM:
                labelFormatter = customLabelFormatter.get();
                break;
            case IDENTITY:
                checkState(String.class.isAssignableFrom(labelIndex.keySet().iterator().next().getClass()),
                        "label class is not a string");
                labelFormatter = (Function) Functions.<String>identity();
                break;
            case TO_STRING:
                labelFormatter = (Function) Functions.<T>toStringFunction();
                break;
            default:
                throw new AssertionError("Unknown label format: " + labelFormat);
        }

        final Comparator<T> labelComparator;
        switch (labelOrder) {
            case ADD_INDEX:
                labelComparator = Comparators.mapped(labelIndex);
                break;
            case CUSTOM:
                labelComparator = customLabelComparator.get();
                break;
            case FORMATTED_LEXICOGRAPHIC:
                labelComparator = Comparators.mapped(labelFormatter);
                break;
            case NATURAL:
                checkState(Comparable.class.isAssignableFrom(labelIndex.keySet().iterator().next().getClass()),
                        "label class does not implement comparable");
                final Comparator temp = Comparators.natural();
                labelComparator = (Comparator<T>) temp;
                break;
            case TO_STRING_LEXICOGRAPHIC:
                labelComparator = Comparators.toStringLexicographic();
                break;
            default:
                throw new AssertionError("Unknown label order: " + labelOrder);
        }

//        labelIndex = ImmutableBiMap.copyOf(ImmutableSortedMap.copyOf(labelIndexMap, labelComparator));


        final int size = labelIndex.size();
        long[][] array = new long[size][size];
        for (Multiset.Entry<Result<T>> result : results.entrySet()) {
            final int row = labelIndex.get(result.getElement().actual);
            final int col = labelIndex.get(result.getElement().predicted);
            array[row][col] += result.getCount();
        }

        ConfusionMatrix<T> matrix = new ConcreteLongArrayConfusionMatrix<T>(labelIndex, array, labelComparator, labelFormatter);
        if (size == 2)
            matrix = new BinaryConfusionMatrix<T>(matrix);

        return matrix;
    }

    public enum LabelFormat {
        TO_STRING,
        IDENTITY,
        CUSTOM
    }


    public enum LabelOrder {
        FORMATTED_LEXICOGRAPHIC,
        TO_STRING_LEXICOGRAPHIC,
        NATURAL,
        ADD_INDEX,
        CUSTOM
    }

    static class Result<T> {
        final T actual;
        final T predicted;

        Result(T actual, T predicted) {
            this.actual = actual;
            this.predicted = predicted;
        }
    }


}
