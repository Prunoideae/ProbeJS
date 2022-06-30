package com.probejs.formatter.formatter;

import com.google.gson.Gson;
import com.probejs.util.PlatformSpecial;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.stream.Collectors;

public class FormatterIngredient implements IFormatter {
    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        Gson g = new Gson();
        List<ResourceLocation> ingredients = PlatformSpecial.INSTANCE.get().getIngredientTypes();
        if (ingredients.isEmpty())
            return List.of("%stype Ingredient = string;");
        return List.of("%stype Ingredient = %s;".formatted(" ".repeat(indent),
                ingredients
                        .stream()
                        .map(ResourceLocation::toString)
                        .map(g::toJson)
                        .collect(Collectors.joining(" | ")))
        );
    }
}
