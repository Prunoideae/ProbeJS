package com.probejs.formatter.formatter;

import com.google.gson.Gson;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;
import java.util.stream.Collectors;

public class FormatterRecipeId implements IFormatter {
    public static MinecraftServer server = null;

    public static void onServerLoaded(MinecraftServer minecraftServer) {
        server = minecraftServer;
    }

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        if (server == null)
            return List.of();
        RecipeManager manager = server.getRecipeManager();
        Gson g = new Gson();
        return List.of("%stype RecipeId = %s;".formatted(" ".repeat(indent), manager.getRecipeIds()
                .filter(rl -> !rl.getPath().startsWith("kjs_")) //Filter out kjs-like recipes
                .map(ResourceLocation::toString)
                .map(g::toJson)
                .collect(Collectors.joining(" | "))));
    }
}
