package com.probejs.compiler;

import com.probejs.formatter.formatter.*;
import net.minecraft.core.Registry;

import java.util.ArrayList;
import java.util.List;

public class SpecialCompiler {
    public static final List<IFormatter> specialCompilers = new ArrayList<>();

    public static List<IFormatter> compileSpecial() {
        List<IFormatter> formatters = new ArrayList<>();
        formatters.add(new FormatterTag("ItemTag", Registry.ITEM));
        formatters.add(new FormatterTag("FluidTag", Registry.FLUID));
        formatters.add(new FormatterTag("BlockTag", Registry.BLOCK));
        formatters.add(new FormatterTag("EntityTypeTag", Registry.ENTITY_TYPE));
        formatters.add(new FormatterMod());
        formatters.add(new FormatterIngredient());
        formatters.add(new FormatterRecipeId());
        formatters.addAll(specialCompilers);
        return formatters;
    }
}
