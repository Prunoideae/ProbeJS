package com.probejs.formatter.formatter.special;

import com.probejs.ProbeCommands;
import com.probejs.ProbeJS;
import com.probejs.formatter.formatter.IFormatter;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.stream.Collectors;

public class FormatterAdvancement implements IFormatter {
    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        return List.of("%stype Advancement = %s;".formatted(" ".repeat(indent),
                ProbeCommands.COMMAND_LEVEL.getServer()
                        .getAdvancements()
                        .getAllAdvancements()
                        .stream()
                        .map(Advancement::getId)
                        .map(ResourceLocation::toString)
                        .map(ProbeJS.GSON::toJson)
                        .collect(Collectors.joining(" | "))
        ));
    }
}
