package uk.ac.susx.mlcl.erl.lib;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Static utility methods pertaining to {@code Function} instances, extending the operations of {@link Functions}.
 * <p/>
 * <p>All methods return serializable functions as long as they're given serializable parameters.
 *
 * @author Hamish Morgan
 */
public class Functions2 {

    // Static utility library should not be instantiated
    private Functions2() {
    }

    /**
     * Creates a function which attempts to cast in parameter to the given target class.
     * <p/>
     * Rather a silly function, that does very little except risk exceptions. The main use is for chaining function,
     * though even there it's likely that unchecked conventions would be better.
     *
     * @param fromCls
     * @param toClass
     * @param <F>
     * @param <T>
     * @return
     */
    public static <F, T extends F> Function<F, T> cast(final Class<F> fromCls, final Class<T> toClass) {
        return new ClassCastFunction<F, T>(toClass);
    }

    /**
     * Creates a function that returns one of two constants depending on given predicate output.
     *
     * @param predicate
     * @param trueValue
     * @param falseValue
     * @param <I>
     * @param <O>
     * @return
     */
    @Nonnull
    public static <I, O> Function<I, O> forPredicate(@Nonnull final Predicate<I> predicate,
                                                     @Nonnull final O trueValue, @Nonnull final O falseValue) {
        return new PredicateFunction<I, O>(predicate, trueValue, falseValue);
    }

    /**
     * Creates a function that returns the same boolean output as the given predicate for all inputs.
     *
     * @param predicate
     * @param <I>
     * @return
     */
    @Nonnull
    public static <I> Function<I, Boolean> forPredicate(@Nonnull final Predicate<I> predicate) {
        return new PredicateFunction<I, Boolean>(predicate, true, false);
    }

    /**
     * @see Functions#forPredicate
     */
    private static class PredicateFunction<T, O> implements Function<T, O>, Serializable {
        private static final long serialVersionUID = 0;
        private final Predicate<T> predicate;
        private final O trueValue;
        private final O falseValue;

        private PredicateFunction(@Nonnull final Predicate<T> predicate, @Nonnull final O trueValue, @Nonnull final O falseValue) {
            this.predicate = checkNotNull(predicate, "predicate");
            this.trueValue = checkNotNull(trueValue, "trueValue");
            this.falseValue = checkNotNull(falseValue, "falseValue");
        }

        @Override
        public O apply(@Nullable T t) {
            return predicate.apply(t) ? trueValue : falseValue;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof PredicateFunction) {
                PredicateFunction<?, ?> that = (PredicateFunction<?, ?>) obj;
                return predicate.equals(that.predicate)
                        && trueValue.equals(that.trueValue)
                        && falseValue.equals(that.falseValue);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int result = predicate.hashCode();
            result = 31 * result + trueValue.hashCode();
            result = 31 * result + falseValue.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "forPredicate(" +
                    "predicate=" + predicate +
                    ", trueValue=" + trueValue +
                    ", falseValue=" + falseValue +
                    ')';
        }
    }

    private static class ClassCastFunction<F, T extends F> implements Function<F, T>, Serializable {

        private static final long serialVersionUID = 0;
        @Nonnull
        private final Class<T> toClass;

        public ClassCastFunction(final @Nonnull Class<T> toClass) {
            checkNotNull(toClass, "toClass");
            this.toClass = toClass;
        }

        @Nullable
        @Override
        public T apply(@Nullable F input) {
            return toClass.cast(input);
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final ClassCastFunction that = (ClassCastFunction) o;
            return toClass.equals(that.toClass);
        }

        @Override
        public int hashCode() {
            return toClass.hashCode();
        }

        @Override
        public String toString() {
            return "cast(" + toClass + ')';
        }
    }
}
