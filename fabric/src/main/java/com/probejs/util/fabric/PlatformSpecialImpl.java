package com.probejs.util.fabric;

import com.probejs.formatter.formatter.IFormatter;
import com.probejs.util.PlatformSpecial;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlatformSpecialImpl extends PlatformSpecial {
    @NotNull
    @Override
    public List<ResourceLocation> getIngredientTypes() {
        //Custom Ingredients are not supported by fabric.
        return List.of();
    }

    @NotNull
    @Override
    public List<IFormatter> getPlatformFormatters() {
        return List.of();
    }
}
