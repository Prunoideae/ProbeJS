package com.probejs.compiler.formatter.formatter.special;

import com.google.gson.JsonObject;
import com.probejs.ProbeJS;
import com.probejs.compiler.formatter.formatter.IFormatter;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FormatterRecipeId implements IFormatter {
    public static Map<ResourceLocation, JsonObject> ORIGINAL_RECIPES = null;

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        if (ORIGINAL_RECIPES == null)
            return List.of();

        return List.of("%stype RecipeId = %s;".formatted(" ".repeat(indent), ORIGINAL_RECIPES.keySet().stream()
                .filter(rl -> !rl.getPath().startsWith("kjs_")) //Filter out kjs-like recipes
                .map(ResourceLocation::toString)
                .map(ProbeJS.GSON::toJson)
                .collect(Collectors.joining(" | "))));
    }
}
