package com.probejs.formatter.formatter;

import com.google.gson.Gson;
import dev.latvian.mods.kubejs.KubeJSRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.List;

public class FormatterRegistry<T> implements IFormatter {
    private final ResourceKey<Registry<T>> registry;
    private final Class<T> clazz;

    public FormatterRegistry(ResourceKey<Registry<T>> registry, Class<T> clazz) {
        this.registry = registry;
        this.clazz = clazz;
    }

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        Gson g = new Gson();
        List<String> items = new ArrayList<>();
        KubeJSRegistries.genericRegistry(registry).getIds().forEach(rl -> {
            if (rl.getNamespace().equals("minecraft"))
                items.add(g.toJson(rl.getPath()));
            items.add(g.toJson(rl.toString()));
        });
        return List.of("%stype %s = %s;".formatted(" ".repeat(indent), clazz.getSimpleName(), String.join(" | ", items)));
    }
}
