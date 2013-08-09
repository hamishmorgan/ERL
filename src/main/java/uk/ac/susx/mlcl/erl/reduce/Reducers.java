package uk.ac.susx.mlcl.erl.reduce;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A factory for various reduce operations.
 * <p/>
 * Most of these are experiments to see what is possible. The only one actually in use, at time of writing, is the sum
 * operation over doubles: {@link uk.ac.susx.mlcl.erl.reduce.Reducers.Doubles#sum()}}.
 */
public class Reducers {

    @Nonnull
    public static <T> Reducer<Map<Integer, T>, T> enumerate() {
        return new Reducer<Map<Integer, T>, T>() {

            private final AtomicInteger nextIndex = new AtomicInteger(0);

            @Nonnull
            @Override
            public Map<Integer, T> foldIn(Map<Integer, T> accum, T next) {
                accum.put(nextIndex.getAndIncrement(), next);
                return accum;
            }
        };
    }

    public static final class CharSequences {
        @Nonnull
        public static Reducer<String, CharSequence> concat() {
            return new Reducer<String, CharSequence>() {
                @Nonnull
                @Override
                public String foldIn(String accum, CharSequence next) {
                    return accum + next;
                }
            };
        }

        @Nonnull
        public static Reducer<StringBuilder, CharSequence> append() {
            return new Reducer<StringBuilder, CharSequence>() {
                @Nonnull
                @Override
                public StringBuilder foldIn(StringBuilder accum, CharSequence next) {
                    return accum.append(next);
                }
            };
        }

        @Nonnull
        public static Reducer<Appendable, CharSequence> append2() {
            return new Reducer<Appendable, CharSequence>() {
                @Nonnull
                @Override
                public Appendable foldIn(Appendable accum, CharSequence next) {
                    try {
                        return accum.append(next);
                    } catch (IOException e) {
                        // XXX: This obviously isn't good
                        throw new RuntimeException(e);
                    }
                }
            };
        }

    }

    public static final class Comparables {
        @Nonnull
        public static <T extends Comparable<T>> Reducer<T, T> max() {
            return new Reducer<T, T>() {
                @Nonnull
                @Override
                public T foldIn(T accum, T next) {
                    return accum.compareTo(next) >= 0 ? accum : next;
                }
            };
        }

        @Nonnull
        public static <T extends Comparable<T>> Reducer<T, T> min() {
            return new Reducer<T, T>() {
                @Nonnull
                @Override
                public T foldIn(T accum, T next) {
                    return accum.compareTo(next) <= 0 ? accum : next;
                }
            };
        }
    }

    public static final class Longs {
        @Nonnull
        public static Reducer<Long, Long> sum() {
            return new Reducer<Long, Long>() {
                @Nonnull
                @Override
                public Long foldIn(Long accum, Long next) {
                    return accum + next;
                }
            };
        }

        @Nonnull
        public static Reducer<Long, Long> leastCommonMultipleLong() {
            return new Reducer<Long, Long>() {
                @Nonnull
                @Override
                public Long foldIn(Long accum, Long next) {
                    return accum * next / gcd(accum, next);
                }

                private long gcd(long a, long b) {
                    while (b != 0) {
                        long temp = b;
                        b = a % b;
                        a = temp;
                    }
                    return a;
                }
            };
        }

        @Nonnull
        public static Reducer<Long, Long> xor() {
            return new Reducer<Long, Long>() {
                @Nonnull
                @Override
                public Long foldIn(Long accum, Long next) {
                    return accum ^ next;
                }
            };
        }
    }

    public static final class Booleans {
        @Nonnull
        public static Reducer<Boolean, Boolean> and() {
            return new Reducer<Boolean, Boolean>() {
                @Nonnull
                @Override
                public Boolean foldIn(Boolean accum, Boolean next) {
                    return accum && next;
                }
            };
        }

        @Nonnull
        public static Reducer<Boolean, Boolean> or() {
            return new Reducer<Boolean, Boolean>() {
                @Nonnull
                @Override
                public Boolean foldIn(Boolean accum, Boolean next) {
                    return accum || next;
                }
            };
        }

        @Nonnull
        public static Reducer<Boolean, Boolean> xor() {
            return new Reducer<Boolean, Boolean>() {
                @Nonnull
                @Override
                public Boolean foldIn(Boolean accum, Boolean next) {
                    return accum ^ next;
                }
            };
        }
    }

    public static class Doubles {
        @Nonnull
        public static Reducer<Double, Double> sum() {
            return new Reducer<Double, Double>() {
                @Nonnull
                @Override
                public Double foldIn(Double accum, Double next) {
                    return accum + next;
                }
            };
        }

        @Nonnull
        public static Reducer<Double, Double> multiply() {
            return new Reducer<Double, Double>() {
                @Nonnull
                @Override
                public Double foldIn(Double accum, Double next) {
                    return accum * next;
                }
            };
        }
    }

    public static class Collections {

        @Nonnull
        public static <T, C extends Collection<? super T>> Reducer<C, T> add() {
            return new Reducer<C, T>() {
                @Nonnull
                @Override
                public C foldIn(C accum, T next) {
                    accum.add(next);
                    return accum;
                }
            };
        }

        @Nonnull
        public static <X, A extends Collection<? super X>, T extends Collection<? extends X>> Reducer<A, T> flatten() {
            return new Reducer<A, T>() {
                @Nonnull
                @Override
                public A foldIn(A accum, T next) {
                    accum.addAll(next);
                    return accum;
                }
            };
        }

    }

    public static class Sets {
        @Nonnull
        public static <T> Reducer<Set<T>, Set<T>> union() {
            return new Reducer<Set<T>, Set<T>>() {
                @Nonnull
                @Override
                public Set<T> foldIn(Set<T> accum, Set<T> next) {
                    return com.google.common.collect.Sets.union(accum, next);
                }
            };
        }

        @Nonnull
        public static <T> Reducer<Set<T>, Set<T>> intersection() {
            return new Reducer<Set<T>, Set<T>>() {
                @Nonnull
                @Override
                public Set<T> foldIn(Set<T> accum, Set<T> next) {
                    return com.google.common.collect.Sets.intersection(accum, next);
                }
            };
        }

    }

    public static class Lists {

    }


}
