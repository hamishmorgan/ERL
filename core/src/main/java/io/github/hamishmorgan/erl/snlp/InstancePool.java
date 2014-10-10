/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package io.github.hamishmorgan.erl.snlp;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.reflect.MutableTypeToInstanceMap;
import com.google.common.reflect.TypeToInstanceMap;
import com.google.common.reflect.TypeToken;
import edu.stanford.nlp.util.Factory;
import javax.annotation.Nullable;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;

public class InstancePool<K, T> {

    private final Map<K, TypeToken<T>> names;

    @Nonnull
    private final TypeToInstanceMap<T> instances;

    private final Map<TypeToken<T>, Factory<T>> factories;

    private final boolean attemptDefaultConstuctor;

    private InstancePool(Map<K, TypeToken<T>> names,
                         Map<TypeToken<T>, Factory<T>> factories,
                         boolean attemptDefaultConstuctor) {
        this.instances = new MutableTypeToInstanceMap<T>();
        this.names = names;
        this.factories = factories;
        this.attemptDefaultConstuctor = attemptDefaultConstuctor;
    }

    @Nonnull
    public static <K, T> Builder<K, T> builder() {
        return new Builder<K, T>();
    }

    @Nonnull
    public Set<K> keySet() {
        return names.keySet();
    }

    @Nullable
    public T getInstance(K name) throws InstantiationException {

        final TypeToken<T> type = names.get(name);

        synchronized (instances) {
            if (!instances.containsKey(type)) {
                instances.putInstance(type, newInstance(type));
            }
        }

        return instances.getInstance(type);
    }

    private T newInstance(@Nonnull TypeToken<T> t) throws InstantiationException {
        if (factories.containsKey(t)) {
            return factories.get(t).create();
        }
        if (attemptDefaultConstuctor) {
            try {
                @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"}) // T should be a class type
                final T instance = (T) t.getRawType().newInstance();
                return instance;
            } catch (IllegalAccessException ex) {
                throw new InstantiationException();
            }
        }
        throw new InstantiationException();
    }

    public static class Builder<K, T> {

        private final ImmutableMap.Builder<K, TypeToken<T>> names;

        private final Map<TypeToken<T>, Factory<T>> factories;

        private boolean defaultConstructionAttempted;

        public Builder() {
            names = ImmutableMap.builder();
            factories = Maps.newHashMap();
            defaultConstructionAttempted = true;
        }

        @Nonnull
        private Builder<K, T> addFactory(K name, TypeToken<T> type, Factory<T> factory) {
            names.put(name, type);
            factories.put(type, factory);
            return this;
        }

        @Nonnull
        public Builder<K, T> addFactory(K name, Class<T> type, Factory<T> factory) {
            return addFactory(name, TypeToken.of(type), factory);
        }

        @Nonnull
        public Builder<K, T> setDefaultConstructionAttempted(
                boolean defaultConstructionAttempted) {
            this.defaultConstructionAttempted = defaultConstructionAttempted;
            return this;
        }

        @Nonnull
        public InstancePool<K, T> build() {
            final ImmutableMap<K, TypeToken<T>> namesMap = names.build();

            final ImmutableMap<TypeToken<T>, Factory<T>> factoriesMap =
                    ImmutableMap.copyOf(factories);

            return new InstancePool<K, T>(
                    namesMap, factoriesMap,
                    defaultConstructionAttempted);
        }
    }
}
