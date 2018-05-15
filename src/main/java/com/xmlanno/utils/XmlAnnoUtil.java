package com.xmlanno.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

public final class XmlAnnoUtil {

    private XmlAnnoUtil() {
    }

    public static boolean isEmpty(Collection<?> t) {

        if (isNull(t))
            throw new IllegalArgumentException("Input parameter is null");

        return t.isEmpty();
    }

    public static <T> boolean isEmpty(T[] t) { return t == null || t.length == 0; }

    public static boolean hasText(String t) { return !isNull(t) && !t.trim().isEmpty(); }

    public static boolean hasTextAll(String... strings) {

        return !isNull(strings)
                && strings.length > 0
                && Arrays.stream(strings)
                    .noneMatch(s -> (isNull(s) || s.isEmpty()));
    }

    public static String repeat(String str, int times) {

        if (!hasText(str) || times == 0)
            return "";

        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < times; i++)
            sb.append(str);

        return sb.toString();
    }

    public static <T> void forEachRemaining(Enumeration<T> e, Consumer<? super T> c) {
        while (e.hasMoreElements()) c.accept(e.nextElement());
    }

    public static <T, K, U> Collector<T, ?, Map<K, U>> toLinkedMap(Function<? super T, ? extends K> keyMapper,
                                                                   Function<? super T, ? extends U> valueMapper) {
        return Collectors.toMap(keyMapper, valueMapper,
                        (u, v) -> {
                            throw new IllegalStateException(String.format("Duplicate key %s", u));
                        },
                        LinkedHashMap::new);
    }
}
