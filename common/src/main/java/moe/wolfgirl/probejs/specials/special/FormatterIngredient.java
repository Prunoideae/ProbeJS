package moe.wolfgirl.probejs.specials.special;

import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.docs.formatter.formatter.IFormatter;
import moe.wolfgirl.probejs.util.PlatformSpecial;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.stream.Collectors;

public class FormatterIngredient implements IFormatter {
    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        List<ResourceLocation> ingredients = PlatformSpecial.INSTANCE.get().getIngredientTypes();
        if (ingredients.isEmpty())
            return List.of("%stype Ingredient = string;".formatted(" ".repeat(indent)));
        return List.of("%stype Ingredient = %s;".formatted(" ".repeat(indent),
                ingredients
                        .stream()
                        .map(ResourceLocation::toString)
                        .map(ProbeJS.GSON::toJson)
                        .collect(Collectors.joining(" | ")))
        );
    }
}
