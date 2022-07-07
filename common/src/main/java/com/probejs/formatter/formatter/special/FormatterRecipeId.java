package com.probejs.formatter.formatter.special;

import com.probejs.ProbeJS;
import com.probejs.formatter.formatter.IFormatter;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.stream.Collectors;

public class FormatterRecipeId implements IFormatter {
    public static List<ResourceLocation> originalIds = null;

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        if (originalIds == null)
            return List.of();
        return List.of("%stype RecipeId = %s;".formatted(" ".repeat(indent), originalIds.stream()
                .filter(rl -> !rl.getPath().startsWith("kjs_")) //Filter out kjs-like recipes
                .map(ResourceLocation::toString)
                .map(ProbeJS.GSON::toJson)
                .collect(Collectors.joining(" | "))));
    }
}
