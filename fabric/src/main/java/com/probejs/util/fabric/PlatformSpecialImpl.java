package com.probejs.util.fabric;

import com.faux.ingredientextension.api.ingredient.IngredientHelper;
import com.probejs.formatter.formatter.IFormatter;
import com.probejs.jdoc.document.DocumentClass;
import com.probejs.util.PlatformSpecial;
import dev.architectury.platform.Platform;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlatformSpecialImpl extends PlatformSpecial {

    @NotNull
    @Override
    public List<ResourceLocation> getIngredientTypes() {
        //Custom Ingredients are not supported by fabric?
        if (Platform.isModLoaded("ingredient-extension-api")) {
            return IngredientHelper.INGREDIENT_SERIALIZER_REGISTRY.keySet().stream().toList();
        }
        return List.of();
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public List<IFormatter> getPlatformFormatters() {
        return List.of();
    }

    @NotNull
    @Override
    public List<DocumentClass> getPlatformDocuments(List<DocumentClass> globalClasses) {
        return super.getPlatformDocuments(globalClasses);
    }
}
