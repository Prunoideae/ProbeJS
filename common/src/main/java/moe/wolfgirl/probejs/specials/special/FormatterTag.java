package moe.wolfgirl.probejs.specials.special;

import moe.wolfgirl.probejs.ProbeConfig;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.docs.formatter.formatter.IFormatter;
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
        String tags = this.registry.getTagNames()
                .map(TagKey::location)
                .map(ResourceLocation::toString)
                .map(ProbeJS.GSON::toJson)
                .collect(Collectors.joining(" | "));
        // Disable literal dumps if there are too many mods.
        if (tags.isEmpty() || !ProbeConfig.INSTANCE.allowRegistryLiteralDumps)
            tags = "string";
        return List.of("%stype %s = %s;".formatted(
                " ".repeat(indent),
                this.name,
                tags));
    }

    public static List<String> getTagsFromRegistry(Registry<?> registry) {
        return registry.getTagNames().map(TagKey::location).map(ResourceLocation::toString).collect(Collectors.toList());
    }
}
