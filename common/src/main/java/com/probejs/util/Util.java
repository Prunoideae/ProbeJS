package com.probejs.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Util {
    public static <T> T tryOrDefault(TrySupplier<T> toEval, T defaultValue) {
        try {
            return toEval.get();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static String getCapitalized(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static String snakeToCapitalized(String s) {
        return Arrays.stream(s.split("_")).map(Util::getCapitalized).collect(Collectors.joining());
    }

    public static String rlToCapitalized(String s) {
        return Arrays.stream(s.split("/")).map(Util::snakeToCapitalized).collect(Collectors.joining());
    }

    public interface TrySupplier<T> {
        T get() throws Exception;
    }

    public static String indent(int num) {
        return " ".repeat(num);
    }
}
