package com.probejs.compiler.formatter.formatter.special;

import com.probejs.ProbeJS;
import com.probejs.compiler.formatter.formatter.IFormatter;
import com.probejs.util.RLHelper;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.List;

public class FormatterRegistry<T> implements IFormatter {
    private final ResourceKey<Registry<T>> registry;

    public FormatterRegistry(ResourceKey<Registry<T>> registry) {
        this.registry = registry;
    }

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        List<String> items = new ArrayList<>();
        String typeName = RLHelper.finalComponentToTitle(registry.location().getPath());
        RegistryInfo.of(registry).getVanillaRegistry().keySet().forEach(rl -> {
            if (rl.getNamespace().equals("minecraft"))
                items.add(ProbeJS.GSON.toJson(rl.getPath()));
            items.add(ProbeJS.GSON.toJson(rl.toString()));
        });
        if (items.isEmpty())
            items.add("never");
        return List.of("%stype %s = %s;".formatted(" ".repeat(indent), typeName, String.join(" | ", items)));
    }
}
