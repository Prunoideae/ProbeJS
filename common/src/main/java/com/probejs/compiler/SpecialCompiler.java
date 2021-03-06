package com.probejs.compiler;

import com.probejs.formatter.formatter.*;
import com.probejs.formatter.formatter.special.FormatterIngredient;
import com.probejs.formatter.formatter.special.FormatterMod;
import com.probejs.formatter.formatter.special.FormatterRecipeId;
import com.probejs.formatter.formatter.special.FormatterTag;
import com.probejs.util.PlatformSpecial;
import net.minecraft.core.Registry;

import java.util.ArrayList;
import java.util.List;

public class SpecialCompiler {
    public static boolean specialInitialized = false;
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
        formatters.addAll(PlatformSpecial.INSTANCE.get().getPlatformFormatters());
        formatters.addAll(specialCompilers);
        return formatters;
    }
}
