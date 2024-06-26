package moe.wolfgirl.probejs.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NameUtils {
    public static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList("abstract,arguments,boolean,break,byte,case,catch,char,const,continue,constructor,debugger,default,delete,do,double,else,eval,false,final,finally,float,for,function,goto,if,implements,in,instanceof,int,interface,let,long,native,new,null,package,private,protected,public,return,short,static,switch,synchronized,this,throw,throws,transient,true,try,typeof,var,void,volatile,while,with,yield,export".split(",")));
    public static final Pattern JS_IDENTIFIER_MATCH = Pattern.compile("[A-Za-z_$][A-Za-z0-9_$]*");
    public static final Pattern MATCH_IMPORT = Pattern.compile("^import \\{(.+)} from (.+)");
    public static final Pattern MATCH_CONST_REQUIRE = Pattern.compile("^const \\{(.+)} = require\\((.+)\\)");
    public static final Pattern MATCH_ANY_REQUIRE = Pattern.compile("^.+ \\{(.+)} = require\\((.+)\\)");


    public static String[] extractAlphabets(String input) {
        return input.split("[^a-zA-Z]+");
    }

    public static String asCamelCase(String[] words) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (!word.isEmpty()) {
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

    public static String firstLower(String word) {
        return Character.toLowerCase(word.charAt(0)) +
                word.substring(1);
    }

    public static String[] resourceLocationToPath(String resourceLocation) {
        return resourceLocation.split("/");
    }

    public static String finalComponentToTitle(String resourceLocation) {
        String[] path = resourceLocationToPath(resourceLocation);
        String last = path[path.length - 1];
        return Arrays.stream(last.split("_")).map(NameUtils::getCapitalized).collect(Collectors.joining());
    }

    public static String rlToTitle(String s) {
        return Arrays.stream(s.split("/")).map(NameUtils::snakeToTitle).collect(Collectors.joining());
    }

    public static boolean isNameSafe(String s) {
        return !KEYWORDS.contains(s) && JS_IDENTIFIER_MATCH.matcher(s).matches();
    }

    public static String getCapitalized(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static String snakeToTitle(String s) {
        return Arrays.stream(s.split("_")).map(NameUtils::getCapitalized).collect(Collectors.joining());
    }

    public static String replaceRegion(String str, int start, int end, String oldText, String newText) {
        if (start < 0 || start >= end || end > str.length()) {
            throw new IllegalArgumentException("Invalid start or end index");
        }

        String prefix = str.substring(0, start);
        String region = str.substring(start, end);
        String suffix = str.substring(end);

        String replacedRegion = region.replace(oldText, newText);

        return prefix + replacedRegion + suffix;
    }

    public static String cutOffStartEnds(String str, List<Integer[]> pairs) {
        StringBuilder result = new StringBuilder(str);

        // Iterate over the pairs in reverse order
        for (int i = pairs.size() - 1; i >= 0; i--) {
            int start = pairs.get(i)[0];
            int end = pairs.get(i)[1] + 1;

            // Cut off the substring from start to end (exclusive)
            result.delete(start, end);
        }

        return result.toString();
    }
}
