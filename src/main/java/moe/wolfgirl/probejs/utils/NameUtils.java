package moe.wolfgirl.probejs.utils;

import java.util.Arrays;
import java.util.HashSet;
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
}
