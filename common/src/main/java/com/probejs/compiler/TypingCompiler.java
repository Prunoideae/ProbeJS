package com.probejs.compiler;

import com.google.gson.*;
import com.probejs.ProbeJS;
import com.probejs.ProbePaths;
import com.probejs.document.DocumentClass;
import com.probejs.document.DocumentComment;
import com.probejs.document.Manager;
import com.probejs.event.CapturedEvent;
import com.probejs.formatter.ClassResolver;
import com.probejs.formatter.NameResolver;
import com.probejs.formatter.SpecialTypes;
import com.probejs.formatter.formatter.clazz.FormatterClass;
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
                List<?> cachedList = ProbeJS.GSON.fromJson(Files.newBufferedReader(cachedClassesPath), List.class);
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
        ProbeJS.GSON.toJson(outJson, cacheWriter);
        cacheWriter.flush();
    }

    public static Map<String, CapturedEvent> readCachedEvents(String fileName) throws IOException {
        Map<String, CapturedEvent> cachedEvents = new HashMap<>();
        Path cachedEventPath = KubeJSPaths.EXPORTED.resolve(fileName);
        if (Files.exists(cachedEventPath)) {
            try {
                JsonObject cachedMap = ProbeJS.GSON.fromJson(Files.newBufferedReader(cachedEventPath), JsonObject.class);
                for (Map.Entry<String, JsonElement> entry : cachedMap.entrySet()) {
                    String key = entry.getKey();
                    JsonElement value = entry.getValue();
                    if (value.isJsonObject()) {
                        CapturedEvent.fromJson(value.getAsJsonObject())
                                .ifPresent(event -> cachedEvents.put(key, event));
                    }
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
                Map<?, ?> cachedMap = ProbeJS.GSON.fromJson(Files.newBufferedReader(cachedEventPath), Map.class);
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
            outJson.add(eventName, eventClass.toJson());
        }
        ProbeJS.GSON.toJson(outJson, cacheWriter);
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
        ProbeJS.GSON.toJson(outJson, cacheWriter);
        cacheWriter.flush();
    }

    public static Set<Class<?>> fetchClasses(Map<ResourceLocation, RecipeTypeJS> typeMap, DummyBindingEvent bindingEvent, Set<Class<?>> cachedClasses) {
        Set<Class<?>> touchableClasses = new HashSet<>(bindingEvent.getClassDumpMap().values());
        touchableClasses.addAll(cachedClasses);
        touchableClasses.addAll(typeMap.values().stream().map(recipeTypeJS -> recipeTypeJS.factory.get().getClass()).collect(Collectors.toList()));
        bindingEvent.getConstantDumpMap().values().stream().map(DummyBindingEvent::getConstantClassRecursive).forEach(touchableClasses::addAll);
        touchableClasses.addAll(CapturedClasses.capturedEvents.values().stream().map(CapturedEvent::getCaptured).collect(Collectors.toList()));
        touchableClasses.addAll(CapturedClasses.capturedRawEvents.values());
        touchableClasses.addAll(CapturedClasses.capturedJavaClasses);

        Walker walker = new Walker(touchableClasses);
        return walker.walk();
    }

    public static void compileGlobal(DummyBindingEvent bindingEvent, Set<Class<?>> globalClasses) throws IOException {

        bindingEvent.getClassDumpMap().forEach((s, c) -> NameResolver.putResolvedName(c, s));
        NameResolver.resolveNames(NameResolver.priorSortClasses(globalClasses));

        BufferedWriter writer = Files.newBufferedWriter(ProbePaths.GENERATED.resolve("globals.d.ts"));
        Map<String, List<IFormatter>> namespaced = new HashMap<>();

        for (Class<?> clazz : globalClasses) {
            ClassInfo info = ClassInfo.getOrCache(clazz);
            FormatterClass formatter = new FormatterClass(info);
            Manager.classDocuments.getOrDefault(clazz.getName(), new ArrayList<>()).forEach(formatter::addDocument);

            NameResolver.ResolvedName name = NameResolver.getResolvedName(info.getName());
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
        writer.write(String.join("\n", new FormatterNamespace("Special", SpecialCompiler.compileSpecial()).format(0, 4)) + "\n");
        writer.write(String.join("\n", new FormatterRawTS(Manager.rawTSDoc).format(0, 4)) + "\n");
        writer.flush();
    }

    private static List<String> getAdditionalEventComments(CapturedEvent event) {
        List<String> comments = new ArrayList<>();
        //TODO: find a way to properly check for sides.

        if (!event.getScriptTypes().isEmpty()) {
            comments.add("* ");
            comments.add("* The event fires on: %s.".formatted(event.getFormattedTypeString()));
        }

        comments.add("* ");
        comments.add("* The event is %scancellable.".formatted(event.isCancellable() ? "" : "**not** "));
        return comments;
    }

    public static void compileEvents(Map<String, CapturedEvent> cachedEvents, Map<String, Class<?>> cachedForgeEvents) throws IOException {
        cachedEvents.putAll(CapturedClasses.capturedEvents);
        cachedForgeEvents.putAll(CapturedClasses.capturedRawEvents);
        BufferedWriter writer = Files.newBufferedWriter(ProbePaths.GENERATED.resolve("events.d.ts"));
        BufferedWriter writerDoc = Files.newBufferedWriter(ProbePaths.GENERATED.resolve("events.documented.d.ts"));

        writer.write("/// <reference path=\"./globals.d.ts\" />\n");
        writer.write("/// <reference path=\"./registries.d.ts\" />\n");
        writerDoc.write("/// <reference path=\"./globals.d.ts\" />\n");
        writerDoc.write("/// <reference path=\"./registries.d.ts\" />\n");

        Set<CapturedEvent> wildcards = new HashSet<>();
        for (Map.Entry<String, CapturedEvent> entry : cachedEvents.entrySet()) {
            CapturedEvent capturedEvent = entry.getValue();
            String id = capturedEvent.getId();
            Class<?> event = capturedEvent.getCaptured();
            String sub = capturedEvent.getSub();
            if (capturedEvent.hasSub())
                wildcards.add(capturedEvent);
            Optional<DocumentComment> document = Manager.classDocuments
                    .getOrDefault(event.getName(), new ArrayList<>())
                    .stream()
                    .map(DocumentClass::getComment)
                    .filter(Objects::nonNull)
                    .findFirst();
            String name = id + (sub == null ? "" : ("." + sub));
            if (document.isPresent()) {
                List<String> docStrings = document.get().format(0, 4);
                docStrings.addAll(docStrings.size() - 1, getAdditionalEventComments(capturedEvent));
                for (String s : docStrings)
                    writerDoc.write(s + "\n");
                writerDoc.write("declare function onEvent(name: %s, handler: (event: %s) => void);\n".formatted(ProbeJS.GSON.toJson(name), FormatterClass.formatTypeParameterized(new TypeInfoClass(event))));
                continue;
            }
            List<String> docStrings = new ArrayList<>();
            docStrings.add("/**");
            docStrings.addAll(getAdditionalEventComments(capturedEvent));
            docStrings.add("*/");
            for (String s : docStrings)
                writer.write(s + "\n");
            writer.write("declare function onEvent(name: %s, handler: (event: %s) => void);\n".formatted(ProbeJS.GSON.toJson(name), FormatterClass.formatTypeParameterized(new TypeInfoClass(event))));
        }

        Set<String> writtenWildcards = new HashSet<>();
        for (CapturedEvent wildcard : wildcards) {
            String id = ProbeJS.GSON.toJson(wildcard.getId());
            if (writtenWildcards.contains(id))
                continue;
            writtenWildcards.add(id);
            Optional<DocumentComment> document = Manager.classDocuments
                    .getOrDefault(wildcard.getCaptured().getName(), new ArrayList<>())
                    .stream()
                    .map(DocumentClass::getComment)
                    .filter(Objects::nonNull)
                    .findFirst();
            if (document.isPresent()) {
                for (String s : document.get().format(0, 4))
                    writerDoc.write(s + "\n");
                writerDoc.write("declare function onEvent(name: `%s.${string}`, handler: (event: %s) => void);\n".formatted(id.substring(1, id.length() - 1), FormatterClass.formatTypeParameterized(new TypeInfoClass(wildcard.getCaptured()))));
                continue;
            }
            writer.write("declare function onEvent(name: `%s.${string}`, handler: (event: %s) => void);\n".formatted(id.substring(1, id.length() - 1), FormatterClass.formatTypeParameterized(new TypeInfoClass(wildcard.getCaptured()))));
        }

        for (Map.Entry<String, Class<?>> entry : cachedForgeEvents.entrySet()) {
            String name = entry.getKey();
            Class<?> event = entry.getValue();
            writer.write("declare function onForgeEvent(name: %s, handler: (event: %s) => void);\n".formatted(ProbeJS.GSON.toJson(name), FormatterClass.formatTypeParameterized(new TypeInfoClass(event))));
        }
        RegistryCompiler.compileEventRegistries(writer);
        writer.flush();
        writerDoc.flush();
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

    private static String formatJavaType(ClassInfo c) {
        if (c.isInterface()) {
            return "declare function java(name: \"%s\"): TSDoc.JavaInterface<%s>;\n".formatted(c.getName(), FormatterClass.formatTypeParameterized(new TypeInfoClass(c.getClazzRaw())));
        } else {
            return "declare function java(name: \"%s\"): TSDoc.JavaClass<typeof %s>;\n".formatted(c.getName(), FormatterClass.formatTypeParameterized(new TypeInfoClass(c.getClazzRaw())));
        }
    }

    public static void compileJava(Set<Class<?>> globalClasses) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(ProbePaths.GENERATED.resolve("java.d.ts"));
        writer.write("/// <reference path=\"./globals.d.ts\" />\n");
        for (Class<?> c : globalClasses) {
            ClassInfo info = ClassInfo.getOrCache(c);
            if (ServerScriptManager.instance.scriptManager.isClassAllowed(c.getName())) {
                writer.write(formatJavaType(info));
            }
        }
        writer.write("/**\n* This name is not present in current ProbeJS's dump, if the class exists, dump after this java() is called to fetch typing.\n*/\n");
        writer.write("declare function java(name: string): never;\n");
        writer.flush();
    }

    public static void compileAdditionalTypeNames() throws IOException {
        Path path = ProbePaths.GENERATED.resolve("names.d.ts");
        if (Files.exists(path))
            return;
        BufferedWriter writer = Files.newBufferedWriter(ProbePaths.GENERATED.resolve("names.d.ts"));
        writer.write("/// <reference path=\"./globals.d.ts\" />\n");
        for (Map.Entry<String, List<NameResolver.ResolvedName>> entry : NameResolver.resolvedNames.entrySet()) {
            List<NameResolver.ResolvedName> exportedNames = entry.getValue();
            if (exportedNames.size() > 1) {
                for (int i = 1; i < exportedNames.size(); i++)
                    writer.write("const %s: typeof %s\n".formatted(exportedNames.get(i).getLastName(), exportedNames.get(0).getLastName()));
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
                        "typeRoots": ["./probe/generated", "./probe/user"],
                        "target": "ES2015"
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
        compileAdditionalTypeNames();
        compileJSConfig();
        cachedJavaClasses.addAll(CapturedClasses.capturedJavaClasses);
        writeCachedEvents("cachedEvents.json", cachedEvents);
        writeCachedForgeEvents("cachedForgedEvents.json", cachedForgeEvents);
        writeCachedClasses("cachedJava.json", cachedJavaClasses);
    }

}
