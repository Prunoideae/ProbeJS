package moe.wolfgirl.specials.special;

import moe.wolfgirl.ProbeJS;
import moe.wolfgirl.docs.formatter.formatter.IFormatter;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FormatterLootTable implements IFormatter {
    public static final Set<ResourceLocation> LOOT_TABLES = new HashSet<>();

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        return List.of("%stype LootTable = %s".formatted(" ".repeat(indent),
                LOOT_TABLES
                        .stream()
                        .map(ResourceLocation::toString)
                        .map(ProbeJS.GSON::toJson)
                        .collect(Collectors.joining(" | "))
        ));
    }
}
