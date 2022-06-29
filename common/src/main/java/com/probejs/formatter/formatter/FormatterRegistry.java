package com.probejs.formatter.formatter;

import com.google.gson.Gson;
import com.probejs.info.MethodInfo;
import dev.latvian.mods.kubejs.KubeJSRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        List<String> remappedName = Arrays.stream(MethodInfo.RUNTIME.getMappedClass(clazz).split("\\.")).collect(Collectors.toList());
        return List.of("%stype %s = %s;".formatted(" ".repeat(indent), remappedName.get(remappedName.size() - 1), String.join(" | ", items)));
    }
}
