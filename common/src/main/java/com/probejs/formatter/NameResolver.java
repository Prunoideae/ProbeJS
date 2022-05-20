package com.probejs.formatter;

import com.google.gson.Gson;
import com.probejs.info.MethodInfo;
import com.probejs.info.type.ITypeInfo;
import dev.latvian.mods.rhino.BaseFunction;
import dev.latvian.mods.rhino.NativeJavaObject;
import dev.latvian.mods.rhino.NativeObject;
import net.minecraft.core.Registry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

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

    public static final HashMap<String, ResolvedName> resolvedNames = new HashMap<>();
    public static final HashMap<Class<?>, Function<ITypeInfo, String>> specialTypeFormatters = new HashMap<>();
    public static final HashMap<Class<?>, Function<Object, String>> specialValueFormatters = new HashMap<>();
    public static final HashMap<Class<?>, Supplier<List<String>>> specialClassAssigner = new HashMap<>();

    public static final Set<String> keywords = new HashSet<>();
    public static final Set<String> resolvedPrimitives = new HashSet<>();

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

    public static void resolveName(Class<?> clazz) {
        String remappedName = MethodInfo.RUNTIME.getMappedClass(clazz);
        ResolvedName resolved = new ResolvedName(Arrays.stream(remappedName.split("\\.")).toList());
        ResolvedName internal = new ResolvedName(List.of("Internal", resolved.getLastName()));
        if (resolvedNames.containsValue(internal))
            putResolvedName(clazz.getName(), resolved);
        else {
            putResolvedName(clazz.getName(), internal);
        }
    }

    public static void resolveNames(Set<Class<?>> classes) {
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

    private static boolean initialized = false;

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

        Gson gson = new Gson();

        putValueFormatter(gson::toJson,
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
                        result.add(((DamageSource) field.get(null)).getMsgId());
                    }
                }
            } catch (Exception ignored) {
            }
            return result;
        });

        SpecialTypes.assignRegistry(Attribute.class, Registry.ATTRIBUTE_REGISTRY);
        SpecialTypes.assignRegistry(MobEffect.class, Registry.MOB_EFFECT_REGISTRY);
        SpecialTypes.assignRegistry(Block.class, Registry.BLOCK_REGISTRY);
        SpecialTypes.assignRegistry(Item.class, Registry.ITEM_REGISTRY);
        SpecialTypes.assignRegistry(SoundEvent.class, Registry.SOUND_EVENT_REGISTRY);
        SpecialTypes.assignRegistry(Fluid.class, Registry.FLUID_REGISTRY);
        SpecialTypes.assignRegistry(Biome.class, Registry.BIOME_REGISTRY);

        addKeyword("function");
        addKeyword("debugger");
        addKeyword("in");
        addKeyword("with");
        addKeyword("java");

    }
}
