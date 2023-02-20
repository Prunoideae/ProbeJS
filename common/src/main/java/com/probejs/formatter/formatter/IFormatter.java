package com.probejs.formatter.formatter;

import java.util.List;
import java.util.function.Function;

@FunctionalInterface
public interface IFormatter {
    List<String> format(Integer indent, Integer stepIndent);

    default List<String> format() {
        return format(0, 4);
    }

    default String formatString(Integer indent, Integer stepIndent) {
        return String.join("\n", format(indent, stepIndent));
    }

    default String formatFirst() {
        return String.join("\n", format());
    }

    default String formatAdapted(Function<IFormatter, String> formatterMethod) {
        return formatFirst();
    }

    default String formatClassVariable() {
        return formatAdapted(IFormatter::formatClassVariable);
    }

    default String formatMethodVariable() {
        return formatAdapted(IFormatter::formatMethodVariable);
    }

    default String formatParamVariable() {
        return formatAdapted(IFormatter::formatParamVariable);
    }

    default String formatFieldVariable() {
        return formatAdapted(IFormatter::formatParamVariable);
    }
}
