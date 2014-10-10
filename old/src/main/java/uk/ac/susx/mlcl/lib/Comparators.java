package uk.ac.susx.mlcl.lib;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.io.Serializable;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Static utility methods pertaining to {@code Comparator} instances.
 * <p/>
 * All methods return serializable comparators as long as they're given serializable parameters.
 *
 * @author Hamish Morgan
 */
public class Comparators {

    // Static utility class should not be instantiated
    private Comparators() {
    }

    /**
     * Return a comparator which orders argument lexicographically by the result of their toString method.
     *
     * @param <T>
     * @return
     */
    @Nonnull
    public static <T> Comparator<T> toStringLexicographic() {
        return mapped((Function<T, String>) Functions.toStringFunction());
    }

    /**
     * Return a comparator which uses the natural ordering of it's arguments to compare them. Note that the
     * argument must, therefore, implement {@code Comparable} or terrible runtime exceptions will occur.
     *
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> Comparator<T> natural() {
        return (Comparator<T>) NaturalComparator.INSTANCE;
    }

    /**
     * Return a {@code Comparator} which always returns 0; i.e it evaluates it's argument as being equal.
     *
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Comparator<T> alwaysEqual() {
        return (Comparator<T>) ConstantComparator.EQUAL;
    }

    /**
     * Return a {@code Comparator} which always returns -1; i.e it evaluates the first argument as being less than
     * the second argument.
     *
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Comparator<T> alwaysLess() {
        return (Comparator<T>) ConstantComparator.LESS;
    }

    /**
     * Return a {@code Comparator} which always returns +1; i.e it evaluates the first argument as being greater than
     * the second argument.
     *
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Comparator<T> alwaysGreater() {
        return (Comparator<T>) ConstantComparator.GREATER;
    }

    /**
     * Return a {@code Comparator} which compares arguments based on the natural order of associated value
     * in the given {@code Map}.
     *
     * @param index
     * @param <T>
     * @param <C>
     * @return
     */
    @Nonnull
    public static <T, C extends Comparable<C>> Comparator<T> mapped(@Nonnull final Map<T, C> index) {
        return mapped(Functions.forMap(index), Comparators.<C>natural());
    }

    /**
     * Return a {@code Comparator} which compares arguments based on the natural order of associated value
     * returned by the the given {@code mapping} function.
     *
     * @param mapping
     * @param <T>
     * @param <C>
     * @return
     */
    @Nonnull
    public static <T, C extends Comparable<C>> Comparator<T> mapped(@Nonnull final Function<T, C> mapping) {
        return new MappedComparator<T, C>(mapping, Comparators.<C>natural());
    }

    /**
     * Return a {@code Comparator} which first maps the arguments using mapping, and then compares these values
     * using the given comparator.
     *
     * @param mapping
     * @param comparator
     * @param <T>
     * @param <C>
     * @return
     */
    @Nonnull
    public static <T, C> Comparator<T> mapped(@Nonnull final Function<T, C> mapping,
                                              @Nonnull final Comparator<C> comparator) {
        return new MappedComparator<T, C>(mapping, comparator);
    }

    /**
     * Return a comparator which produces the opposite result of the given delegate comparator.
     *
     * @param delegate
     * @param <T>
     * @return
     */
    @Nonnull
    public static <T> Comparator<T> reverse(@Nonnull Comparator<T> delegate) {
        return new ReversedComparator<T>(delegate);
    }

    /**
     * Return a {@code Comparator} which produces comparison entirely at random.
     *
     * @param lessThanProbability    probability of the comparator returning -1
     * @param greaterThanProbability probability of the comparator returning +1
     * @param random
     * @param <T>
     * @return
     */
    @Nonnull
    public static <T> Comparator<T> random(final double lessThanProbability,
                                           final double greaterThanProbability,
                                           @Nonnull final Random random) {
        return new RandomComparator<T>(random, lessThanProbability, greaterThanProbability);
    }

    /**
     * Chain the given comparators such that the result of child1 is return if it is non-zero, otherwise the result
     * of child2 is returned.
     *
     * @param child1
     * @param child2
     * @param <T>
     * @return
     */
    @Nonnull
    public static <T> Comparator<T> chain(@Nonnull final Comparator<T> child1, @Nonnull final Comparator<T> child2) {
        return new ChainedComparator<T>(ImmutableList.of(child1, child2));
    }

    /**
     * Chain the given comparators such the result returned is that of the first comparator returning a non-zero value.
     *
     * @param childComparators
     * @param <T>
     * @return
     */
    @Nonnull
    public static <T> Comparator<T> chain(@Nonnull final List<Comparator<T>> childComparators) {
        return new ChainedComparator<T>(childComparators);
    }

    /**
     * Chain the given comparators such the result returned is that of the first comparator returning a non-zero value.
     *
     * @param childComparators
     * @param <T>
     * @return
     */
    @Nonnull
    public static <T> Comparator<T> chain(@Nonnull Comparator<T>... childComparators) {
        return chain(ImmutableList.copyOf(childComparators));
    }

    private enum ConstantComparator implements Comparator<Object> {

        LESS(-1),
        EQUAL(0),
        GREATER(1);
        private final int constant;

        ConstantComparator(final int constant) {
            this.constant = constant;
        }

