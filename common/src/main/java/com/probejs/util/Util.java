package com.probejs.util;

import com.probejs.ProbeJS;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Util {

    public static final Set<String> KEYWORDS = new HashSet<>() {
        {
            var keywords = "abstract,arguments,boolean,break,byte,case,catch,char,const,continue,debugger,default,delete,do,double,else,eval,false,final,finally,float,for,function,goto,if,implements,in,instanceof,int,interface,let,long,native,new,null,package,private,protected,public,return,short,static,switch,synchronized,this,throw,throws,transient,true,try,typeof,var,void,volatile,while,with,yield".split(",");
            addAll(List.of(keywords));
        }
    };

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

    /**
     * Guards the method/field name by a matching regex and a list of keywords
     *
     * @param s string input
     * @return the original string if valid, otherwise it will be jsonify.
     */
    public static String getSafeName(String s) {
        return !KEYWORDS.contains(s) && s.matches("^[$A-Z_][0-9A-Z_$]*$") ? s : ProbeJS.GSON.toJson(s);
    }

    public interface TrySupplier<T> {
        T get() throws Exception;
    }

    public static String indent(int num) {
        return " ".repeat(num);
    }
}
