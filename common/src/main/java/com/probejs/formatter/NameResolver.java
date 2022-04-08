package com.probejs.formatter;

import com.google.gson.Gson;
import com.probejs.info.type.ITypeInfo;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NameResolver {
    public static class ResolvedName {
        public static final ResolvedName UNRESOLVED = new ResolvedName(List.of("any"));
        private final List<String> names;

        private ResolvedName(List<String> names) {
            this.names = names.stream().map(NameResolver::getNameSafe).collect(Collectors.toList());
        }

        public String getFullName() {
            return String.join(".", names);
        }

        public String getNamespace() {
            return String.join(".", names.subList(0, names.size() - 1));
        }

        public String getLastName() {
            return names.get(names.size() - 1);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ResolvedName that = (ResolvedName) o;
            return names.equals(that.names);
        }

        @Override
        public int hashCode() {
            return Objects.hash(names);
        }

        @Override
        public String toString() {
            return "ResolvedName{" +
                    "names=" + names +
                    '}';
        }
    }

    public static final HashMap<String, ResolvedName> resolvedNames = new HashMap<>();
    public static final HashMap<Class<?>, Function<ITypeInfo, String>> specialTypeFormatters = new HashMap<>();
    public static final HashMap<Class<?>, Function<Object, String>> specialValueFormatters = new HashMap<>();
    public static final Set<String> keywords = new HashSet<>();

    public static void putResolvedName(String className, String resolvedName) {
        putResolvedName(className, new ResolvedName(Arrays.stream(resolvedName.split("\\.")).toList()));
    }

    public static void putResolvedName(String className, ResolvedName resolvedName) {
        if (!resolvedNames.containsKey(className))
            resolvedNames.put(className, resolvedName);
    }

    public static void putResolvedName(Class<?> className, ResolvedName resolvedName) {
        putResolvedName(className.getName(), resolvedName);
    }

    public static void putResolvedName(Class<?> className, String resolvedName) {
        putResolvedName(className, new ResolvedName(Arrays.stream(resolvedName.split("\\.")).toList()));
    }

    public static ResolvedName getResolvedName(String className) {
        return resolvedNames.getOrDefault(className, ResolvedName.UNRESOLVED);
    }

    public static void putTypeFormatter(Class<?> className, Function<ITypeInfo, String> formatter) {
        specialTypeFormatters.put(className, formatter);
    }

    public static boolean isTypeSpecial(Class<?> clazz) {
        return specialTypeFormatters.containsKey(clazz);
    }

    public static void putValueFormatter(Function<Object, String> transformer, Class<?>... classes) {
        for (Class<?> clazz : classes)
            specialValueFormatters.put(clazz, transformer);
    }

    public static String formatValue(Object object) {
        if (object == null)
            return null;
        if (specialValueFormatters.containsKey(object.getClass()))
            return specialValueFormatters.get(object.getClass()).apply(object);
        for (Map.Entry<Class<?>, Function<Object, String>> entry : specialValueFormatters.entrySet()) {
            if (entry.getKey().isAssignableFrom(object.getClass()))
                return entry.getValue().apply(object);
        }
        return null;
    }

    public static void resolveNames(Set<Class<?>> classes) {
        Set<ResolvedName> usedNames = new HashSet<>(resolvedNames.values());
        for (Class<?> clazz : classes) {
            ResolvedName resolved = new ResolvedName(Arrays.stream(clazz.getName().split("\\.")).toList());
            ResolvedName internal = new ResolvedName(List.of("Internal", resolved.getLastName()));
            if (usedNames.contains(internal))
                putResolvedName(clazz.getName(), resolved);
            else {
                putResolvedName(clazz.getName(), internal);
                usedNames.add(internal);
            }
        }
    }

    public static void addKeyword(String kw) {
        keywords.add(kw);
    }

    public static String getNameSafe(String kw) {
        return keywords.contains(kw) ? kw + "_" : kw;
    }

    public static void init() {
        putResolvedName(Object.class, "object");
        putResolvedName(String.class, "string");
        putResolvedName(Character.class, "string");
        putResolvedName(Character.TYPE, "string");

        putResolvedName(Void.class, "void");
        putResolvedName(Void.TYPE, "void");

        putResolvedName(Long.class, "number");
        putResolvedName(Long.TYPE, "number");
        putResolvedName(Integer.class, "number");
        putResolvedName(Integer.TYPE, "number");
        putResolvedName(Short.class, "number");
        putResolvedName(Short.TYPE, "number");
        putResolvedName(Byte.class, "number");
        putResolvedName(Byte.TYPE, "number");

        putResolvedName(Double.class, "number");
        putResolvedName(Double.TYPE, "number");
        putResolvedName(Float.class, "number");
        putResolvedName(Float.TYPE, "number");

        putResolvedName(Boolean.class, "boolean");
        putResolvedName(Boolean.TYPE, "boolean");

        Gson gson = new Gson();

        putValueFormatter(gson::toJson,
                String.class, Character.class, Character.TYPE,
                Long.class, Long.TYPE, Integer.class, Integer.TYPE,
                Short.class, Short.TYPE, Byte.class, Byte.TYPE,
                Double.class, Double.TYPE, Float.class, Float.TYPE,
                Boolean.class, Boolean.TYPE);

        addKeyword("function");
        addKeyword("debugger");
        addKeyword("in");
        addKeyword("with");
        addKeyword("java");

        SpecialTypes.init();
    }
}
