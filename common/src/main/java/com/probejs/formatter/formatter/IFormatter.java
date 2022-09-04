package com.probejs.formatter.formatter;

import java.util.List;

public interface IFormatter {
    List<String> format(Integer indent, Integer stepIndent);

    default List<String> format() {
        return format(0, 0);
    }

    default String formatFirst() {
        return format().get(0);
    }
}
