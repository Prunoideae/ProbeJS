package com.probejs.compiler;

import com.probejs.ProbeCommands;
import com.probejs.formatter.formatter.*;
import com.probejs.formatter.formatter.special.*;
import com.probejs.util.PlatformSpecial;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SpecialCompiler {
    public static final List<IFormatter> specialCompilers = new ArrayList<>();

    public static String rl2Cap(ResourceLocation location) {
        String[] elements = location.getPath().split("/");
        return Arrays.stream(elements[elements.length - 1].split("_"))
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
                .collect(Collectors.joining(""));
    }

    private static List<FormatterTag> getTagFormatters() {
        List<FormatterTag> formatters = new ArrayList<>();
        ProbeCommands.COMMAND_LEVEL.registryAccess().registries().forEach(entry -> {
            ResourceKey<?> key = entry.key();
            Registry<?> registry = entry.value();
            formatters.add(new FormatterTag(rl2Cap(key.location()) + "Tag", registry));
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
        formatters.addAll(PlatformSpecial.INSTANCE.get().getPlatformFormatters());
        formatters.addAll(specialCompilers);
        return formatters;
    }


    public static List<String> compileTagEvents() {
        return List.of();
    }
}
