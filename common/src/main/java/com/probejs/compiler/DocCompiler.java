package com.probejs.compiler;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import com.probejs.ProbeConfig;
import com.probejs.ProbeJS;
import com.probejs.ProbePaths;
import com.probejs.compiler.formatter.ClassResolver;
import com.probejs.compiler.formatter.NameResolver;
import com.probejs.compiler.formatter.SpecialTypes;
import com.probejs.compiler.formatter.formatter.FormatterNamespace;
import com.probejs.compiler.formatter.formatter.IFormatter;
import com.probejs.compiler.formatter.formatter.jdoc.FormatterClass;
import com.probejs.jdoc.java.Walker;
import com.probejs.jdoc.Manager;
import com.probejs.jdoc.Serde;
import com.probejs.jdoc.document.DocumentClass;
import com.probejs.jdoc.property.PropertyComment;
import com.probejs.jdoc.jsgen.DocGenerationEventJS;
import com.probejs.util.PlatformSpecial;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.KubeJSPaths;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import dev.latvian.mods.kubejs.event.EventGroupWrapper;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.recipe.RecipeTypeJS;
import dev.latvian.mods.kubejs.recipe.RegisterRecipeTypesEvent;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.kubejs.server.ServerScriptManager;
import dev.latvian.mods.kubejs.util.KubeJSPlugins;
import net.minecraft.resources.ResourceLocation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class DocCompiler {

    public static void compileGlobal(Collection<DocumentClass> globalClasses) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(ProbePaths.GENERATED.resolve("globals.d.ts"));
        Multimap<String, IFormatter> namespaces = ArrayListMultimap.create();
        for (DocumentClass clazz : globalClasses) {
            FormatterClass formatter = new FormatterClass(clazz);
            NameResolver.ResolvedName resolvedName = NameResolver.getResolvedName(clazz.getName());
            if (resolvedName.getNamespace().isEmpty()) {
                writer.write(formatter.formatString(0, 4) + "\n");
                if (clazz.isInterface())
                    writer.write("declare const %s: %s;\n".formatted(resolvedName.getFullName(), resolvedName.getFullName()));
            } else {
                clazz.getConstructors().forEach(constructor -> constructor.addProperty(new PropertyComment(
                        "Internal constructor, this means that it's not valid unless you use `java()`."
                )));
                namespaces.put(resolvedName.getNamespace(), formatter.setInternal(true));
            }
        }
        namespaces.putAll("Special", SpecialCompiler.compileSpecial());

        for (String key : namespaces.keySet()) {
            Collection<IFormatter> formatters = namespaces.get(key);
            FormatterNamespace namespace = new FormatterNamespace(key, formatters);
            writer.write(namespace.formatString(0, 4) + "\n");
        }
        writer.close();
    }

    public static Set<Class<?>> readCachedClasses(String fileName) throws IOException {
        Set<Class<?>> cachedClasses = new HashSet<>();
        Path cachedClassesPath = ProbePaths.CACHE.resolve(fileName);
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
        BufferedWriter cacheWriter = Files.newBufferedWriter(ProbePaths.CACHE.resolve(fileName));
        JsonArray outJson = new JsonArray();
        for (Class<?> clazz : javaClasses) {
            outJson.add(clazz.getName());
        }
        ProbeJS.GSON.toJson(outJson, cacheWriter);
        cacheWriter.close();
    }

    public static Map<String, Class<?>> readCachedForgeEvents(String fileName) throws IOException {
        Map<String, Class<?>> cachedEvents = new HashMap<>();
        Path cachedEventPath = ProbePaths.CACHE.resolve(fileName);
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

    public static void writeCachedForgeEvents(String fileName, Map<String, Class<?>> events) throws IOException {
        BufferedWriter cacheWriter = Files.newBufferedWriter(ProbePaths.CACHE.resolve(fileName));
        JsonObject outJson = new JsonObject();
        for (Map.Entry<String, Class<?>> entry : events.entrySet()) {
            String eventName = entry.getKey();
            Class<?> eventClass = entry.getValue();
            outJson.addProperty(eventName, eventClass.getName());
        }
        ProbeJS.GSON.toJson(outJson, cacheWriter);
        cacheWriter.close();
    }

    public static Set<Class<?>> fetchClasses(Map<ResourceLocation, RecipeTypeJS> typeMap, DummyBindingEvent bindingEvent, Set<Class<?>> cachedClasses) {
        Set<Class<?>> touchableClasses = new HashSet<>(bindingEvent.getClassDumpMap().values());
        touchableClasses.addAll(cachedClasses);
        touchableClasses.addAll(typeMap.values().stream().map(recipeTypeJS -> recipeTypeJS.factory.get().getClass()).toList());
        bindingEvent.getConstantDumpMap().values().stream().map(DummyBindingEvent::getConstantClassRecursive).forEach(touchableClasses::addAll);
        touchableClasses.addAll(CapturedClasses.getCapturedRawEvents().values());
        touchableClasses.addAll(CapturedClasses.getCapturedJavaClasses());

        Walker walker = new Walker(touchableClasses);
        return walker.walk();
    }

    public static void compileConstants(DummyBindingEvent bindingEvent) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(ProbePaths.GENERATED.resolve("constants.d.ts"));
        writer.write("/// <reference path=\"./globals.d.ts\" />\n");
        for (Map.Entry<String, Object> entry : bindingEvent.getConstantDumpMap().entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof EventGroupWrapper) //Skip exported event constants
                continue;
            writer.write("declare const %s: %s;\n".formatted(name, Objects.requireNonNull(Serde.getValueFormatter(Serde.getValueProperty(value))).formatFirst()));
        }
        writer.close();
    }

    public static void compileJSConfig() throws IOException {
        writeMergedConfig(
                KubeJSPaths.DIRECTORY.resolve("jsconfig.json"),
                """
                        {
                            "compilerOptions": {
                                "lib": ["ES5", "ES2015"],
                                "typeRoots": ["./probe/generated", "./probe/user"],
                                "target": "ES2015"
                            }
                        }"""
        );
    }

    public static void compileVSCodeConfig() throws IOException {
        writeMergedConfig(
                ProbePaths.WORKSPACE_SETTINGS.resolve("settings.json"),
                """
                        {
                            "json.schemas": [
                                    {
                                        "fileMatch": [
                                            "/lang/*.json"
                                        ],
                                        "url": "./.vscode/probe.lang-schema.json"
                                    },
                                    {
                                        "fileMatch": [
                                            "/probe/docs/*.json"
                                        ],
                                        "url": "./.vscode/probe.doc-schema.json"
                                    }
                            ]
                        }
                        """
        );

    }

    public static void compileGitIgnore() throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(ProbePaths.PROBE.resolve(".gitignore"));
        writer.write("*\n");
        writer.write("*/\n");
        writer.close();
    }

    private static JsonElement mergeJsonRecursively(JsonElement first, JsonElement second) {
        if (first instanceof JsonObject firstObject && second instanceof JsonObject secondObject) {
            var result = firstObject.deepCopy();
            for (Map.Entry<String, JsonElement> entry : secondObject.entrySet()) {
                String key = entry.getKey();
                JsonElement value = entry.getValue();
                if (result.has(key)) {
                    result.add(key, mergeJsonRecursively(result.get(key), value));
                } else {
                    result.add(key, value);
                }
            }
            return result;
        }

        if (first instanceof JsonArray firstArray && second instanceof JsonArray secondArray) {
            List<JsonElement> elements = new ArrayList<>();
            for (JsonElement element : firstArray) {
                elements.add(element.deepCopy());
            }
            for (JsonElement element : secondArray) {
                int index;
                if ((index = elements.indexOf(element)) != -1) {
                    elements.set(index, mergeJsonRecursively(elements.get(index), element));
                } else {
                    elements.add(element);
                }
            }
            JsonArray result = new JsonArray();
            for (JsonElement element : elements) {
                result.add(element);
            }
            return result;
        }

        return second;
    }

    private static void writeMergedConfig(Path path, String config) throws IOException {
        JsonObject updates = ProbeJS.GSON.fromJson(config, JsonObject.class);
        JsonObject read = Files.exists(path) ? ProbeJS.GSON.fromJson(Files.newBufferedReader(path), JsonObject.class) : new JsonObject();
        if (read == null)
            read = new JsonObject();
        JsonObject original = (JsonObject) mergeJsonRecursively(read, updates);
        JsonWriter jsonWriter = ProbeJS.GSON_WRITER.newJsonWriter(Files.newBufferedWriter(path));
        jsonWriter.setIndent("    ");
        ProbeJS.GSON_WRITER.toJson(original, JsonObject.class, jsonWriter);
        jsonWriter.close();
    }

    private static void exportClasses(List<DocumentClass> documents, Path path) throws IOException {
        JsonArray classes = new JsonArray();
        documents.forEach(document -> classes.add(document.serialize()));
        BufferedWriter writer = Files.newBufferedWriter(path);
        JsonWriter jsonWriter = ProbeJS.GSON_WRITER.newJsonWriter(writer);
        jsonWriter.setIndent("    ");
        ProbeJS.GSON_WRITER.toJson(classes, JsonArray.class, jsonWriter);
        jsonWriter.close();
    }

    private static void exportSerializedClasses(List<DocumentClass> documents, List<DocumentClass> mergedDocuments) throws IOException {
        exportClasses(documents, ProbePaths.CACHE.resolve("javaClasses.json"));
        exportClasses(mergedDocuments, ProbePaths.CACHE.resolve("mergedClasses.json"));
    }

    private static DummyBindingEvent fetchBindings(ScriptManager manager) {
        DummyBindingEvent bindingEvent = new DummyBindingEvent(manager);
        KubeJSPlugins.forEachPlugin(plugin -> plugin.registerBindings(bindingEvent));
        return bindingEvent;
    }

    public static void compile(Consumer<String> sendMessage, DocGenerationEventJS event) throws IOException {
        DummyBindingEvent bindingEvent = fetchBindings(ServerScriptManager.getScriptManager())
                .merge(fetchBindings(KubeJS.getClientScriptManager()))
                .merge(fetchBindings(KubeJS.getStartupScriptManager()));
        Map<ResourceLocation, RecipeTypeJS> typeMap = new HashMap<>();
        RegisterRecipeTypesEvent recipeEvent = new RegisterRecipeTypesEvent(typeMap);

        KubeJSPlugins.forEachPlugin(plugin -> plugin.registerRecipeTypes(recipeEvent));
        KubeJSPlugins.forEachPlugin(plugin -> plugin.registerBindings(bindingEvent));

        sendMessage.accept("KubeJS plugins reloaded.");

        //Fetch all cached classes
        CapturedClasses.capturedRawEvents.putAll(readCachedForgeEvents("cachedForgeEvents.json"));
        CapturedClasses.capturedJavaClasses.addAll(readCachedClasses("cachedJava.json"));
        Set<Class<?>> cachedClasses = new HashSet<>();
        cachedClasses.addAll(EventCompiler.fetchEventClasses());
        cachedClasses.addAll(CapturedClasses.capturedRawEvents.values());
        cachedClasses.addAll(CapturedClasses.capturedJavaClasses);
        cachedClasses.addAll(RegistryCompiler.getKJSRegistryClasses());
        if (ProbeConfig.INSTANCE.allowRegistryObjectDumps)
            cachedClasses.addAll(SpecialTypes.collectRegistryClasses());

        //Fetch all classes
        sendMessage.accept("Start fetching classes...");
        Set<Class<?>> globalClasses = DocCompiler.fetchClasses(typeMap, bindingEvent, cachedClasses);
        sendMessage.accept("Classes fetched.");
        globalClasses.removeIf(c -> ClassResolver.skipped.contains(c));
        bindingEvent.getClassDumpMap().forEach((s, c) -> NameResolver.putResolvedName(c, s));
        SpecialTypes.processFunctionalInterfaces(globalClasses);
        SpecialTypes.processEnums(globalClasses);
        sendMessage.accept("Special types processed.");

        //Load and merge documents
        sendMessage.accept("Started translating Java classes to JSON intermediates...");
        List<DocumentClass> javaDocs = Manager.loadJavaClasses(globalClasses);
        //Insert some special documents to extend the function
        javaDocs.addAll(PlatformSpecial.INSTANCE.get().getPlatformDocuments(javaDocs));

        sendMessage.accept("Translation completed, started downloading docs...");
        Manager.downloadDocs();
        sendMessage.accept("Docs downloaded, started merging docs...");
        List<DocumentClass> fetchedDocs = Manager.loadFetchedClassDoc();
        List<DocumentClass> modDocs = Manager.loadModDocuments();
        List<DocumentClass> userDocs = new ArrayList<>();
        try {
            userDocs = Manager.loadUserDocuments();
        } catch (Exception e) {
            ProbeJS.LOGGER.error("Error loading User-defined docs!");
        }
        Map<String, DocumentClass> mergedDocsMap = Manager.mergeDocuments(javaDocs,
                fetchedDocs,
                modDocs, userDocs
        );

        event.getTransformers().forEach((key, transformer) -> {
            if (mergedDocsMap.containsKey(key)) {
                transformer.forEach(t -> t.accept(mergedDocsMap.get(key)));
            }
        });

        List<DocumentClass> mergedDocs = mergedDocsMap.values().stream().toList();
        sendMessage.accept("Docs merged. Started compiling...");
        NameResolver.priorSortClasses(mergedDocs).forEach(NameResolver::resolveName);

        //Add specials
        SpecialCompiler.specialCompilers.addAll(event.getSpecialFormatters());

        //Compile things
        exportSerializedClasses(javaDocs, mergedDocs);
        compileGlobal(mergedDocs);
        RegistryCompiler.compileRegistries();
        EventCompiler.initSpecialEvents();
        EventCompiler.compileEvents(mergedDocsMap);
        compileConstants(bindingEvent);
        compileAdditionalTypeNames();
        RawCompiler.compileRaw();
        compileJSConfig();
        compileVSCodeConfig();
        compileGitIgnore();

        SchemaCompiler.compile(mergedDocs);

        DocCompiler.writeCachedForgeEvents("cachedForgeEvents.json", CapturedClasses.getCapturedRawEvents());
        DocCompiler.writeCachedClasses("cachedJava.json", CapturedClasses.capturedJavaClasses);
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
                for (int i = 1; i < exportedNames.size(); i++) {
                    //FIXME: I don't know what went wrong in name resolving but I don't want to fix it later
                    if (NameResolver.resolvedPrimitives.contains(exportedNames.get(i).getLastName()))
                        continue;
                    if (exportedNames.get(0).getLastName().equals("any"))
                        continue;
                    if (exportedNames.get(0).getLastName().equals(exportedNames.get(i).getLastName()))
                        continue;
                    writer.write("const %s: typeof %s\n".formatted(exportedNames.get(i).getLastName(), exportedNames.get(0).getLastName()));
                }
            }
        }
        writer.close();
    }

    public static class CapturedClasses {

        public static Map<String, Class<?>> capturedRawEvents = new ConcurrentHashMap<>();
        public static Set<Class<?>> capturedJavaClasses = new HashSet<>();
        public static Set<Class<?>> ignoredEvents = new HashSet<>();

        static {
            ignoredEvents.add(RegistryObjectBuilderTypes.RegistryEventJS.class);
        }

        public static Map<String, Class<?>> getCapturedRawEvents() {
            return ImmutableMap.copyOf(capturedRawEvents);
        }

        public static Set<Class<?>> getCapturedJavaClasses() {
            return ImmutableSet.copyOf(capturedJavaClasses);
        }
    }
}
