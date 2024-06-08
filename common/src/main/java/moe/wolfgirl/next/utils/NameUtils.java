package moe.wolfgirl.next.utils;

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
}
