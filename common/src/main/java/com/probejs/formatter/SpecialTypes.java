package com.probejs.formatter;

import com.mojang.serialization.Codec;
import com.probejs.ProbeJS;
import com.probejs.compiler.SpecialCompiler;
import com.probejs.formatter.formatter.IFormatter;
import com.probejs.formatter.formatter.jdoc.FormatterClass;
import com.probejs.formatter.formatter.jdoc.FormatterMethod;
import com.probejs.formatter.formatter.special.FormatterRegistry;
import com.probejs.info.ClassInfo;
import com.probejs.info.MethodInfo;
import com.probejs.info.type.ITypeInfo;
import com.probejs.info.type.InfoTypeResolver;
import com.probejs.jdoc.Serde;
import com.probejs.jdoc.document.DocumentMethod;
import com.probejs.util.Pair;
import dev.latvian.mods.rhino.util.EnumTypeWrapper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

public class SpecialTypes {

    public static Set<Class<?>> skippedSpecials = new HashSet<>();

    private static boolean isFunctionalInterface(Class<?> clazz) {
        if (clazz.isAnnotationPresent(FunctionalInterface.class)) {
            return true;
        }
        for (Class<?> superInterface : clazz.getInterfaces()) {
            if (isFunctionalInterface(superInterface))
                return true;
        }
        return false;
    }

    public static void processFunctionalInterfaces(Set<Class<?>> globalClasses) {
        for (Class<?> clazz : globalClasses) {
            //For some very random reason, people think that anything extending a FunctionInterface is also a FunctionalInterface
            if (clazz.isInterface() && !skippedSpecials.contains(clazz) && isFunctionalInterface(clazz)) {
                //...But a FunctionalInterfaces must only have one and only one abstract method
                ClassInfo info = ClassInfo.getOrCache(clazz);
                if (info.getMethodInfo().stream().filter(MethodInfo::isAbstract).count() != 1)
                    continue;
                FormatterClass.SPECIAL_FORMATTER_REGISTRY.put(info.getName(), (document) -> (indent, stepIndent) -> {
                    Optional<DocumentMethod> documentMethod = document.getMethods().stream().filter(DocumentMethod::isAbstract).map(DocumentMethod::applyProperties).findFirst();
                    if (documentMethod.isEmpty())
                        return List.of("any");
                    return List.of("((%s)=>%s)".formatted(
                            documentMethod.get().getParams()
                                    .stream()
                                    .map(FormatterMethod.FormatterParam::new)
                                    .map(IFormatter::formatFirst)
                                    .collect(Collectors.joining(", ")),
                            Serde.getTypeFormatter(documentMethod.get().getReturns()).underscored().formatFirst()));
                });
            }
        }
    }

    public static void processEnums(Set<Class<?>> globalClasses) {
        for (Class<?> clazz : globalClasses) {
            if (clazz.isEnum()) {
                EnumTypeWrapper<?> wrapper = EnumTypeWrapper.get(clazz);
                NameResolver.putSpecialAssignments(clazz, () -> wrapper.nameValues.keySet().stream().map(ProbeJS.GSON::toJson).collect(Collectors.toList()));
            }
        }
    }

    public static <T> void assignRegistry(Class<T> clazz, ResourceKey<Registry<T>> registry) {
        SpecialCompiler.specialCompilers.add(new FormatterRegistry<>(registry, clazz));
        List<String> remappedName = Arrays.stream(MethodInfo.getRemappedOrOriginalClass(clazz).split("\\.")).toList();
        NameResolver.putSpecialAssignments(clazz, () -> List.of("Special.%s".formatted(remappedName.get(remappedName.size() - 1))));
    }

    @SuppressWarnings("unchecked")
    public static <T> void assignRegistries(Class<?> registryClazz) {
        for (var field : registryClazz.getFields()) {
            if (field.getType() == ResourceKey.class && Modifier.isStatic(field.getModifiers())) {
                try {
                    ResourceKey<Registry<T>> key = (ResourceKey<Registry<T>>) field.get(null);
                    var type = field.getGenericType();
                    var type1 = ((ParameterizedType) type).getActualTypeArguments()[0];
                    var type2 = ((ParameterizedType) type1).getActualTypeArguments()[0];
                    ITypeInfo typeInfo = InfoTypeResolver.resolveType(type2);
                    if (typeInfo == null)
                        continue;
                    Class<T> clazz = (Class<T>) typeInfo.getResolvedClass();
                    if (clazz == ResourceLocation.class || clazz == ResourceKey.class || clazz == Codec.class)
                        continue;
                    assignRegistry(clazz, key);
                } catch (IllegalAccessException | IllegalArgumentException e) {
                    ProbeJS.LOGGER.error("Can not touch field: %s of %s".formatted(field.getName(), field.getDeclaringClass()));
                }
            }
        }
    }
}
