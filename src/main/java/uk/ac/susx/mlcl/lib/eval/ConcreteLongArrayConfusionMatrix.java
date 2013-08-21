package uk.ac.susx.mlcl.lib.eval;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Lists;
import uk.ac.susx.mlcl.lib.Comparators;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An implementation of {@link ConfusionMatrix} backing off to a EJML matrix.
 */
class ConcreteLongArrayConfusionMatrix<T> extends ConfusionMatrix<T> {

    private final BiMap<T, Integer> labelIndexMap;
    private final Comparator<T> labelOrder;
    private final Function<T, String> labelFormatter;
    private final long[][] mat;

    ConcreteLongArrayConfusionMatrix(final BiMap<T, Integer> labelIndexMap,
                                     final long[][] mat,
                                     final Comparator<T> labelOrder,
                                     final Function<T, String> labelFormatter) {
        this.labelIndexMap = checkNotNull(labelIndexMap, "labelIndexMap");
        this.mat = checkNotNull(mat, "mat");
        checkArgument(mat.length == 0 || mat[0].length == mat.length);
        this.labelOrder = checkNotNull(labelOrder, "labelOrder");
        this.labelFormatter = checkNotNull(labelFormatter, "labelFormatter");
    }

    public static ConfusionMatrix<String> newInstance(final long[][] array) {
        String[] labels = new String[array.length];
        for (int i = 0; i < array.length; i++)
            labels[i] = Long.toString(i);
        return newInstance(array, labels);
    }

    public static <T> ConfusionMatrix<T> newInstance(final long[][] array, final T[] labels) {
        checkNotNull(array, "array");
        checkNotNull(labels, "labels");
        checkArgument(array.length == labels.length, "#rows != #labels");
        checkArgument(array.length == 0 || array[0].length == labels.length, "#cols != #labels");

        final ImmutableBiMap.Builder<T, Integer> labelIndexBuilder = ImmutableBiMap.builder();
        for (int i = 0; i < labels.length; i++)
            labelIndexBuilder.put(labels[i], i);

        final BiMap<T,Integer> labelIndexMap = labelIndexBuilder.build();

        final Comparator<T> order = Comparators.mapped(labelIndexMap);

        return new ConcreteLongArrayConfusionMatrix<T>(
                labelIndexMap,
                array,
                order,
                (Function<T,String>)Functions.toStringFunction());
    }

    public List<T> getLabels() {
        final List<T> result = Lists.newArrayList(labelIndexMap.keySet());
        Collections.sort(result, labelOrder);
        return result;
    }

    public long getCount(T actual, T predicted) {
        final int y = labelIndexMap.get(actual);
        final int x = labelIndexMap.get(predicted);
        return (long) mat[y][x];
    }

    @Override
    protected String formatLabel(T label) {
        return labelFormatter.apply(label);
    }
}
