package com.xmlanno.reflection;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.xmlanno.configs.Configurations;
import com.xmlanno.utils.TailCall;
import com.xmlanno.utils.TailCalls;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Objects.isNull;

public class Warehouse {

    private transient boolean concurrent;

    private final Map<String, Multimap<String, String>> warehouseMap;

    protected Warehouse() {
        warehouseMap = new HashMap<>(0);
        concurrent = false;
    }

    public Warehouse(Configurations configs) {
        warehouseMap = new HashMap<>(0);
        concurrent = !isNull(configs.getExecutorService());
    }

    public Set<String> keySet() { return warehouseMap.keySet(); }

    public Multimap<String, String> getOrCreate(String index) {

        Multimap<String, String> multiMap = warehouseMap.get(index);

        if (isNull(multiMap)) {
            multiMap = createMultiMap();
            warehouseMap.put(index, multiMap);
        }

        return multiMap;
    }

    private Multimap<String, String> createMultiMap() {

        SetMultimap<String, String> multiMap = Multimaps.newSetMultimap(new HashMap<>(),
                () -> Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>()));

        return concurrent ? Multimaps.synchronizedSetMultimap(multiMap) :multiMap;
    }

    public Multimap<String, String> get(String index) {

        final Multimap<String, String> multiMap = warehouseMap.get(index);

        if (isNull(multiMap))
            throw new ReflectionsException(String.format("Scanner %s was not configured", index));

        return multiMap;
    }

    /** get the values stored for the given {@code index} and {@code keys} */
    public Iterable<String> get(String index, String... keys) {
        return get(index, Arrays.asList(keys));
    }

    /** get the values stored for the given {@code index} and {@code keys} */
    public Iterable<String> get(String index, Iterable<String> keys) {

        final Multimap<String, String> multiMap = get(index);

        final IterableChain<String> result = new IterableChain<>();

        keys.forEach(k -> result.addAll(multiMap.get(k)));

        return result;
    }

    /** recursively get the values stored for the given {@code index} and {@code keys}, including keys */
    private Iterable<String> getAllIncluding(final String index, final Iterable<String> keys, final IterableChain<String> result) {

        result.addAll(keys);

        keys.forEach(key -> {
            Iterable<String> values = get(index, key);
            if (values.iterator().hasNext())
                getAllIncluding(index, values, result);
        });

        return result;
    }


    /** recursively get the values stored for the given {@code index} and {@code keys}, not including keys */
    public Iterable<String> getAll(String index, String key) { return getAllIncluding(index, get(index, key), new IterableChain<>()); }

    /** recursively get the values stored for the given {@code index} and {@code keys}, not including keys */
    public Iterable<String> getAll(String index, Iterable<String> keys) { return getAllIncluding(index, get(index, keys), new IterableChain<>()); }

    private static class IterableChain<T> implements Iterable<T> {

        private final List<Iterable<T>> chain = Lists.newArrayList();

        private void addAll(Iterable<T> iterable) { chain.add(iterable); }

        public Iterator<T> iterator() { return Iterables.concat(chain).iterator(); }
    }
}
