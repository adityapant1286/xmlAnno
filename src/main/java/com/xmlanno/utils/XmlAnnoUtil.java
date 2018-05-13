package com.xmlanno.utils;

import java.util.Arrays;
import java.util.Collection;

import static java.util.Objects.isNull;

public final class XmlAnnoUtil {

    private XmlAnnoUtil() {
    }

    public static boolean isEmpty(Collection<?> t) {

        if (isNull(t))
            throw new IllegalArgumentException("Input parameter is null");

        return t.isEmpty();
    }

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
}
