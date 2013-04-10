/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.snlp;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.reflect.MutableTypeToInstanceMap;
import com.google.common.reflect.TypeToInstanceMap;
import com.google.common.reflect.TypeToken;
import edu.stanford.nlp.util.Factory;

import java.util.Map;
import java.util.Set;

/**
 * @param <K>
 * @param <T>
 * @author hamish
 */
public class InstancePool<K, T> {

    private final Map<K, TypeToken<T>> names;

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

    public static <K, T> Builder<K, T> builder() {
        return new Builder<K, T>();
    }

    public Set<K> keySet() {
        return names.keySet();
    }

    public T getInstance(K name) throws InstantiationException {

        final TypeToken<T> type = names.get(name);

        synchronized (instances) {
            if (!instances.containsKey(type)) {
                instances.putInstance(type, newInstance(type));
            }
        }

        return instances.getInstance(type);
    }

    private T newInstance(TypeToken<T> t) throws InstantiationException {
        if (factories.containsKey(t)) {
            T instance = factories.get(t).create();
            return instance;
        }
        if (attemptDefaultConstuctor) {
            try {
                T instance = (T) t.getRawType().newInstance();
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

        private Builder<K, T> addFactory(K name, TypeToken<T> type, Factory<T> factory) {
            names.put(name, type);
            factories.put(type, factory);
            return this;
        }

        public Builder<K, T> addFactory(K name, Class<T> type, Factory<T> factory) {
            return addFactory(name, TypeToken.of(type), factory);
        }

        public Builder<K, T> setDefaultConstructionAttempted(
                boolean defaultConstructionAttempted) {
            this.defaultConstructionAttempted = defaultConstructionAttempted;
            return this;
        }

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
