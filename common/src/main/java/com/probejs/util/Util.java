package com.probejs.util;

import com.probejs.ProbeJS;
import com.probejs.compiler.formatter.formatter.jdoc.FormatterType;
import com.probejs.jdoc.property.PropertyType;

import java.util.*;
import java.util.stream.Collectors;

public class Util {

    public static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList("abstract,arguments,boolean,break,byte,case,catch,char,const,continue,debugger,default,delete,do,double,else,eval,false,final,finally,float,for,function,goto,if,implements,in,instanceof,int,interface,let,long,native,new,null,package,private,protected,public,return,short,static,switch,synchronized,this,throw,throws,transient,true,try,typeof,var,void,volatile,while,with,yield".split(",")));

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
    
    /**
     * Guards the method/field name by a matching regex and a list of keywords
     *
     * @param s string input
     * @return the original string if valid, otherwise it will be jsonify.
     */
    public static String getSafeName(String s) {
        return !KEYWORDS.contains(s) && (
                s.toUpperCase().matches("^[$A-Z_][0-9A-Z_$]*$") |
                        (s.startsWith("[") && s.endsWith("]"))
        ) ? s : ProbeJS.GSON.toJson(s);
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
