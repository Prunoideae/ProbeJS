package com.probejs.specials;

import com.probejs.ProbeCommands;
import com.probejs.ProbeJS;
import com.probejs.ProbePaths;
import com.probejs.docs.formatter.formatter.FormatterNamespace;
import com.probejs.docs.formatter.formatter.IFormatter;
import com.probejs.util.RLHelper;
import com.probejs.util.Util;
import dev.latvian.mods.kubejs.server.tag.TagEventJS;
import dev.latvian.mods.kubejs.server.tag.TagWrapper;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TagEventCompiler {

    @SuppressWarnings("unchecked")
    public static void compileTagEvents() throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(ProbePaths.GENERATED.resolve("tag_events.d.ts"));
        writer.write("/// <reference path=\"./globals.d.ts\" />\n");
        IFormatter namespace = new FormatterNamespace("TagEvent",
                ProbeCommands.COMMAND_LEVEL.registryAccess()
                        .registries()
                        .map(RegistryAccess.RegistryEntry::key)
                        .map(r -> new FormatterTagEvent((ResourceKey<Registry<?>>) r))
                        .collect(Collectors.toList())
        );
        writer.write(String.join("\n", namespace.format(0, 4)));
        writer.close();
    }

    @SuppressWarnings("unchecked")
    public static List<String> getTagEventOverrides() {
        ArrayList<String> lines = new ArrayList<>();
        ProbeCommands.COMMAND_LEVEL.registryAccess().registries().forEach(entry -> {
            if (entry.value().getTagNames().findAny().isEmpty()) return;
            ResourceKey<Registry<?>> registry = (ResourceKey<Registry<?>>) entry.key();
            if (registry.location().getNamespace().equals("minecraft")) {
                lines.add("tags(type: %s, handler: (event: TagEvent.%s) => void): void".formatted(
                        ProbeJS.GSON.toJson(registry.location().getPath()),
                        RLHelper.finalComponentToTitle(registry.location().getPath())
                ));
            }
            lines.add("tags(type: %s, handler: (event: TagEvent.%s) => void): void".formatted(
                    ProbeJS.GSON.toJson(registry.location().toString()),
                    RLHelper.finalComponentToTitle(registry.location().getPath())
            ));
        });
        return lines;
    }

    public static class FormatterTagEvent implements IFormatter {
        private final ResourceKey<Registry<?>> registry;

        public FormatterTagEvent(ResourceKey<Registry<?>> registry) {
            this.registry = registry;
        }

        @Override
        public List<String> format(Integer indent, Integer stepIndent) {
            ArrayList<String> formatted = new ArrayList<>();
            String capitalized = RLHelper.finalComponentToTitle(registry.location().getPath());
            String specialType = "Special.%s".formatted(capitalized);
            String tagType = specialType + "Tag";
            String wrapperType = Util.formatMaybeParameterized(TagWrapper.class);

            formatted.add("%sclass %s extends %s {".formatted(" ".repeat(indent), capitalized, Util.formatMaybeParameterized(TagEventJS.class)));
            formatted.add("%sget(id: %s): %s".formatted(" ".repeat(indent), tagType, wrapperType));
            formatted.add("%sadd(tag: %s, ...id: %s[]): %s".formatted(" ".repeat(indent), tagType, specialType, wrapperType));
            formatted.add("%sremove(tag: %s, ...id: %s[]): %s".formatted(" ".repeat(indent), tagType, specialType, wrapperType));
            formatted.add("%sremoveAll(tag: %s): %s".formatted(" ".repeat(indent), tagType, wrapperType));
            formatted.add("%sremoveAllTagsFrom(...id: %s[]): %s".formatted(" ".repeat(indent), specialType, wrapperType));
            formatted.add(" ".repeat(indent) + "}");
            return formatted;
        }
    }
}
