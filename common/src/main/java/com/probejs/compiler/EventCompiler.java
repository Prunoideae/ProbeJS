package com.probejs.compiler;

import com.probejs.ProbeJS;
import com.probejs.ProbePaths;
import com.probejs.info.ClassInfo;
import com.probejs.jdoc.document.DocumentClass;
import com.probejs.jdoc.property.PropertyComment;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EventCompiler {

    public static List<Class<?>> fetchEventClasses() {
        return EventGroup.getGroups()
                .values()
                .stream()
                .map(EventGroup::getHandlers)
                .map(Map::values)
                .flatMap(Collection::stream)
                .map(handler -> handler.eventType.get())
                .collect(Collectors.toList());
    }

    public static List<String> getRegistryEventLines() {
        ArrayList<String> lines = new ArrayList<>();
        for (RegistryObjectBuilderTypes<?> types : RegistryObjectBuilderTypes.MAP.values()) {
            String fullName = types.registryKey.location().getNamespace() + "." + types.registryKey.location().getPath().replace('/', '.');
            String registryName = RegistryCompiler.FormatterRegistry.getFormattedRegistryName(types);
            lines.add("register(type: %s, handler: (event: Registry.%s) => void,".formatted(ProbeJS.GSON.toJson(fullName), registryName));
            if (types.registryKey.location().getNamespace().equals("minecraft")) {
                String shortName = types.registryKey.location().getPath().replace('/', '.');
                lines.add("register(type: %s, handler: (event: Registry.%s) => void,".formatted(ProbeJS.GSON.toJson(shortName), registryName));
            }
        }
        return lines;
    }

    public static void compileEvents(Map<String, DocumentClass> globalClasses) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(ProbePaths.GENERATED.resolve("events.d.ts"));
        writer.write("/// <reference path=\"./globals.d.ts\" />\n");
        writer.write("/// <reference path=\"./registries.d.ts\" />\n");

        for (Map.Entry<String, EventGroup> entry : EventGroup.getGroups().entrySet()) {
            String name = entry.getKey();
            EventGroup group = entry.getValue();

            List<String> elements = new ArrayList<>();
            elements.add("{");
            for (Map.Entry<String, EventHandler> e : group.getHandlers().entrySet()) {

                String eventName = e.getKey();
                EventHandler handler = e.getValue();
                Class<?> event = handler.eventType.get();
                ClassInfo eventType = ClassInfo.getOrCache(event);
                if (eventName.equals("registry") && name.equals("StartupEvents")) {
                    elements.addAll(getRegistryEventLines());
                    continue; //Overrides default registry event
                }
                DocumentClass document = globalClasses.get(eventType.getName());
                PropertyComment comment = document.getMergedComment()
                        .merge(new PropertyComment("This event is %scancellable"
                                .formatted(handler.isCancelable() ? "" : "**not** ")));
                elements.addAll(comment.formatLines(4));
                if (handler.getSupportsExtraId()) {
                    elements.add("%s(extra: string, handler: (event: %s) => void),".formatted(
                            eventName, RegistryCompiler.formatMaybeParameterized(event)
                    ));
                }
                if (!handler.getRequiresExtraId()) {
                    elements.add("%s(handler: (event: %s) => void),".formatted(
                            eventName, RegistryCompiler.formatMaybeParameterized(event)
                    ));
                }
            }
            elements.add("};");
            writer.write("declare const %s: %s".formatted(name, String.join("\n", elements)));
        }
    }

}
