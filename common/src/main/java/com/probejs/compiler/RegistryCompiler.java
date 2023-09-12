package com.probejs.compiler;

import com.probejs.ProbeJS;
import com.probejs.ProbePaths;
import com.probejs.compiler.formatter.formatter.FormatterNamespace;
import com.probejs.compiler.formatter.formatter.IFormatter;
import com.probejs.util.RLHelper;
import com.probejs.util.Util;
import dev.latvian.mods.kubejs.registry.BuilderType;
import dev.latvian.mods.kubejs.registry.RegistryEventJS;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import net.minecraft.resources.ResourceLocation;


import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class RegistryCompiler {
    public static Set<Class<?>> getKJSRegistryClasses() {
        Set<Class<?>> result = new HashSet<>();
        result.add(RegistryInfo.class);
        result.add(RegistryEventJS.class);
        RegistryInfo.MAP.values().forEach(v -> v.types.values().forEach(v1 -> result.add(v1.builderClass())));
        return result;
    }

    public static void compileRegistryEvents() throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(ProbePaths.GENERATED.resolve("registries.d.ts"));
        writer.write("/// <reference path=\"./globals.d.ts\" />\n");
        IFormatter namespace = new FormatterNamespace("Registry", RegistryInfo.MAP.values().stream().map(FormatterRegistry::new).collect(Collectors.toList()));
        writer.write(String.join("\n", namespace.format(0, 4)));
        writer.close();
    }

    public static List<String> getRegistryEventOverrides() {
        ArrayList<String> lines = new ArrayList<>();
        for (RegistryInfo<?> types : RegistryInfo.MAP.values()) {
            ResourceLocation loc = types.key.location();
            String registryName = RegistryCompiler.FormatterRegistry.getFormattedRegistryName(types);
            lines.add("registry(type: %s, handler: (event: Registry.%s) => void):void,".formatted(ProbeJS.GSON.toJson(loc.toString()), registryName));
            if (loc.getNamespace().equals("minecraft")) {
                String shortName = loc.getPath().replace('/', '.');
                lines.add("registry(type: %s, handler: (event: Registry.%s) => void):void,".formatted(ProbeJS.GSON.toJson(shortName), registryName));
            }
        }
        return lines;
    }

    public static class FormatterRegistry implements IFormatter {
        RegistryInfo<?> types;
        String name;

        public static String getFormattedRegistryName(RegistryInfo<?> types) {
            return RLHelper.rlToTitle(types.key.location().getPath());
        }

        private FormatterRegistry(RegistryInfo<?> types) {
            this.types = types;
            this.name = getFormattedRegistryName(types);
        }

        @Override
        public List<String> format(Integer indent, Integer stepIndent) {
            List<String> formatted = new ArrayList<>();
            int stepped = indent + stepIndent;
            formatted.add(" ".repeat(indent) + "class %s extends %s {".formatted(name, Util.formatMaybeParameterized(RegistryEventJS.class)));
            for (BuilderType<?> builder : types.types.values()) {
                formatted.add(" ".repeat(stepped) + "create(id: string, type: %s): %s;".formatted(ProbeJS.GSON.toJson(builder.type()), Util.formatMaybeParameterized(builder.builderClass())));
            }
            if (types.getDefaultType() != null) {
                formatted.add(" ".repeat(stepped) + "create(id: string): %s;".formatted(Util.formatMaybeParameterized(types.getDefaultType().builderClass())));
            }
            formatted.add(" ".repeat(indent) + "}");
            return formatted;
        }
    }

}
