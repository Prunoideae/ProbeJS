package com.probejs.formatter.formatter;

import com.google.gson.Gson;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.List;
import java.util.stream.Collectors;

public class FormatterTag implements IFormatter {
    private final Registry<?> registry;
    private final String name;

    public FormatterTag(String name, Registry<?> registry) {
        this.name = name;
        this.registry = registry;
    }

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        Gson g = new Gson();
        return List.of("%stype %s = %s;".formatted(
                " ".repeat(indent),
                this.name,
                this.registry.getTagNames()
                        .map(TagKey::location)
                        .map(ResourceLocation::toString)
                        .map(g::toJson)
                        .collect(Collectors.joining(" | "))));
    }
}
