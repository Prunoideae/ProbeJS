package com.probejs.compiler;

import com.google.gson.*;
import com.probejs.ProbeJS;
import com.probejs.ProbePaths;
import com.probejs.document.DocumentClass;
import com.probejs.document.Manager;
import com.probejs.event.CapturedEvent;
import com.probejs.formatter.ClassResolver;
import com.probejs.formatter.NameResolver;
import com.probejs.formatter.SpecialTypes;
import com.probejs.formatter.formatter.FormatterClass;
import com.probejs.formatter.formatter.FormatterNamespace;
import com.probejs.formatter.formatter.FormatterRawTS;
import com.probejs.formatter.formatter.IFormatter;
import com.probejs.info.ClassInfo;
import com.probejs.info.Walker;
import com.probejs.info.type.TypeInfoClass;
import com.probejs.plugin.CapturedClasses;
import dev.latvian.mods.kubejs.KubeJSPaths;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.recipe.RecipeTypeJS;
import dev.latvian.mods.kubejs.recipe.RegisterRecipeHandlersEvent;
import dev.latvian.mods.kubejs.server.ServerScriptManager;
import dev.latvian.mods.kubejs.util.KubeJSPlugins;
import net.minecraft.resources.ResourceLocation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class TypingCompiler {

    public static Set<Class<?>> readCachedClasses(String fileName) throws IOException {
        Set<Class<?>> cachedClasses = new HashSet<>();
        Path cachedClassesPath = KubeJSPaths.EXPORTED.resolve(fileName);
        if (Files.exists(cachedClassesPath)) {
            try {
                List<?> cachedList = new Gson().fromJson(Files.newBufferedReader(cachedClassesPath), List.class);
                cachedList.forEach((c) -> {
                    try {
                        Class<?> clazz = Class.forName((String) c);
                        cachedClasses.add(clazz);
                    } catch (ClassNotFoundException e) {
                        ProbeJS.LOGGER.warn("Class %s was in the cache, but disappeared in packages now.".formatted(c));
                    }
                });
            } catch (JsonSyntaxException | JsonIOException e) {
                ProbeJS.LOGGER.warn("Cannot read malformed cache, ignoring.");
            }
        }
        return cachedClasses;
    }

    public static void writeCachedClasses(String fileName, Set<Class<?>> javaClasses) throws IOException {
        BufferedWriter cacheWriter = Files.newBufferedWriter(KubeJSPaths.EXPORTED.resolve(fileName));
        JsonArray outJson = new JsonArray();
        for (Class<?> clazz : javaClasses) {
            outJson.add(clazz.getName());
        }
        new Gson().toJson(outJson, cacheWriter);
        cacheWriter.flush();
    }

    public static Map<String, CapturedEvent> readCachedEvents(String fileName) throws IOException {
        Map<String, CapturedEvent> cachedEvents = new HashMap<>();
        Path cachedEventPath = KubeJSPaths.EXPORTED.resolve(fileName);
        if (Files.exists(cachedEventPath)) {
            try {
                JsonObject cachedMap = new Gson().fromJson(Files.newBufferedReader(cachedEventPath), JsonObject.class);
                for (Map.Entry<String, JsonElement> entry : cachedMap.entrySet()) {
                    String key = entry.getKey();
                    JsonElement value = entry.getValue();
                    if (value.isJsonObject()) {
                        var obj = value.getAsJsonObject();
                        if (obj.has("class") && obj.has("id")) {
                            try {
                                Class<?> clazz = Class.forName(obj.get("class").getAsString());
                                if (EventJS.class.isAssignableFrom(clazz)) {
                                    cachedEvents.put(key, new CapturedEvent((Class<? extends EventJS>) clazz, obj.get("id").getAsString(), obj.has("sub") ? obj.get("sub").getAsString() : null));
                                    continue;
                                }
                            } catch (ClassNotFoundException e) {
                                ProbeJS.LOGGER.warn("Class %s was in the cache, but disappeared in packages now.".formatted(obj.get("class").getAsString()));
                                continue;
                            }
                        }
                    }
                    ProbeJS.LOGGER.warn("Dropping unknown/unsupported entry: %s, this may caused by a change in cache format, please regenerate the dump".formatted(entry.getKey()));
                }
            } catch (JsonSyntaxException | JsonIOException e) {
                ProbeJS.LOGGER.warn("Cannot read malformed cache, ignoring.");
            }
        }
        return cachedEvents;
    }

    public static Map<String, Class<?>> readCachedForgeEvents(String fileName) throws IOException {
        Map<String, Class<?>> cachedEvents = new HashMap<>();
        Path cachedEventPath = KubeJSPaths.EXPORTED.resolve(fileName);
        if (Files.exists(cachedEventPath)) {
            try {
                Map<?, ?> cachedMap = new Gson().fromJson(Files.newBufferedReader(cachedEventPath), Map.class);
                cachedMap.forEach((k, v) -> {
                    if (k instanceof String && v instanceof String) {
                        try {
                            Class<?> clazz = Class.forName((String) v);
                            if (EventJS.class.isAssignableFrom(clazz))
                                cachedEvents.put((String) k, clazz);
                        } catch (ClassNotFoundException e) {
                            ProbeJS.LOGGER.warn("Class %s was in the cache, but disappeared in packages now.".formatted(v));
                        }
                    }
                });
            } catch (JsonSyntaxException | JsonIOException e) {
                ProbeJS.LOGGER.warn("Cannot read malformed cache, ignoring.");
            }
        }
        return cachedEvents;
    }

    public static void writeCachedEvents(String fileName, Map<String, CapturedEvent> events) throws IOException {
        BufferedWriter cacheWriter = Files.newBufferedWriter(KubeJSPaths.EXPORTED.resolve(fileName));
        JsonObject outJson = new JsonObject();
        for (Map.Entry<String, CapturedEvent> entry : events.entrySet()) {
            String eventName = entry.getKey();
            CapturedEvent eventClass = entry.getValue();
            var captured = new JsonObject();
            captured.addProperty("class", eventClass.getCaptured().getName());
            captured.addProperty("id", eventClass.getId());
            if (eventClass.hasSub())
                captured.addProperty("sub", eventClass.getSub());
            outJson.add(eventName, captured);
        }
        Gson gson = new Gson();
        gson.toJson(outJson, cacheWriter);
        cacheWriter.flush();
    }

    public static void writeCachedForgeEvents(String fileName, Map<String, Class<?>> events) throws IOException {
        BufferedWriter cacheWriter = Files.newBufferedWriter(KubeJSPaths.EXPORTED.resolve(fileName));
        JsonObject outJson = new JsonObject();
        for (Map.Entry<String, Class<?>> entry : events.entrySet()) {
            String eventName = entry.getKey();
            Class<?> eventClass = entry.getValue();
            outJson.addProperty(eventName, eventClass.getName());
        }
        Gson gson = new Gson();
        gson.toJson(outJson, cacheWriter);
        cacheWriter.flush();
    }

    public static Set<Class<?>> fetchClasses(Map<ResourceLocation, RecipeTypeJS> typeMap, DummyBindingEvent bindingEvent, Set<Class<?>> cachedClasses) {
        Set<Class<?>> touchableClasses = new HashSet<>(bindingEvent.getClassDumpMap().values());
        touchableClasses.addAll(cachedClasses);
        touchableClasses.addAll(typeMap.values().stream().map(recipeTypeJS -> recipeTypeJS.factory.get().getClass()).collect(Collectors.toList()));
        touchableClasses.addAll(bindingEvent.getConstantDumpMap().values().stream().map(Object::getClass).collect(Collectors.toList()));
        touchableClasses.addAll(CapturedClasses.capturedEvents.values().stream().map(CapturedEvent::getCaptured).collect(Collectors.toList()));
        touchableClasses.addAll(CapturedClasses.capturedRawEvents.values());
        touchableClasses.addAll(CapturedClasses.capturedJavaClasses);

        Walker walker = new Walker(touchableClasses);
        return walker.walk();
    }

    public static void compileGlobal(DummyBindingEvent bindingEvent, Set<Class<?>> globalClasses) throws IOException {

        bindingEvent.getClassDumpMap().forEach((s, c) -> NameResolver.putResolvedName(c, s));
        NameResolver.resolveNames(globalClasses);

        BufferedWriter writer = Files.newBufferedWriter(ProbePaths.GENERATED.resolve("globals.d.ts"));
        Map<String, List<IFormatter>> namespaced = new HashMap<>();

        for (Class<?> clazz : globalClasses) {
            FormatterClass formatter = new FormatterClass(ClassInfo.getOrCache(clazz));
            Manager.classDocuments.getOrDefault(clazz.getName(), new ArrayList<>()).forEach(formatter::addDocument);

            NameResolver.ResolvedName name = NameResolver.getResolvedName(clazz.getName());
            if (name.getNamespace().isEmpty()) {
                writer.write(String.join("\n", formatter.format(0, 4)) + "\n");
                if (clazz.isInterface())
                    writer.write("declare const %s: %s;".formatted(name.getFullName(), name.getFullName()) + "\n");
            } else {
                formatter.setInternal(true);
                namespaced.computeIfAbsent(name.getNamespace(), s -> new ArrayList<>()).add(formatter);
            }
        }

        for (Map.Entry<String, List<IFormatter>> entry : namespaced.entrySet()) {
            String path = entry.getKey();
            List<IFormatter> formatters = entry.getValue();
            FormatterNamespace namespace = new FormatterNamespace(path, formatters);
            writer.write(String.join("\n", namespace.format(0, 4)) + "\n");
        }

        for (Map.Entry<String, List<DocumentClass>> entry : Manager.classAdditions.entrySet()) {
            List<DocumentClass> document = entry.getValue();
            DocumentClass start = document.get(0);
            document.subList(1, document.size()).forEach(start::merge);
        }

        writer.write(String.join("\n", new FormatterNamespace("Document", Manager.classAdditions.values().stream().map(l -> l.get(0)).collect(Collectors.toList())).format(0, 4)) + "\n");
        writer.write(String.join("\n", new FormatterNamespace("Type", Manager.typeDocuments).format(0, 4)) + "\n");
        writer.write(String.join("\n", new FormatterRawTS(Manager.rawTSDoc).format(0, 4)) + "\n");
        writer.flush();
    }

    public static void compileEvents(Map<String, CapturedEvent> cachedEvents, Map<String, Class<?>> cachedForgeEvents) throws IOException {
        cachedEvents.putAll(CapturedClasses.capturedEvents);
        cachedForgeEvents.putAll(CapturedClasses.capturedRawEvents);
        BufferedWriter writer = Files.newBufferedWriter(ProbePaths.GENERATED.resolve("events.d.ts"));
        writer.write("/// <reference path=\"./globals.d.ts\" />\n");
        writer.write("/// <reference path=\"./registries.d.ts\" />\n");
        Gson g = new Gson();
        Set<CapturedEvent> wildcards = new HashSet<>();
        for (Map.Entry<String, CapturedEvent> entry : cachedEvents.entrySet()) {
            String id = entry.getValue().getId();
            Class<?> event = entry.getValue().getCaptured();
            String sub = entry.getValue().getSub();
            if (entry.getValue().hasSub())
                wildcards.add(entry.getValue());
            writer.write("declare function onEvent(name: %s, handler: (event: %s) => void);\n".formatted(g.toJson(id + (sub == null ? "" : ("." + sub))), FormatterClass.formatTypeParameterized(new TypeInfoClass(event))));
        }

        Set<String> writtenWildcards = new HashSet<>();
        for (CapturedEvent wildcard : wildcards) {
            String id = g.toJson(wildcard.getId());
            if (writtenWildcards.contains(id))
                continue;
            writtenWildcards.add(id);
            writer.write("declare function onEvent(name: `%s.${string}`, handler: (event: %s) => void);\n".formatted(id.substring(1, id.length() - 1), FormatterClass.formatTypeParameterized(new TypeInfoClass(wildcard.getCaptured()))));
        }

        for (Map.Entry<String, Class<?>> entry : cachedForgeEvents.entrySet()) {
            String name = entry.getKey();
            Class<?> event = entry.getValue();
            writer.write("declare function onForgeEvent(name: %s, handler: (event: %s) => void);\n".formatted(g.toJson(name), FormatterClass.formatTypeParameterized(new TypeInfoClass(event))));
        }
        RegistryCompiler.compileEventRegistries(writer);
        writer.flush();
    }

    public static void compileConstants(DummyBindingEvent bindingEvent) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(ProbePaths.GENERATED.resolve("constants.d.ts"));
        writer.write("/// <reference path=\"./globals.d.ts\" />\n");
        for (Map.Entry<String, Object> entry : bindingEvent.getConstantDumpMap().entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            String resolved = NameResolver.formatValue(value);
            writer.write("declare const %s: %s;\n".formatted(name, resolved == null ? FormatterClass.formatTypeParameterized(new TypeInfoClass(value.getClass())) : resolved));
        }
        writer.flush();
    }

    public static void compileJava(Set<Class<?>> globalClasses) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(ProbePaths.GENERATED.resolve("java.d.ts"));
        writer.write("/// <reference path=\"./globals.d.ts\" />\n");
        for (Class<?> c : globalClasses) {
            if (ServerScriptManager.instance.scriptManager.isClassAllowed(c.getName())) {
                writer.write("declare function java(name: \"%s\"): typeof %s;\n".formatted(c.getName(), FormatterClass.formatTypeParameterized(new TypeInfoClass(c))));
            }
        }
        writer.flush();
    }

    public static void compileJSConfig() throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(KubeJSPaths.DIRECTORY.resolve("jsconfig.json"));
        writer.write("""
                {
                    "compilerOptions": {
                        "lib": ["ES5", "ES2015"],
                        "typeRoots": ["./probe/generated", "./probe/user"]
                    }
                }""");
        writer.flush();
    }

    public static void compile() throws IOException {
        DummyBindingEvent bindingEvent = new DummyBindingEvent(ServerScriptManager.instance.scriptManager);
        Map<ResourceLocation, RecipeTypeJS> typeMap = new HashMap<>();
        RegisterRecipeHandlersEvent recipeEvent = new RegisterRecipeHandlersEvent(typeMap);

        KubeJSPlugins.forEachPlugin(plugin -> plugin.addRecipes(recipeEvent));
        KubeJSPlugins.forEachPlugin(plugin -> plugin.addBindings(bindingEvent));

        Map<String, CapturedEvent> cachedEvents = readCachedEvents("cachedEvents.json");
        Map<String, Class<?>> cachedForgeEvents = readCachedForgeEvents("cachedForgeEvents.json");
        Set<Class<?>> cachedJavaClasses = readCachedClasses("cachedJava.json");
        Set<Class<?>> cachedClasses = new HashSet<>();

        cachedEvents.values().forEach(v -> cachedClasses.add(v.getCaptured()));
        cachedClasses.addAll(cachedForgeEvents.values());
        cachedClasses.addAll(cachedJavaClasses);
        cachedClasses.addAll(RegistryCompiler.getRegistryClasses());

        Set<Class<?>> globalClasses = fetchClasses(typeMap, bindingEvent, cachedClasses);
        globalClasses.removeIf(c -> ClassResolver.skipped.contains(c));
        SpecialTypes.processFunctionalInterfaces(globalClasses);
        compileGlobal(bindingEvent, globalClasses);
        RegistryCompiler.compileRegistries();
        compileEvents(cachedEvents, cachedForgeEvents);
        compileConstants(bindingEvent);
        compileJava(globalClasses);
        compileJSConfig();
        cachedJavaClasses.addAll(CapturedClasses.capturedJavaClasses);
        writeCachedEvents("cachedEvents.json", cachedEvents);
        writeCachedForgeEvents("cachedForgedEvents.json", cachedForgeEvents);
        writeCachedClasses("cachedJava.json", cachedJavaClasses);
    }

}
