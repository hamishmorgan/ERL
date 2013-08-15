package uk.ac.susx.mlcl.erl.reduce;

import javax.annotation.Nonnull;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The <tt>Reductor</tt> class provides an adapter of {@link Reducer} operations to applied {@link Iterable} data types. Where
 * the {@link Reducer} interface defines a single operation the <tt>Reductor</tt> applies that operation to a full
 * sequence.
 */
public class Reductor<A, T> {

    private final Reducer<A, T> worker;

    /**
     * @param worker
     */
    public Reductor(@Nonnull final Reducer<A, T> worker) {
        this.worker = checkNotNull(worker, "worker");
    }

    /**
     * Convenience static method with applied the given reducer to each element of the iterator, starting with the
     * given initial value.
     *
     * @param reducer
     * @param itr
     * @param initialValue
     * @param <A>
     * @param <T>
     * @return
     */
    public static <A, T> A reduce(Reducer<A, T> reducer, Iterator<T> itr, A initialValue) {
        return new Reductor<A, T>(reducer).fold(initialValue, itr);
    }

    /**
     * For reducers where both the accumulation and element type are the same (e.g common numeric operations such as
     * summing the initial value is often the first element in the iterator.
     *
     * @param reducer
     * @param itr
     * @param <T>
     * @return
     */
    public static <T> T reduce(Reducer<T, T> reducer, Iterator<T> itr) {
        return new Reductor<T, T>(reducer).fold(itr.next(), itr);
    }

    /**
     * @param rval
     * @param itr
     * @return
     */
    public A fold(final A rval, @Nonnull final Iterator<T> itr) {
        A val = rval;
        while (itr.hasNext())
            val = worker.foldIn(val, itr.next());
        return val;
    }


}