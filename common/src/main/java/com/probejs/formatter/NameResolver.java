package com.probejs.formatter;

import com.probejs.ProbeJS;
import com.probejs.document.type.IType;
import com.probejs.document.type.TypeNamed;
import com.probejs.document.type.TypeParameterized;
import com.probejs.info.MethodInfo;
import com.probejs.info.type.ITypeInfo;
import dev.latvian.mods.kubejs.block.MaterialJS;
import dev.latvian.mods.kubejs.block.MaterialListJS;
import dev.latvian.mods.kubejs.item.ingredient.IngredientJS;
import dev.latvian.mods.kubejs.util.ClassWrapper;
import dev.latvian.mods.rhino.BaseFunction;
import dev.latvian.mods.rhino.NativeJavaObject;
import dev.latvian.mods.rhino.NativeObject;
import net.minecraft.MethodsReturnNonnullByDefault;
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
    public static final HashMap<Class<?>, List<IType>> specialExtension = new HashMap<>();
    public static final HashMap<Class<?>, Function<ITypeInfo, String>> specialTypeFormatters = new HashMap<>();
    public static final HashMap<Class<?>, Function<Object, String>> specialValueFormatters = new HashMap<>();
    public static final HashMap<Class<?>, Boolean> specialTypeGuards = new HashMap<>();
    public static final HashMap<Class<?>, Supplier<List<String>>> specialClassAssigner = new HashMap<>();
    public static final List<String> nameResolveSpecials = new ArrayList<>();
    public static final Set<String> keywords = new HashSet<>();
    public static final Set<String> resolvedPrimitives = new HashSet<>();

    public static void putResolvedName(String className, String resolvedName) {
        putResolvedName(className, new ResolvedName(Arrays.stream(resolvedName.split("\\.")).toList()));
    }

    //Put a resolved name to the class.
    public static void putResolvedName(String className, ResolvedName resolvedName) {
        resolvedNames.computeIfAbsent(className, s -> new ArrayList<>()).add(resolvedName);
    }

    public static void putResolvedName(Class<?> className, ResolvedName resolvedName) {
        String remappedName = MethodInfo.RUNTIME.getMappedClass(className);
        putResolvedName(remappedName, resolvedName);
    }

    public static void putResolvedName(Class<?> className, String resolvedName) {
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

    public static void putValueFormatter(Function<Object, String> transformer, Class<?>... classes) {
        for (Class<?> clazz : classes)
            specialValueFormatters.put(clazz, transformer);
    }

    public static String formatValue(Object object) {
        if (object == null)
            return "null";
        if (specialValueFormatters.containsKey(object.getClass()))
            return specialValueFormatters.get(object.getClass()).apply(object);
        for (Map.Entry<Class<?>, Function<Object, String>> entry : specialValueFormatters.entrySet()) {
            if (entry.getKey().isAssignableFrom(object.getClass()))
                return entry.getValue().apply(object);
        }
        return null;
    }

    public static boolean findResolvedName(ResolvedName name) {
        return resolvedNames.values().stream().anyMatch(names -> names.contains(name));
    }

    //Resolves a name.
    //Will skip if the name is already resolved.
    public static void resolveName(Class<?> clazz) {
        String remappedName = MethodInfo.RUNTIME.getMappedClass(clazz);
        if (resolvedNames.containsKey(remappedName))
            return;
        ResolvedName resolved = new ResolvedName(Arrays.stream(remappedName.split("\\.")).toList());
        ResolvedName internal = new ResolvedName(List.of("Internal", resolved.getLastName()));
        if (findResolvedName(internal))
            putResolvedName(remappedName, resolved);
        else {
            putResolvedName(remappedName, internal);
        }
    }

    public static void resolveNames(List<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            resolveName(clazz);
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
        specialClassAssigner.put(clazz, assigns);
    }

    public static List<String> getClassAssignments(Class<?> clazz) {
        return specialClassAssigner.getOrDefault(clazz, ArrayList::new).get();
    }

    public static void putSpecialExtension(Class<?> clazz, String base) {
        putSpecialExtension(clazz, new TypeNamed(base));
    }

    public static void putSpecialExtension(Class<?> clazz, IType type) {
        specialExtension.computeIfAbsent(clazz, c -> new ArrayList<>()).add(type);
    }

    public static void addPrioritizedPackage(String prior) {
        //Add a point to the end to prevent overlapping of package names
        //For example, net.minecraft and net.minecraftforge
        nameResolveSpecials.add(prior + ".");
    }

    public static List<Class<?>> priorSortClasses(Set<Class<?>> classes) {
        Map<Integer, List<Class<?>>> partMap = new HashMap<>();
        for (Class<?> clazz : classes) {
            int index = -1;
            String remappedName = MethodInfo.RUNTIME.getMappedClass(clazz);
            for (int i = 0; i < nameResolveSpecials.size(); i++) {
                if (remappedName.startsWith(nameResolveSpecials.get(i)))
                    index = i;
            }
            partMap.computeIfAbsent(index, i -> new ArrayList<>()).add(clazz);
        }
        List<Class<?>> result = new ArrayList<>();
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


        putValueFormatter(ProbeJS.GSON::toJson,
                String.class, Character.class, Character.TYPE,
                Long.class, Long.TYPE, Integer.class, Integer.TYPE,
                Short.class, Short.TYPE, Byte.class, Byte.TYPE,
                Double.class, Double.TYPE, Float.class, Float.TYPE,
                Boolean.class, Boolean.TYPE);
        putValueFormatter(SpecialTypes::formatMap, Map.class);
        putValueFormatter(SpecialTypes::formatList, List.class);
        putValueFormatter(SpecialTypes::formatScriptable, NativeObject.class);
        putValueFormatter(SpecialTypes::formatFunction, BaseFunction.class);
        putValueFormatter(SpecialTypes::formatNJO, NativeJavaObject.class);
        //putValueFormatter(SpecialTypes::formatScriptable, Scriptable.class);

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
        putTypeFormatter(Class.class, SpecialTypes::formatClassLike);
        putTypeFormatter(ClassWrapper.class, SpecialTypes::formatClassLike);
        putTypeGuard(true, Class.class, ClassWrapper.class);
        putTypeGuard(false, IngredientJS.class);

        putSpecialExtension(List.class, new TypeParameterized(new TypeNamed("TSDoc.JSArray"), List.of(new TypeNamed("E"))));
        putSpecialExtension(List.class, new TypeParameterized(new TypeNamed(Collection.class.getName()), List.of(new TypeNamed("E"))));
        putSpecialExtension(Map.class, new TypeParameterized(new TypeNamed("TSDoc.JSMap"), List.of(new TypeNamed("K"), new TypeNamed("V"))));

        addKeyword("function");
        addKeyword("debugger");
        addKeyword("in");
        addKeyword("with");
        addKeyword("java");
        addKeyword("var");
        addKeyword("const");

        addPrioritizedPackage("java");
        addPrioritizedPackage("net.minecraft");
        addPrioritizedPackage("net.minecraftforge");
        addPrioritizedPackage("dev.latvian.mods");
    }
}
