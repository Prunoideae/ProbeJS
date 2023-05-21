package com.probejs.compiler;

import com.probejs.ProbeJS;
import com.probejs.ProbePaths;
import com.probejs.compiler.formatter.formatter.FormatterNamespace;
import com.probejs.compiler.formatter.formatter.IFormatter;
import com.probejs.compiler.formatter.formatter.jdoc.FormatterType;
import com.probejs.jdoc.property.PropertyType;
import com.probejs.util.Util;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class RegistryCompiler {
    public static Set<Class<?>> getKJSRegistryClasses() {
        Set<Class<?>> result = new HashSet<>();
        result.add(RegistryObjectBuilderTypes.class);
        result.add(RegistryObjectBuilderTypes.RegistryEventJS.class);
        RegistryObjectBuilderTypes.MAP.values().forEach(v -> v.types.values().forEach(v1 -> result.add(v1.builderClass())));
        return result;
    }

    public static void compileRegistries() throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(ProbePaths.GENERATED.resolve("registries.d.ts"));
        writer.write("/// <reference path=\"./globals.d.ts\" />\n");
        IFormatter namespace = new FormatterNamespace("Registry", RegistryObjectBuilderTypes.MAP.values().stream().map(FormatterRegistry::new).collect(Collectors.toList()));
        writer.write(String.join("\n", namespace.format(0, 4)));
        writer.close();
    }

    public static List<String> compileRegistryEvents() {
        ArrayList<String> lines = new ArrayList<>();
        for (RegistryObjectBuilderTypes<?> types : RegistryObjectBuilderTypes.MAP.values()) {
            String fullName = types.registryKey.location().toString();
            String registryName = RegistryCompiler.FormatterRegistry.getFormattedRegistryName(types);
            lines.add("registry(type: %s, handler: (event: Registry.%s) => void):void,".formatted(ProbeJS.GSON.toJson(fullName), registryName));
            if (types.registryKey.location().getNamespace().equals("minecraft")) {
                String shortName = types.registryKey.location().getPath().replace('/', '.');
                lines.add("registry(type: %s, handler: (event: Registry.%s) => void):void,".formatted(ProbeJS.GSON.toJson(shortName), registryName));
            }
        }
        return lines;
    }

    public static class FormatterRegistry implements IFormatter {
        RegistryObjectBuilderTypes<?> types;
        String name;

        public static String getFormattedRegistryName(RegistryObjectBuilderTypes<?> types) {
            return Arrays.stream(types.registryKey.location().getPath().split("/"))
                    .map(str -> Arrays.stream(str.split("_"))
                            .map(Util::getCapitalized)
                            .collect(Collectors.joining("")))
                    .collect(Collectors.joining(""));
        }

        private FormatterRegistry(RegistryObjectBuilderTypes<?> types) {
            this.types = types;
            this.name = getFormattedRegistryName(types);
        }

        @Override
        public List<String> format(Integer indent, Integer stepIndent) {
            List<String> formatted = new ArrayList<>();
            int stepped = indent + stepIndent;
            formatted.add(" ".repeat(indent) + "class %s extends %s {".formatted(name, formatMaybeParameterized(RegistryObjectBuilderTypes.RegistryEventJS.class)));
            for (RegistryObjectBuilderTypes.BuilderType<?> builder : types.types.values()) {
                formatted.add(" ".repeat(stepped) + "create(id: string, type: %s): %s;".formatted(ProbeJS.GSON.toJson(builder.type()), formatMaybeParameterized(builder.builderClass())));
            }
            if (types.getDefaultType() != null) {
                formatted.add(" ".repeat(stepped) + "create(id: string): %s;".formatted(formatMaybeParameterized(types.getDefaultType().builderClass())));
            }
            formatted.add(" ".repeat(indent) + "}");
            return formatted;
        }
    }

    public static String formatMaybeParameterized(Class<?> clazz) {
        if (clazz.getTypeParameters().length == 0) {
            return new FormatterType.Clazz(new PropertyType.Clazz(clazz.getName())).formatFirst();
        } else {
            return new FormatterType.Parameterized(
                    new PropertyType.Parameterized(
                            new PropertyType.Clazz(clazz.getName()),
                            Collections.nCopies(clazz.getTypeParameters().length, new PropertyType.Clazz(Object.class.getName()))
                    )
            ).formatFirst();
        }
    }
}
