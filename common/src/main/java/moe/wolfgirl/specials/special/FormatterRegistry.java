package moe.wolfgirl.specials.special;

import moe.wolfgirl.ProbeCommands;
import moe.wolfgirl.ProbeConfig;
import moe.wolfgirl.ProbeJS;
import moe.wolfgirl.docs.formatter.formatter.IFormatter;
import moe.wolfgirl.util.RLHelper;
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
        ProbeCommands.getRegistry(registry).keySet().forEach(rl -> {
            if (rl.getNamespace().equals("minecraft"))
                items.add(ProbeJS.GSON.toJson(rl.getPath()));
            items.add(ProbeJS.GSON.toJson(rl.toString()));
        });
        String joined = String.join(" | ", items);
        // Disable literal dumps if there are too many items
        if (items.isEmpty() || !ProbeConfig.INSTANCE.allowRegistryLiteralDumps)
            joined = "string";
        return List.of("%stype %s = %s;".formatted(" ".repeat(indent), typeName, joined));
    }
}
