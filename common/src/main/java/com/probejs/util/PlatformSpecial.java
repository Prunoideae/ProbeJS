package com.probejs.util;


import com.google.common.base.Suppliers;
import com.probejs.ProbeJS;
import com.probejs.formatter.NameResolver;
import com.probejs.formatter.formatter.IFormatter;
import com.probejs.formatter.formatter.special.FormatterRegistry;
import com.probejs.info.ClassInfo;
import com.probejs.info.MethodInfo;
import com.probejs.jdoc.document.DocumentClass;
import com.probejs.jdoc.document.DocumentMethod;
import com.probejs.jdoc.property.PropertyParam;
import com.probejs.jdoc.property.PropertyType;
import com.probejs.plugin.CapturedClasses;
import dev.latvian.mods.kubejs.bindings.JavaWrapper;
import dev.latvian.mods.kubejs.server.ServerScriptManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
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

    @Nonnull
    public List<DocumentClass> getPlatformDocuments(List<DocumentClass> globalClasses) {
        ArrayList<DocumentClass> documents = new ArrayList<>();
        try {
            //Document for Java.loadClass
            DocumentClass javaWrapper = DocumentClass.fromJava(ClassInfo.getOrCache(JavaWrapper.class));
            DocumentMethod loadClass = DocumentMethod.fromJava(new MethodInfo(JavaWrapper.class.getMethod("loadClass", String.class), DocumentMethod.class));
            for (DocumentClass globalClass : globalClasses) {
                if (ServerScriptManager.getScriptManager().isClassAllowed(globalClass.getName())) {
                    DocumentMethod method = loadClass.copy();
                    method.params.set(0, new PropertyParam("className", new PropertyType.Native(ProbeJS.GSON.toJson(globalClass.getName())), false));
                    method.returns = new PropertyType.TypeOf(new PropertyType.Clazz(globalClass.getName()));
                    javaWrapper.methods.add(method);
                }
            }
            documents.add(javaWrapper);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return documents;
    }

    private List<IFormatter> platformFormatters = new ArrayList<>();

    public void assignPlatformFormatter(IFormatter formatter) {
        platformFormatters.add(formatter);
    }

    protected static <T> IFormatter assignRegistry(Class<T> clazz, ResourceKey<Registry<T>> registry) {
        List<String> remappedName = Arrays.stream(MethodInfo.getRemappedOrOriginalClass(clazz).split("\\.")).toList();
        NameResolver.putSpecialAssignments(clazz, () -> List.of("Special.%s".formatted(remappedName.get(remappedName.size() - 1))));
        return new FormatterRegistry<>(registry, clazz);
    }
}
