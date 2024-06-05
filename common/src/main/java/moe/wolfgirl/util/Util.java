package moe.wolfgirl.util;

import moe.wolfgirl.ProbeJS;
import moe.wolfgirl.docs.formatter.formatter.jdoc.FormatterType;
import moe.wolfgirl.jdoc.property.PropertyType;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Util {

    public static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList("abstract,arguments,boolean,break,byte,case,catch,char,const,continue,constructor,debugger,default,delete,do,double,else,eval,false,final,finally,float,for,function,goto,if,implements,in,instanceof,int,interface,let,long,native,new,null,package,private,protected,public,return,short,static,switch,synchronized,this,throw,throws,transient,true,try,typeof,var,void,volatile,while,with,yield".split(",")));

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

    public static String snakeToTitle(String s) {
        return Arrays.stream(s.split("_")).map(Util::getCapitalized).collect(Collectors.joining());
    }

    public static String pathToTitle(String s) {
        return Arrays.stream(s.split("/")).map(Util::snakeToTitle).collect(Collectors.joining());
    }

    private static final Pattern CAMEL_CASE_MATCH = Pattern.compile("[A-Z][a-z0-9]*");

    public static String camelCaseToSnake(String s) {
        // example: "camelCase" -> "camel_case"
        return CAMEL_CASE_MATCH.matcher(s).replaceAll("_$0").toLowerCase();
    }

    public static String snakeToCamelCase(String s) {
        // example: "snake_case" -> "snakeCase"
        var parts = s.split("_");
        var sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i == 0) {
                sb.append(parts[i]);
            } else {
                sb.append(getCapitalized(parts[i]));
            }
        }
        return sb.toString();
    }

    private static final Pattern JS_IDENTIFIER_MATCH = Pattern.compile("[A-Za-z_$][A-Za-z0-9_$]*");

    /**
     * Guards the method/field name by a matching regex and a list of keywords
     *
     * @param s string input
     * @return the original string if valid, otherwise it will be jsonified.
     */
    public static String getSafeName(String s) {
        return !KEYWORDS.contains(s) && (
                JS_IDENTIFIER_MATCH.matcher(s).matches() ||
                        (s.startsWith("[") && s.endsWith("]"))
        ) ? s : ProbeJS.GSON.toJson(s);
    }

    public static boolean isNameSafe(String s) {
        return !KEYWORDS.contains(s) && JS_IDENTIFIER_MATCH.matcher(s).matches();
    }

    public static String formatMaybeParameterized(Class<?> clazz) {
        if (clazz.getTypeParameters().length == 0) {
            return new FormatterType.Clazz(new PropertyType.Clazz(clazz.getName())).formatFirst();
        } else {
            return new FormatterType.Parameterized(
                    new PropertyType.Parameterized(
                            new PropertyType.Clazz(clazz.getName()),
                            Collections.nCopies(clazz.getTypeParameters().length, new PropertyType.Clazz(Object.class.getName()))
                    )
            ).formatFirst();
        }
    }

    public interface TrySupplier<T> {
        T get() throws Exception;
    }

    public static String indent(int num) {
        return " ".repeat(num);
    }
}
