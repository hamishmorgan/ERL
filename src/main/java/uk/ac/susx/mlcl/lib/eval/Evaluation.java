package uk.ac.susx.mlcl.lib.eval;

import com.google.common.base.Function;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.*;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 02/08/2013
 * Time: 12:49
 * To change this template use File | Settings | File Templates.
 */
public class Evaluation<T> {

    @Nonnull
    private final List<T> actual;
    @Nonnull
    private final List<T> predicted;
    @Nonnull
    private final Comparator<T> comparator;
    @Nonnull
    private final Function<T, String> labelFormatter;

    public Evaluation(final @Nonnull List<T> actual,
                      final @Nonnull List<T> predicted,
                      final @Nonnull Comparator<T> elementComparator,
                      final @Nonnull Function<T, String> elementFormatter) {
        checkArgument(actual.size() == predicted.size());
        this.actual = checkNotNull(actual, "actual");
        this.predicted = checkNotNull(predicted, "predicted");
        this.comparator = checkNotNull(elementComparator, "comparator");
        this.labelFormatter = checkNotNull(elementFormatter, "labelFormatter");
    }

    public ConfusionMatrix<T> getConfusionMatrix() {

        ConfusionMatrixBuilder<T> builder = ConfusionMatrix.builder();

        for (final Iterator<T> ait = actual.iterator(), pit = predicted.iterator(); ait.hasNext() || pit.hasNext(); )
            builder.addResult(ait.next(), pit.next());
        builder.setLabelOrder(comparator);
        builder.setLabelFormat(labelFormatter);

        return builder.build();
//
//
//        final List<T> labels = ImmutableList.copyOf(ImmutableSortedSet.orderedBy(comparator).addAll(actual).addAll(predicted).build());
//        final BiMap<T, Integer> labelIndex = ImmutableBiMap.copyOf(EvalUtil.indexMap(labels, comparator));
//
//
//        final Map<T, Integer> index = EvalUtil.indexMap(labels, comparator);
//        final SimpleMatrix mat = new SimpleMatrix(labels.size(), labels.size());
//
//        for (final Iterator<T> ait = actual.iterator(), pit = predicted.iterator(); ait.hasNext() || pit.hasNext(); ) {
//            final int i = mat.getIndex(index.get(ait.next()), index.get(pit.next()));
//            mat.set(i, mat.get(i) + 1.0);
//        }
//
//        ConfusionMatrix<T> cmat = new ConcreteEJMLConfusionMatrix<T>(labelIndex, mat, comparator, labelFormatter);
//        if (labels.size() == 2)
//            cmat = new BinaryConfusionMatrix<T>(cmat);
//        return cmat;
    }

    public String getResultsTable() {
        StringBuilder builder = new StringBuilder();
        builder.append("Actual");
        builder.append('\t');
        builder.append("Predicted");
        builder.append('\t');
        builder.append("Correct");
        builder.append(System.getProperty("line.separator"));
        for (final Iterator<T> ait = actual.iterator(), pit = predicted.iterator(); ait.hasNext() || pit.hasNext(); ) {
            final T a = ait.next(), p = pit.next();
            builder.append(labelFormatter.apply(a));
            builder.append('\t');
            builder.append(labelFormatter.apply(p));
            builder.append('\t');
            builder.append(comparator.compare(a, p) == 0);
            builder.append(System.getProperty("line.separator"));
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return getResultsTable();
    }

}
