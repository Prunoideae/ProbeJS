package com.probejs.formatter.formatter;

import com.google.gson.Gson;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.recipe.RecipeEventJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;
import java.util.stream.Collectors;

public class FormatterRecipeId implements IFormatter {
    public static List<ResourceLocation> originalIds = null;

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        if (originalIds == null)
            return List.of();
        Gson g = new Gson();
        return List.of("%stype RecipeId = %s;".formatted(" ".repeat(indent), originalIds.stream()
                .filter(rl -> !rl.getPath().startsWith("kjs_")) //Filter out kjs-like recipes
                .map(ResourceLocation::toString)
                .map(g::toJson)
                .collect(Collectors.joining(" | "))));
    }
}
