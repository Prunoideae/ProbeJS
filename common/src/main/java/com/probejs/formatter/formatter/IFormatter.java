package com.probejs.formatter.formatter;

import java.util.List;

public interface IFormatter {
    List<String> format(Integer indent, Integer stepIndent);
}
