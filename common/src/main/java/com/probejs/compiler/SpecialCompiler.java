package com.probejs.compiler;

import com.probejs.ProbeCommands;
import com.probejs.compiler.formatter.formatter.IFormatter;
import com.probejs.compiler.formatter.formatter.special.*;
import com.probejs.recipe.FormatterRecipe;
import com.probejs.recipe.component.FormatterRecipeSchema;
import com.probejs.util.PlatformSpecial;
import com.probejs.util.RLHelper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.List;

public class SpecialCompiler {
    public static final List<IFormatter> specialCompilers = new ArrayList<>();

    private static List<FormatterTag> getTagFormatters() {
        List<FormatterTag> formatters = new ArrayList<>();
        ProbeCommands.COMMAND_LEVEL.registryAccess().registries().forEach(entry -> {
            ResourceKey<?> key = entry.key();
            Registry<?> registry = entry.value();
            formatters.add(new FormatterTag(RLHelper.finalComponentToTitle(key.location().getPath()) + "Tag", registry));
        });
        return formatters;
    }

    public static List<IFormatter> compileSpecial() {
        List<IFormatter> formatters = new ArrayList<>();
        formatters.add(new FormatterMod());
        formatters.add(new FormatterIngredient());
        formatters.add(new FormatterAdvancement());
        formatters.add(new FormatterRecipeId());
        formatters.add(new FormatterLang());
        formatters.add(new FormatterLootTable());
        formatters.add(new FormatterTexture());
        formatters.add(new FormatterModel());
        formatters.addAll(getTagFormatters());
        formatters.add(FormatterRecipe.formatRecipeNamespaces());
        formatters.add(FormatterRecipeSchema.formatRecipeClasses());
        formatters.add((i, d) -> List.of("%stype ArrayOrSelf<T> = T[] | T".formatted(" ".repeat(i))));
        formatters.addAll(PlatformSpecial.INSTANCE.get().getPlatformFormatters());
        formatters.addAll(specialCompilers);
        return formatters;
    }


    public static List<String> compileTagEvents() {
        return List.of();
    }
}
