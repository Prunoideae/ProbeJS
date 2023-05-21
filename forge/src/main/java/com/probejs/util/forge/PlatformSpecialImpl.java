package com.probejs.util.forge;

import com.google.common.collect.BiMap;
import com.probejs.compiler.formatter.formatter.IFormatter;
import com.probejs.jdoc.document.DocumentClass;
import com.probejs.util.PlatformSpecial;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;

public class PlatformSpecialImpl extends PlatformSpecial {
    private static Field ingredientInst = null;

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public List<ResourceLocation> getIngredientTypes() {
        if (ingredientInst == null) {
            Field ingredients;
            try {
                ingredients = CraftingHelper.class.getDeclaredField("ingredients");
                ingredients.setAccessible(true);
            } catch (NoSuchFieldException e) {
                return List.of();
            }
            ingredientInst = ingredients;
        }

        try {
            BiMap<ResourceLocation, IIngredientSerializer<?>> ingredientValue = (BiMap<ResourceLocation, IIngredientSerializer<?>>) ingredientInst.get(null);
            return ingredientValue.keySet().stream().toList();
        } catch (IllegalAccessException e) {
            return List.of();
        }
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
        List<DocumentClass> superDocuments = super.getPlatformDocuments(globalClasses);
        try {
            superDocuments.add(ForgeEventDocument.loadForgeEventDocument());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return superDocuments;
    }
}