        @Override
        public int compare(@Nullable final Object o1, @Nullable final Object o2) {
            return constant;
        }

        @Override
        public String toString() {
            return "always_" + name();
        }
    }


    // Enum singleton pattern
    private enum NaturalComparator implements Comparator<Comparable<Object>> {

        INSTANCE;

        @Override
        public int compare(@Nonnull final Comparable<Object> o1, @Nonnull final Comparable<Object> o2) {
            return o1.compareTo(o2);
        }

        @Override
        public String toString() {
            return "natural";
        }
    }

    private static final class ReversedComparator<T> extends ForwardingComparator<T> implements Serializable {
        private static final long serialVersionUID = 0;

        ReversedComparator(@Nonnull final Comparator<T> delegate) {
            super(delegate);
        }

        @Override
        public int compare(T o1, T o2) {
            return -super.compare(o1, o2);
        }

        @Override
        public String toString() {
            return "reversed(" + getDelegate() + ')';
        }
    }

    private static abstract class ForwardingComparator<T> implements Comparator<T> {

        @Nonnull
        private final Comparator<T> delegate;

        protected ForwardingComparator(@Nonnull final Comparator<T> delegate) {
            this.delegate = checkNotNull(delegate, "delegate");
        }

        @Nonnull
        protected Comparator<T> getDelegate() {
            return delegate;
        }

        @Override
        public int compare(T o1, T o2) {
            return delegate.compare(o1, o2);
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final ForwardingComparator that = (ForwardingComparator) o;
            return delegate.equals(that.delegate);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }
    }

    private static final class MappedComparator<T, C>
            implements Comparator<T>, Serializable {

        private static final long serialVersionUID = 0;
        @Nonnull
        private final Function<T, C> mapping;
        @Nonnull
        private final Comparator<C> delegate;

        MappedComparator(@Nonnull final Function<T, C> mapping, @Nonnull final Comparator<C> delegate) {
            this.mapping = checkNotNull(mapping, "mapping");
            this.delegate = checkNotNull(delegate, "delegate");
        }

        @Override
        public int compare(T o1, T o2) {
            return delegate.compare(mapping.apply(o1), mapping.apply(o2));
        }

        @Override
        public boolean equals(@Nullable final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final MappedComparator that = (MappedComparator) o;
            return delegate.equals(that.delegate) && mapping.equals(that.mapping);
        }

        @Override
        public int hashCode() {
            return 31 * mapping.hashCode() + delegate.hashCode();
        }

        @Override
        public String toString() {
            return "MappedComparator{" +
                    "mapping=" + mapping +
                    ", delegate=" + delegate +
                    '}';
        }
    }

    private static final class RandomComparator<T> implements Comparator<T>, Serializable {

        private static final long serialVersionUID = 0;
        @Nonnull
        private final Random random;
        @Nonnegative
        private final double lessThanProbability;
        @Nonnegative
        private final double greaterThanProbability;

        RandomComparator(@Nonnull final Random random,
                         @Nonnegative final double lessThanProbability,
                         @Nonnegative final double greaterThanProbability) {
            this.random = checkNotNull(random, "random");
            checkArgument(lessThanProbability >= 0 && lessThanProbability <= 1,
                    "P(<) expect in range 0-1; but found " + lessThanProbability);
            checkArgument(greaterThanProbability >= 0 && greaterThanProbability <= 1,
                    "P(>) expect in range 0-1; but found " + lessThanProbability);
            checkArgument(lessThanProbability + greaterThanProbability <= 1,
                    "P(<)+P(>) expect in range 0-1; but found " + (lessThanProbability + greaterThanProbability));
            this.lessThanProbability = lessThanProbability;
            this.greaterThanProbability = greaterThanProbability;
        }

        @Override
        public int compare(final @Nullable T o1, final @Nullable T o2) {
            double value = random.nextDouble();
            if (value < lessThanProbability)
                return -1;
            if (value > (1.0 - greaterThanProbability))
                return +1;
            return 0;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final RandomComparator that = (RandomComparator) o;
            return Double.compare(that.greaterThanProbability, greaterThanProbability) == 0
                    && Double.compare(that.lessThanProbability, lessThanProbability) == 0
                    && random.equals(that.random);

        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            result = random.hashCode();
            temp = Double.doubleToLongBits(lessThanProbability);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(greaterThanProbability);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return String.format("random(%f, %f, %s)", lessThanProbability, greaterThanProbability, random);
        }
    }

    @Immutable
    private static final class ChainedComparator<T> implements Comparator<T>, Serializable {

        private static final long serialVersionUID = 0;
        @Nonnull
        private final List<Comparator<T>> children;

        ChainedComparator(@Nonnull final List<Comparator<T>> children) {
            this.children = checkNotNull(children, "children");
        }

        @Override
        public int compare(T o1, T o2) {
            int result = 0;
            final Iterator<Comparator<T>> it = children.iterator();
            while (result == 0 && it.hasNext())
                result = it.next().compare(o1, o2);
            return result;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChainedComparator that = (ChainedComparator) o;
            return children.equals(that.children);
        }

        @Override
        public int hashCode() {
            return children.hashCode();
        }

        @Override
        public String toString() {
            return "chain(" + children + ')';
        }
    }
}
