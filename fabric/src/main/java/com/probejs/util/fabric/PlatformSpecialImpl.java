package com.probejs.util.fabric;

import com.probejs.formatter.SpecialTypes;
import com.probejs.formatter.formatter.IFormatter;
import com.probejs.util.PlatformSpecial;
import dev.architectury.platform.Platform;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.brew.Brew;
import vazkii.botania.common.brew.ModBrews;

import java.util.List;

public class PlatformSpecialImpl extends PlatformSpecial {
    private boolean inited = false;

    @NotNull
    @Override
    public List<ResourceLocation> getIngredientTypes() {
        //Custom Ingredients are not supported by fabric.
        return List.of();
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public List<IFormatter> getPlatformFormatters() {
        if (!inited) {
            inited = true;
        }
        return List.of();
    }
}
