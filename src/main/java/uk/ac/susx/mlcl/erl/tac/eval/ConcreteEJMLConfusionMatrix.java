package uk.ac.susx.mlcl.erl.tac.eval;

import com.google.common.base.Function;
import com.google.common.collect.BiMap;
import com.google.common.collect.Lists;
import org.ejml.simple.SimpleMatrix;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An implementation of {@link ConfusionMatrix} backing off to a EJML matrix.
 */
class ConcreteEJMLConfusionMatrix<T> extends ConfusionMatrix<T> {

    private final BiMap<T, Integer> labelIndexMap;
    private final Comparator<T> labelOrder;
    private final Function<T, String> labelFormatter;
    private final SimpleMatrix mat;

    ConcreteEJMLConfusionMatrix(final BiMap<T, Integer> labelIndexMap,
                                final SimpleMatrix mat,
                                final Comparator<T> labelOrder,
                                final Function<T, String> labelFormatter) {
        this.labelIndexMap = checkNotNull(labelIndexMap, "labelIndexMap");
        this.mat = checkNotNull(mat, "mat");
        checkArgument(mat.numRows() == mat.numCols());
        this.labelOrder = checkNotNull(labelOrder, "labelOrder");
        this.labelFormatter = checkNotNull(labelFormatter, "labelFormatter");
    }

    public List<T> getLabels() {
        final List<T> result = Lists.newArrayList(labelIndexMap.keySet());
        Collections.sort(result, labelOrder);
        return result;
    }


    public long getCount(T actual, T predicted) {
        final int y = labelIndexMap.get(actual);
        final int x = labelIndexMap.get(predicted);
        return (long)mat.get(y, x);
    }

    @Override
    protected String formatLabel(T label) {
        return labelFormatter.apply(label);
    }
}
