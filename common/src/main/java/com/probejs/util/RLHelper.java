package com.probejs.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public class RLHelper {
    public static String[] resourceLocationToPath(String resourceLocation) {
        return resourceLocation.split("/");
    }

    public static String finalComponentToCamel(String resourceLocation) {
        String[] path = resourceLocationToPath(resourceLocation);
        String last = path[path.length - 1];
        return Arrays.stream(last.split("_")).map(Util::getCapitalized).collect(Collectors.joining());
    }

    public static String rlToCamel(String s) {
        return Arrays.stream(s.split("/")).map(Util::snakeToCapitalized).collect(Collectors.joining());
    }
}
