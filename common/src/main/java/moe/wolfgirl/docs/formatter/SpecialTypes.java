package moe.wolfgirl.docs.formatter;

import moe.wolfgirl.ProbeCommands;
import moe.wolfgirl.ProbeJS;
import moe.wolfgirl.specials.SpecialCompiler;
import moe.wolfgirl.specials.special.FormatterRegistry;
import moe.wolfgirl.util.RLHelper;
import dev.latvian.mods.rhino.util.EnumTypeWrapper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.*;
import java.util.stream.Collectors;

public class SpecialTypes {
    public static Map<Class<?>, ResourceKey<?>> registryAssignments = new HashMap<>();

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

    public static <T> void assignRegistry(Class<T> clazz, ResourceKey<Registry<T>> resourceKey) {
        SpecialCompiler.specialCompilers.add(new FormatterRegistry<>(resourceKey));
        NameResolver.putSpecialAssignments(clazz, () -> List.of("Special.%s".formatted(RLHelper.finalComponentToTitle(resourceKey.location().getPath()))));
        registryAssignments.put(clazz, resourceKey);
    }

    public static String getRegistryTagName(Class<?> clazz) {
        // We here assume that people use the TagKeyComponent, thus adding # to the beginning of the Special.XXXTag by default
        var key = registryAssignments.get(clazz);
        if (key == null)
            return null;
        return "`#${Special.%sTag}`".formatted(RLHelper.finalComponentToTitle(key.location().getPath()));
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
            ResourceKey<? extends Registry<?>> key = entry.key();
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
        return classes.stream().limit(32768).collect(Collectors.toList());
    }
}
