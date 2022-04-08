package com.probejs.formatter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class ClassResolver {
    public static Set<Class<?>> skipped = new HashSet<>();

    public static void skipClass(Class<?>... clazz) {
        skipped.addAll(Arrays.asList(clazz));
    }

    public static boolean acceptMethod(String methodName) {
        return !methodName.equals("constructor") && !Pattern.matches("^[fm]_[\\d_]+$", methodName);
    }

    public static boolean acceptField(String fieldName) {
        return !fieldName.equals("constructor") && !Pattern.matches("^[fm]_[\\d_]+$", fieldName);
    }

    public static void init() {
        skipClass(Object.class);
        skipClass(Void.class, Void.TYPE);
        skipClass(String.class, Character.class, Character.TYPE);
        skipClass(Long.class, Long.TYPE);
        skipClass(Integer.class, Integer.TYPE);
        skipClass(Short.class, Short.TYPE);
        skipClass(Byte.class, Byte.TYPE);
        skipClass(Double.class, Double.TYPE, Float.class, Float.TYPE);
        skipClass(Boolean.class, Boolean.TYPE);
    }


}
