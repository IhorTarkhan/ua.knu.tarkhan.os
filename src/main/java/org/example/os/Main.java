package org.example.os;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        ArrayList<String> strings = new ArrayList<>();

        strings.add("ffff");
        strings.add("aa");
        strings.add("aaaaa");
        strings.add("abc");

//        strings.remove(2);
//        strings.add(2, "aaaaaaaaaaaaaaaa");
        strings.set(2, "aaaaaaaaaaaaaaaa");

        List<Integer> collect = strings.stream()
                .filter(s -> s.startsWith("a"))
                .map(s -> s.length())
                .collect(Collectors.toList());

        System.out.println(strings.size());


    }
}
