package com.probejs.formatter.formatter;

import java.util.List;

@FunctionalInterface
public interface IFormatter {
    List<String> format(Integer indent, Integer stepIndent);

    default List<String> format() {
        return format(0, 4);
    }

    default String formatFirst() {
        return String.join("\n", format());
    }
}
