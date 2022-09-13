package com.probejs.util.forge;

import com.google.common.collect.BiMap;
import com.probejs.formatter.SpecialTypes;
import com.probejs.formatter.formatter.IFormatter;
import com.probejs.util.PlatformSpecial;
import dev.architectury.platform.Platform;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.brew.Brew;
import vazkii.botania.common.brew.ModBrews;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class PlatformSpecialImpl extends PlatformSpecial {
    private static Field ingredientInst = null;
    private boolean inited = false;

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
        List<IFormatter> formatters = new ArrayList<>();
        if (!inited) {
            if (Platform.isModLoaded("kubejs_mekanism")) {
                SpecialTypes.assignRegistry(Gas.class, MekanismAPI.gasRegistry().getRegistryKey());
                SpecialTypes.assignRegistry(Slurry.class, MekanismAPI.slurryRegistry().getRegistryKey());
                SpecialTypes.assignRegistry(InfuseType.class, MekanismAPI.infuseTypeRegistry().getRegistryKey());
                SpecialTypes.assignRegistry(Pigment.class, MekanismAPI.pigmentRegistry().getRegistryKey());
            }
            if (Platform.isModLoaded("kubejs_botania")) {
                SpecialTypes.assignRegistry(Brew.class, (ResourceKey<Registry<Brew>>) ModBrews.registry.key());
            }
            inited = true;
        }
        return formatters;
    }
}
