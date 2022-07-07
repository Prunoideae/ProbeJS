package com.probejs.util;


import com.google.common.base.Suppliers;
import com.probejs.formatter.NameResolver;
import com.probejs.formatter.formatter.IFormatter;
import com.probejs.formatter.formatter.special.FormatterRegistry;
import com.probejs.info.MethodInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class PlatformSpecial {
    public static Supplier<PlatformSpecial> INSTANCE = Suppliers.memoize(() -> {
        var serviceLoader = ServiceLoader.load(PlatformSpecial.class);
        return serviceLoader.findFirst().orElseThrow(() -> new RuntimeException("Could not find platform implementation for PlatformSpecial!"));
    });

    @Nonnull
    public abstract List<ResourceLocation> getIngredientTypes();

    @Nonnull
    public abstract List<IFormatter> getPlatformFormatters();

    protected static <T> IFormatter assignRegistry(Class<T> clazz, ResourceKey<Registry<T>> registry) {
        List<String> remappedName = Arrays.stream(MethodInfo.RUNTIME.getMappedClass(clazz).split("\\.")).collect(Collectors.toList());
        NameResolver.putSpecialAssignments(clazz, () -> List.of("Special.%s".formatted(remappedName.get(remappedName.size() - 1))));
        return new FormatterRegistry<>(registry, clazz);
    }
}
