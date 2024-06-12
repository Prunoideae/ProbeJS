package moe.wolfgirl.next.utils;

import moe.wolfgirl.util.Util;

import java.util.Arrays;
import java.util.stream.Collectors;

public class NameUtils {
    public static String[] extractAlphabets(String input) {
        return input.split("[^a-zA-Z]+");
    }

    public static String asCamelCase(String[] words) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.length() > 0) {
                if (i == 0) {
                    result.append(Character.toLowerCase(word.charAt(0)));
                } else {
                    result.append(Character.toUpperCase(word.charAt(0)));
                }
                result.append(word.substring(1));
            }
        }
        return result.toString();
    }

    public static String[] resourceLocationToPath(String resourceLocation) {
        return resourceLocation.split("/");
    }

    public static String finalComponentToTitle(String resourceLocation) {
        String[] path = resourceLocationToPath(resourceLocation);
        String last = path[path.length - 1];
        return Arrays.stream(last.split("_")).map(Util::getCapitalized).collect(Collectors.joining());
    }

    public static String rlToTitle(String s) {
        return Arrays.stream(s.split("/")).map(Util::snakeToTitle).collect(Collectors.joining());
    }
}
