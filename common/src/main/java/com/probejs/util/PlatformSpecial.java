package com.probejs.util;


import com.google.common.base.Suppliers;
import com.probejs.ProbeJS;
import com.probejs.docs.formatter.NameResolver;
import com.probejs.docs.formatter.formatter.IFormatter;
import com.probejs.specials.special.FormatterRegistry;
import com.probejs.jdoc.document.DocumentClass;
import com.probejs.jdoc.document.DocumentMethod;
import com.probejs.jdoc.java.ClassInfo;
import com.probejs.jdoc.java.MethodInfo;
import com.probejs.jdoc.property.PropertyParam;
import com.probejs.jdoc.property.PropertyType;
import dev.latvian.mods.kubejs.bindings.JavaWrapper;
import dev.latvian.mods.kubejs.recipe.RecipesEventJS;
import dev.latvian.mods.kubejs.server.ServerScriptManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Supplier;

public abstract class PlatformSpecial {
    public static Supplier<PlatformSpecial> INSTANCE = Suppliers.memoize(() -> {
        var serviceLoader = ServiceLoader.load(PlatformSpecial.class);
        return serviceLoader.findFirst().orElseThrow(() -> new RuntimeException("Could not find platform implementation for PlatformSpecial!"));
    });

    @NotNull
    public abstract List<ResourceLocation> getIngredientTypes();

    @NotNull
    public abstract List<IFormatter> getPlatformFormatters();

    @NotNull
    public List<DocumentClass> getPlatformDocuments(List<DocumentClass> globalClasses) {
        ArrayList<DocumentClass> documents = new ArrayList<>();
        try {
            //Document for Java.loadClass
            DocumentClass javaWrapper = DocumentClass.fromJava(ClassInfo.getOrCache(JavaWrapper.class));
            DocumentMethod loadClass = DocumentMethod.fromJava(new MethodInfo(MethodInfo.getMethodInfo(JavaWrapper.class.getMethod("loadClass", String.class), JavaWrapper.class).get(), JavaWrapper.class));
            for (DocumentClass globalClass : globalClasses) {
                if (ServerScriptManager.getScriptManager().isClassAllowed(globalClass.getName())) {
                    DocumentMethod method = loadClass.copy();
                    method.params.set(0, new PropertyParam("className", new PropertyType.Native(ProbeJS.GSON.toJson(globalClass.getName())), false));
                    //Return interface directly since typeof Interface = any in Typescript
                    method.returns = globalClass.isInterface() ?
                            new PropertyType.Clazz(globalClass.getName()) :
                            new PropertyType.TypeOf(new PropertyType.Clazz(globalClass.getName()));
                    javaWrapper.methods.add(method);
                }
            }
            documents.add(javaWrapper);

            //Document for blending recipe events
            DocumentClass recipeEventJS = DocumentClass.fromJava(ClassInfo.getOrCache(RecipesEventJS.class));
            DocumentMethod getRecipes = DocumentMethod.fromJava(new MethodInfo(MethodInfo.getMethodInfo(RecipesEventJS.class.getMethod("getRecipes"), RecipesEventJS.class).get(), RecipesEventJS.class));
            getRecipes.returns = new PropertyType.Native("Special.DocumentedRecipes");
            recipeEventJS.methods.add(getRecipes);
            documents.add(recipeEventJS);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return documents;
    }

    protected final List<IFormatter> platformFormatters = new ArrayList<>();

    public void assignPlatformFormatter(IFormatter formatter) {
        platformFormatters.add(formatter);
    }

    protected static <T> IFormatter assignRegistry(Class<T> clazz, ResourceKey<Registry<T>> registry) {
        List<String> remappedName = Arrays.stream(MethodInfo.getRemappedOrOriginalClass(clazz).split("\\.")).toList();
        NameResolver.putSpecialAssignments(clazz, () -> List.of("Special.%s".formatted(remappedName.get(remappedName.size() - 1))));
        return new FormatterRegistry<>(registry);
    }

    public void preCompile() {

    }

    public abstract TextureAtlasSprite getFluidSprite(Fluid fluid);
}
