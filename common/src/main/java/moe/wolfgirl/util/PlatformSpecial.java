package moe.wolfgirl.util;


import com.google.common.base.Suppliers;
import moe.wolfgirl.docs.formatter.NameResolver;
import moe.wolfgirl.docs.formatter.formatter.IFormatter;
import moe.wolfgirl.jdoc.document.DocumentClass;
import moe.wolfgirl.jdoc.document.DocumentMethod;
import moe.wolfgirl.jdoc.java.MethodInfo;
import moe.wolfgirl.specials.special.FormatterRegistry;
import moe.wolfgirl.util.special_docs.BlockEntityInfoDocument;
import moe.wolfgirl.util.special_docs.JavaWrapperDocument;
import moe.wolfgirl.util.special_docs.RecipeEventDocument;
import moe.wolfgirl.util.special_docs.SchemaRegistryEventJSDocument;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
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

    public static DocumentMethod getMethodDocument(Class<?> clazz, String name, Class<?>... paramTypes) throws NoSuchMethodException {
        return DocumentMethod.fromJava(new MethodInfo(MethodInfo.getMethodInfo(clazz.getMethod(name, paramTypes), clazz).get(), clazz));
    }


    @NotNull
    public abstract List<ResourceLocation> getIngredientTypes();

    @NotNull
    public abstract List<IFormatter> getPlatformFormatters();

    @NotNull
    public List<DocumentClass> getPlatformDocuments(List<DocumentClass> globalClasses) {
        ArrayList<DocumentClass> documents = new ArrayList<>();
        try {
            // TODO: Make it dynamic... But I wonder who will use them
            // probably rewrite in 1.20.4 or higher
            documents.add(JavaWrapperDocument.loadJavaWrapperDocument(globalClasses));
            documents.add(RecipeEventDocument.loadRecipeEventDocument());
            BlockEntityInfoDocument.loadBlockEntityInfoDocument(globalClasses);
            SchemaRegistryEventJSDocument.loadRecipeSchemaRegistryEvent(globalClasses);
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

    public List<File> getModFiles(){
        return null;
    }
}
