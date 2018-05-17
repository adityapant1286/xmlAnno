package com.xmlanno.demo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Demo {

//    public void demo(@XmlDoc String input) {
//
//    }

    public static void main(String[] args) {
        listToMap();
    }

    static void listToArray() {
        List<String> list = new ArrayList<>();
        list.add("A");
        list.add("B");

        String[] strings = list.toArray(new String[0]);

        Stream.of(strings).forEach(System.out::println);
    }

    static void listToMap() {
        List<A> list = new ArrayList<>();
        list.add(new A("A"));
        list.add(new A("X"));
        list.add(new A("C"));

        Map<String, A> collect = list.stream().collect(LinkedHashMap::new, (map, a) -> map.put(a.s1, a), LinkedHashMap::putAll);

        System.out.println(collect);
    }


    private static class A {
        String s1;
        A(String s1) { this.s1 = s1; }
    }
}
