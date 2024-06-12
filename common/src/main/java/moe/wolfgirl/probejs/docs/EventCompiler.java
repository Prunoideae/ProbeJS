package moe.wolfgirl.probejs.docs;

import com.mojang.datafixers.util.Pair;
import moe.wolfgirl.probejs.ProbePaths;
import moe.wolfgirl.probejs.jdoc.Serde;
import moe.wolfgirl.probejs.jdoc.document.DocumentClass;
import moe.wolfgirl.probejs.jdoc.java.ClassInfo;
import moe.wolfgirl.probejs.jdoc.property.AbstractProperty;
import moe.wolfgirl.probejs.jdoc.property.PropertyComment;
import moe.wolfgirl.probejs.jdoc.property.PropertyType;
import moe.wolfgirl.probejs.specials.RegistryCompiler;
import moe.wolfgirl.probejs.specials.TagEventCompiler;
import moe.wolfgirl.probejs.specials.special.recipe.component.ComponentConverter;
import moe.wolfgirl.probejs.util.Util;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.typings.desc.TypeDescJS;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EventCompiler {
    public static Map<Pair<String, String>, Function<EventHandler, List<String>>> SPECIAL_EVENT_OVERRIDE = new HashMap<>();

    public static void initSpecialEvents() {
        SPECIAL_EVENT_OVERRIDE.put(new Pair<>("StartupEvents", "registry"), handler -> RegistryCompiler.getRegistryEventOverrides());
        SPECIAL_EVENT_OVERRIDE.put(new Pair<>("ServerEvents", "tags"), handler -> TagEventCompiler.getTagEventOverrides());
    }

    public static List<Class<?>> fetchEventClasses() {
        return EventGroup.getGroups().values().stream().map(EventGroup::getHandlers).map(Map::values).flatMap(Collection::stream).map(handler -> handler.eventType.get()).collect(Collectors.toList());
    }

    private static <T extends AbstractProperty<T>> Optional<T> findProperty(Map<String, DocumentClass> globalClasses, DocumentClass documentClass, Class<T> propertyClass) {
        var result = documentClass.findProperty(propertyClass);
        if (result.isPresent()) return result;
        var parent = PropertyType.getClazzName(documentClass.getParent()).orElse(null);
        if (parent != null && globalClasses.containsKey(parent)) {
            return findProperty(globalClasses, globalClasses.get(parent), propertyClass);
        }

        for (PropertyType<?> type : documentClass.getInterfaces()) {
            String implemented = PropertyType.getClazzName(type).orElse(null);
            if (implemented != null && globalClasses.containsKey(implemented)) {
                Optional<T> found = findProperty(globalClasses, globalClasses.get(implemented), propertyClass);
                if (found.isPresent()) return found;
            }
        }

        return Optional.empty();
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

                Function<EventHandler, List<String>> specialHandler = SPECIAL_EVENT_OVERRIDE.get(new Pair<>(name, eventName));

                if (specialHandler != null) {
                    elements.addAll(specialHandler.apply(handler));
                    continue; //Overrides default event formatting
                }
                DocumentClass document = globalClasses.get(eventType.getName());
                PropertyComment comment = document.getMergedComment()
                        .merge(new PropertyComment("@at *%s*".formatted(
                                handler.scriptTypePredicate
                                        .getValidTypes()
                                        .stream()
                                        .map(type -> type.name)
                                        .collect(Collectors.joining(", "))
                        )));
                if (handler.getHasResult())
                    comment = comment.merge(new PropertyComment("@cancellable"));

                elements.addAll(comment.formatLines(4));
                if (handler.extra != null) {
                    TypeDescJS desc = handler.extra.describeType.apply(ComponentConverter.PROBEJS_CONTEXT);
                    PropertyType<?> type = ComponentConverter.fromDescription(desc);

                    elements.add("%s(extra: %s, handler: (event: %s) => void):void,".formatted(
                            eventName,
                            Serde.getTypeFormatter(type)
                                    .underscored()
                                    .formatFirst(),
                            Util.formatMaybeParameterized(event)
                    ));
                }
                if (handler.extra == null || !handler.extra.required) {
                    elements.add("%s(handler: (event: %s) => void):void,".formatted(eventName, Util.formatMaybeParameterized(event)));
                }
            }
            elements.add("};\n");
            writer.write("declare const %s: %s".formatted(name, String.join("\n", elements)));
        }
        writer.close();
    }

}
