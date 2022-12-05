package com.probejs.formatter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.probejs.ProbeJS;
import com.probejs.info.ClassInfo;
import com.probejs.info.MethodInfo;
import com.probejs.info.type.ITypeInfo;
import com.probejs.jdoc.document.DocumentClass;
import dev.latvian.mods.kubejs.block.MaterialJS;
import dev.latvian.mods.kubejs.block.MaterialListJS;
import dev.latvian.mods.rhino.util.RemapForJS;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.world.damagesource.DamageSource;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
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

    public static final HashMap<String, List<ResolvedName>> resolvedNames = new HashMap<>();
    public static final HashMap<Class<?>, Function<ITypeInfo, String>> specialTypeFormatters = new HashMap<>();
    public static final HashMap<Class<?>, Boolean> specialTypeGuards = new HashMap<>();
    public static final Multimap<String, Supplier<List<String>>> specialClassAssigner = ArrayListMultimap.create();
    public static final List<String> nameResolveSpecials = new ArrayList<>();
    public static final Set<String> keywords = new HashSet<>();
    public static final Set<String> resolvedPrimitives = new HashSet<>();

    public static void putResolvedName(String className, String resolvedName) {
        putResolvedName(className, new ResolvedName(Arrays.stream(resolvedName.split("\\.")).toList()));
    }

    //Put a resolved name to the class.
    public static void putResolvedName(String className, ResolvedName resolvedName) {
        List<ResolvedName> resolvedNameList = resolvedNames.computeIfAbsent(className, s -> new ArrayList<>());
        if (!resolvedNameList.contains(resolvedName))
            resolvedNameList.add(resolvedName);
    }

    public static void putResolvedName(Class<?> className, ResolvedName resolvedName) {
        String remappedName = ClassInfo.getOrCache(className).getName();
        putResolvedName(remappedName, resolvedName);
    }

    public static void putResolvedName(Class<?> className, String resolvedName) {
        if (className.isAnnotationPresent(RemapForJS.class))
            resolvedName = className.getAnnotation(RemapForJS.class).value();
        putResolvedName(className, new ResolvedName(Arrays.stream(resolvedName.split("\\.")).toList()));
    }

    @MethodsReturnNonnullByDefault
    public static ResolvedName getResolvedName(String className) {
        List<ResolvedName> names = resolvedNames.get(className);
        if (names == null || names.size() == 0)
            return ResolvedName.UNRESOLVED;

        return names.get(0);
    }

    public static void putTypeFormatter(Class<?> className, Function<ITypeInfo, String> formatter) {
        specialTypeFormatters.put(className, formatter);
    }

    public static void putTypeGuard(boolean isSafe, Class<?>... classes) {
        for (Class<?> clazz : classes)
            specialTypeGuards.put(clazz, isSafe);
    }

    public static boolean isTypeSpecial(Class<?> clazz) {
        return specialTypeFormatters.containsKey(clazz);
    }

    public static boolean findResolvedName(ResolvedName name) {
        return resolvedNames.values().stream().anyMatch(names -> names.contains(name));
    }

    //Resolves a name.
    //Will skip if the name is already resolved.
    public static void resolveName(DocumentClass document) {
        String name = document.getName();
        if (resolvedNames.containsKey(name))
            return;
        ResolvedName resolved = new ResolvedName(Arrays.stream(name.split("\\.")).toList());
        ResolvedName internal = new ResolvedName(List.of("Internal", resolved.getLastName()));
        if (findResolvedName(internal)) {
            putResolvedName(name, resolved);
        } else {
            putResolvedName(name, internal);
        }
    }

    public static void addKeyword(String kw) {
        keywords.add(kw);
    }

    public static String getNameSafe(String kw) {
        return keywords.contains(kw) ? kw + "_" : kw;
    }

    public static void putResolvedPrimitive(Class<?> clazz, String resolvedName) {
        putResolvedName(clazz, resolvedName);
        resolvedPrimitives.add(clazz.getName());
    }

    public static void putSpecialAssignments(Class<?> clazz, Supplier<List<String>> assigns) {
        specialClassAssigner.put(MethodInfo.getRemappedOrOriginalClass(clazz), assigns);
    }

    public static List<String> getClassAssignments(String clazz) {
        ArrayList<String> assignables = new ArrayList<>();
        specialClassAssigner.get(clazz).stream().map(Supplier::get).forEach(assignables::addAll);
        return assignables;
    }

    public static void addPrioritizedPackage(String prior) {
        //Add a point to the end to prevent overlapping of package names
        //For example, net.minecraft and net.minecraftforge
        nameResolveSpecials.add(prior + ".");
    }

    public static List<DocumentClass> priorSortClasses(Iterable<DocumentClass> classes) {
        Multimap<Integer, DocumentClass> partMap = ArrayListMultimap.create();
        for (DocumentClass clazz : classes) {
            int index = -1;
            String name = clazz.getName();
            for (int i = 0; i < nameResolveSpecials.size(); i++) {
                if (name.startsWith(nameResolveSpecials.get(i)))
                    index = i;
            }
            partMap.put(index, clazz);
        }
        ArrayList<DocumentClass> result = new ArrayList<>();
        for (int i = 0; i < nameResolveSpecials.size(); i++) {
            if (partMap.containsKey(i))
                result.addAll(partMap.get(i));
        }
        if (partMap.containsKey(-1))
            result.addAll(partMap.get(-1));
        return result;
    }

    private static boolean initialized = false;

    @SuppressWarnings("unchecked")
    public static void init() {
        if (initialized)
            return;
        initialized = true;

        resolvedNames.clear();

        putResolvedPrimitive(Object.class, "any");
        putResolvedPrimitive(String.class, "string");
        putResolvedPrimitive(Character.class, "string");
        putResolvedPrimitive(Character.TYPE, "string");

        putResolvedPrimitive(Void.class, "void");
        putResolvedPrimitive(Void.TYPE, "void");

        putResolvedPrimitive(Long.class, "number");
        putResolvedPrimitive(Long.TYPE, "number");
        putResolvedPrimitive(Integer.class, "number");
        putResolvedPrimitive(Integer.TYPE, "number");
        putResolvedPrimitive(Short.class, "number");
        putResolvedPrimitive(Short.TYPE, "number");
        putResolvedPrimitive(Byte.class, "number");
        putResolvedPrimitive(Byte.TYPE, "number");

        putResolvedPrimitive(Double.class, "number");
        putResolvedPrimitive(Double.TYPE, "number");
        putResolvedPrimitive(Float.class, "number");
        putResolvedPrimitive(Float.TYPE, "number");

        putResolvedPrimitive(Boolean.class, "boolean");
        putResolvedPrimitive(Boolean.TYPE, "boolean");

        putSpecialAssignments(DamageSource.class, () -> {
            List<String> result = new ArrayList<>();
            try {
                for (var field : DamageSource.class.getDeclaredFields()) {
                    field.setAccessible(true);
                    if (Modifier.isStatic(field.getModifiers()) && field.getType() == DamageSource.class) {
                        result.add(ProbeJS.GSON.toJson(((DamageSource) field.get(null)).getMsgId()));
                    }
                }
            } catch (Exception ignored) {
            }
            return result;
        });
        putSpecialAssignments(MaterialJS.class, () -> MaterialListJS.INSTANCE.map.keySet().stream().map(ProbeJS.GSON::toJson).collect(Collectors.toList()));

        SpecialTypes.assignRegistries();

        addKeyword("function");
        addKeyword("debugger");
        addKeyword("in");
        addKeyword("with");
        addKeyword("java");
        addKeyword("var");
        addKeyword("const");

        addPrioritizedPackage("java");
        addPrioritizedPackage("dev.latvian.mods");
        addPrioritizedPackage("net.minecraft");
        addPrioritizedPackage("net.minecraftforge");
    }
}
