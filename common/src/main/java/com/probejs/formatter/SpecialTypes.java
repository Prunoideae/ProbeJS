package com.probejs.formatter;

import com.probejs.ProbeCommands;
import com.probejs.ProbeJS;
import com.probejs.compiler.SpecialCompiler;
import com.probejs.formatter.formatter.IFormatter;
import com.probejs.formatter.formatter.jdoc.FormatterClass;
import com.probejs.formatter.formatter.jdoc.FormatterMethod;
import com.probejs.formatter.formatter.special.FormatterRegistry;
import com.probejs.info.ClassInfo;
import com.probejs.info.MethodInfo;
import com.probejs.jdoc.Serde;
import com.probejs.jdoc.document.DocumentMethod;
import dev.latvian.mods.rhino.util.EnumTypeWrapper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

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
                            Serde.getTypeFormatter(documentMethod.get().getReturns()).underscored().formatParamVariable()));
                });
            }
        }
    }

    public static void processEnums(Set<Class<?>> globalClasses) {
        for (Class<?> clazz : globalClasses) {
            if (clazz.isEnum()) {
                try {
                    EnumTypeWrapper<?> wrapper = EnumTypeWrapper.get(clazz);
                    NameResolver.putSpecialAssignments(clazz, () -> wrapper.nameValues.keySet().stream().map(ProbeJS.GSON::toJson).collect(Collectors.toList()));
                } catch (Throwable e) {
                    ProbeJS.LOGGER.warn("Failed to process enum: %s".formatted(clazz.getName()));
                }
            }
        }
    }

    public static <T> void assignRegistry(Class<T> clazz, ResourceKey<Registry<T>> registry) {
        SpecialCompiler.specialCompilers.add(new FormatterRegistry<>(registry, clazz));
        List<String> remappedName = Arrays.stream(MethodInfo.getRemappedOrOriginalClass(clazz).split("\\.")).toList();
        NameResolver.putSpecialAssignments(clazz, () -> List.of("Special.%s".formatted(remappedName.get(remappedName.size() - 1))));
    }

    private static List<Class<?>> getParentInterfaces(List<Class<?>> putative, Class<?> o) {
        List<Class<?>> result = new ArrayList<>();
        for (Class<?> clazz : putative) {
            if (!clazz.isAssignableFrom(o)) {
                result.addAll(getParentInterfaces(List.of(clazz.getInterfaces()), o));
            } else {
                result.add(clazz);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> void assignRegistries() {
        ProbeCommands.COMMAND_LEVEL.registryAccess().registries().forEach(entry -> {
            //We know that, all objects in Registry<T> must extend or implement T
            //So T must be the superclass or superinterface of all object in Registry<T>
            //And it must not be synthetic class unless some people are really crazy
            ResourceKey<?> key = entry.key();
            Registry<?> registry = entry.value();
            Class<?> putativeParent = null;
            //We assume it's class based first
            for (Object o : registry) {
                if (putativeParent == null) {
                    putativeParent = o.getClass();
                    continue;
                }
                while (!putativeParent.isAssignableFrom(o.getClass())) {
                    putativeParent = putativeParent.getSuperclass();
                }
            }
            if (putativeParent == null) //No object present in registry, can only ignore
                return;

            while (putativeParent.isSynthetic()) //Wipe up the synthetic ass
                putativeParent = putativeParent.getSuperclass();

            //If result is object, probably it's using interface to register things
            if (putativeParent == Object.class) {
                List<Class<?>> putativeInterfaces = new ArrayList<>();
                for (Object o : registry) {
                    if (putativeInterfaces.isEmpty()) {
                        putativeInterfaces.addAll(List.of(o.getClass().getInterfaces()));
                        continue;
                    }
                    putativeInterfaces = getParentInterfaces(putativeInterfaces, o.getClass());
                }
                if (!putativeInterfaces.isEmpty())
                    putativeParent = putativeInterfaces.get(0);
            }
            assignRegistry((Class<T>) putativeParent, (ResourceKey<Registry<T>>) key);
        });
    }

    public static List<Class<?>> collectRegistryClasses() {
        //Collects classes in registry, so we can touch some more things.
        //Like furnace block entity class
        List<Class<?>> classes = new ArrayList<>();
        ProbeCommands.COMMAND_LEVEL.registryAccess().registries().forEach(entry -> {
            Registry<?> registry = entry.value();
            for (Object o : registry) {
                Class<?> clazz = o.getClass();
                if (!clazz.isSynthetic())
                    classes.add(o.getClass());
            }

        });
        return classes.stream().limit(16384).collect(Collectors.toList());
    }
}
