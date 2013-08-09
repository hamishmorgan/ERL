package uk.ac.susx.mlcl.erl.tac.eval;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 05/08/2013
 * Time: 14:42
 * To change this template use File | Settings | File Templates.
 */
public class EvalUtil {


    public static <T> ImmutableSortedMap<T, Integer> indexMap(@Nonnull final List<T> elements, Comparator<T> c) {
        return indexMap(elements, ImmutableSortedMap.<T, Integer>orderedBy(c)).build();
    }

    public static <T> ImmutableMap<T, Integer> indexMap(@Nonnull final List<T> elements) {
        return indexMap(elements, ImmutableMap.<T, Integer>builder()).build();
    }

    @Nonnull
    private static <T, B extends ImmutableMap.Builder<T, Integer>> B indexMap(@Nonnull final List<T> elements, @Nonnull final B builder) {
        for (int i = 0; i < elements.size(); i++)
            builder.put(elements.get(i), i);
        return builder;
    }


}
