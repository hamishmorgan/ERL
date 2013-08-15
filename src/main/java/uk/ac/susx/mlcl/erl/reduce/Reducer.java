package uk.ac.susx.mlcl.erl.reduce;

import javax.annotation.Nullable;

/**
 * Interface for a function that can be applied to performing a "reduce" operation single item from a data sequence into
 * some accumulation.
 * <p/>
 * A simple example of a possible reducer is the summation operation, which can be applied repeatedly onto a list until
 * a single accumated value is derived.
 * <p/>
 * The {@link Reducers} class provides common reducer implementations and related utilites. The {@link Reductor} class
 * provides an adapter to iterable data types; where this interface defines a single operation the
 * <tt>Reductor</tt> applies that operation to a full sequence.
 *
 * @param <A> Accumulator type
 * @param <T> Item type
 * @author Hamish Morgan
 */
public interface Reducer<A, T> {
    /**
     * Returns the results of the accumulator with the next item folded in.
     *
     * @param accumulator value accumulated before reduction
     * @param next  item to fold in
     * @return accumulator value after reduction
     * @throws NullPointerException if any argument is null and this predicate does not accept null arguments
     */
    @Nullable
    A foldIn(@Nullable A accumulator, @Nullable T next) throws NullPointerException;

    /**
     * Indicates whether another object is equal to this reducer.
     * <p/>
     * <p>Most implementations will have no reason to override the behavior of {@link Object#equals}.
     * However, an implementation may also choose to return {@code true} whenever {@code object} is a
     * {@link Reducer} that it considers <i>interchangeable</i> with this one. "Interchangeable"
     * <i>typically</i> means that {@code Objects.equal(this.foldIn(a, t), that.foldIn(a,t))} is true for all
     * {@code a} of type {@code A} and {@code t} of type {@code T} . Note that a {@code false} result from this method
     * does not imply that the functions are known <i>not</i> to be interchangeable.
     */
    @Override
    boolean equals(@Nullable Object object);
}