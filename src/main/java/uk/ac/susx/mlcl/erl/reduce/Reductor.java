package uk.ac.susx.mlcl.erl.reduce;

import javax.annotation.Nonnull;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 05/08/2013
 * Time: 10:26
 * To change this template use File | Settings | File Templates.
 */
public class Reductor<A, T> {

    private final Reducer<A, T> worker;

    public Reductor(Reducer<A, T> worker) {
        this.worker = worker;
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

    public A fold(final A rval, @Nonnull final Iterator<T> itr) {
        A val = rval;
        while (itr.hasNext()) {
            val = worker.foldIn(val, itr.next());
        }
        return val;
    }

}