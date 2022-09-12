package com.probejs.compiler;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import com.probejs.ProbeJS;
import com.probejs.ProbePaths;
import com.probejs.event.CapturedEvent;
import com.probejs.formatter.ClassResolver;
import com.probejs.formatter.NameResolver;
import com.probejs.formatter.SpecialTypes;
import com.probejs.formatter.formatter.FormatterNamespace;
import com.probejs.formatter.formatter.IFormatter;
import com.probejs.formatter.formatter.jdoc.FormatterClass;
import com.probejs.formatter.formatter.jdoc.FormatterType;
import com.probejs.info.Walker;
import com.probejs.jdoc.Manager;
import com.probejs.jdoc.Serde;
import com.probejs.jdoc.document.DocumentClass;
import com.probejs.jdoc.property.PropertyComment;
import com.probejs.jdoc.property.PropertyType;
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
        writer.flush();
    }

    private static String formatJavaType(DocumentClass c) {
        var formatter = new FormatterType.Clazz(new PropertyType.Clazz(c.getName()));
        if (c.isInterface()) {
            return "declare function java(name: %s): %s;\n".formatted(
                    ProbeJS.GSON.toJson(c.getName()),
                    formatter.formatFirst()
            );
        } else {
            return "declare function java(name: %s): typeof %s;\n".formatted(
                    ProbeJS.GSON.toJson(c.getName()),
                    formatter.formatFirst()
            );
        }
    }

    public static void compileJava(Collection<DocumentClass> globalClasses) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(ProbePaths.GENERATED.resolve("java.d.ts"));
        writer.write("/// <reference path=\"./globals.d.ts\" />\n");
        for (DocumentClass c : globalClasses) {
            if (ServerScriptManager.instance.scriptManager.isClassAllowed(c.getName()))
                writer.write(formatJavaType(c));
        }
        writer.flush();
    }

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
        touchableClasses.addAll(typeMap.values().stream().map(recipeTypeJS -> recipeTypeJS.factory.get().getClass()).toList());
        bindingEvent.getConstantDumpMap().values().stream().map(DummyBindingEvent::getConstantClassRecursive).forEach(touchableClasses::addAll);
        touchableClasses.addAll(CapturedClasses.getCapturedEvents().values().stream().map(CapturedEvent::getCaptured).toList());
        touchableClasses.addAll(CapturedClasses.getCapturedRawEvents().values());
        touchableClasses.addAll(CapturedClasses.getCapturedJavaClasses());

        Walker walker = new Walker(touchableClasses);
        return walker.walk();
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

    public static String formatMaybeParameterized(DocumentClass clazz) {
        if (clazz.getGenerics().isEmpty()) {
            return new FormatterType.Clazz(new PropertyType.Clazz(clazz.getName())).formatFirst();
        } else {
            return new FormatterType.Parameterized(
                    new PropertyType.Parameterized(
                            new PropertyType.Clazz(clazz.getName()),
                            Collections.nCopies(clazz.getGenerics().size(), new PropertyType.Clazz(Object.class.getName())))
            ).formatFirst();
        }
    }

    public static void compileEvents(Map<String, CapturedEvent> cachedEvents, Map<String, Class<?>> cachedForgeEvents, Map<String, DocumentClass> globalClasses) throws IOException {
        cachedEvents.putAll(CapturedClasses.capturedEvents);
        cachedForgeEvents.putAll(CapturedClasses.capturedRawEvents);
        BufferedWriter writer = Files.newBufferedWriter(ProbePaths.GENERATED.resolve("events.d.ts"));

        writer.write("/// <reference path=\"./globals.d.ts\" />\n");
        writer.write("/// <reference path=\"./registries.d.ts\" />\n");

        Set<CapturedEvent> wildcards = new HashSet<>();
        for (Map.Entry<String, CapturedEvent> entry : cachedEvents.entrySet()) {
            CapturedEvent capturedEvent = entry.getValue();
            String id = capturedEvent.getId();
            Class<?> event = capturedEvent.getCaptured();
            String sub = capturedEvent.getSub();
            String name = id + (sub == null ? "" : ("." + sub));
            if (capturedEvent.hasSub())
                wildcards.add(capturedEvent);
            DocumentClass clazz = globalClasses.get(event.getName());
            if (clazz == null)
                continue;
            PropertyComment mergedComment = clazz.getMergedComment();
            mergedComment.getLines().addAll(getAdditionalEventComments(capturedEvent));
            writer.write(String.join("\n", mergedComment.formatLines(0)) + "\n");
            writer.write("declare function onEvent(name: %s, handler: (event: %s) => void);\n".formatted(ProbeJS.GSON.toJson(name), formatMaybeParameterized(clazz)));
        }

        Set<String> writtenWildcards = new HashSet<>();
        for (CapturedEvent wildcard : wildcards) {
            String id = ProbeJS.GSON.toJson(wildcard.getId());
            id = id.substring(1, id.length() - 1);
            if (writtenWildcards.contains(id))
                continue;
            writtenWildcards.add(id);
            Class<?> event = wildcard.getCaptured();
            DocumentClass clazz = globalClasses.get(event.getName());
            if (clazz == null)
                continue;
            PropertyComment mergedComment = clazz.getMergedComment();
            mergedComment.getLines().addAll(getAdditionalEventComments(wildcard));
            writer.write("declare function onEvent(name: `%s`, handler: (event: %s) => void);\n".formatted(id, formatMaybeParameterized(clazz)));
        }

        for (Map.Entry<String, Class<?>> entry : cachedForgeEvents.entrySet()) {
            String name = entry.getKey();
            Class<?> event = entry.getValue();
            writer.write("declare function onForgeEvent(name: %s, handler: (event: %s) => void);\n".formatted(ProbeJS.GSON.toJson(name), formatMaybeParameterized(event)));
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
            writer.write("declare const %s: %s;\n".formatted(name, Objects.requireNonNull(Serde.getValueFormatter(Serde.getValueProperty(value))).formatFirst()));
        }
        writer.flush();
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

    private static List<String> getAdditionalEventComments(CapturedEvent event) {
        List<String> comments = new ArrayList<>();
        if (!event.getScriptTypes().isEmpty()) {
            comments.add("");
            comments.add("The event fires on: %s.".formatted(event.getFormattedTypeString()));
        }

        comments.add("");
        comments.add("The event is %scancellable.".formatted(event.isCancellable() ? "" : "**not** "));
        return comments;
    }

    private static void writeMergedConfig(Path path, String config) throws IOException {
        JsonObject updates = ProbeJS.GSON.fromJson(config, JsonObject.class);
        JsonObject original = Files.exists(path) ? ProbeJS.GSON.fromJson(Files.newBufferedReader(path), JsonObject.class) : new JsonObject();
        updates.entrySet().forEach((entry) -> original.add(entry.getKey(), entry.getValue()));
        JsonWriter jsonWriter = ProbeJS.GSON_WRITER.newJsonWriter(Files.newBufferedWriter(path));
        jsonWriter.setIndent("    ");
        ProbeJS.GSON_WRITER.toJson(original, JsonObject.class, jsonWriter);
        jsonWriter.flush();
    }

    private static void exportClasses(List<DocumentClass> documents, Path path) throws IOException {
        JsonArray classes = new JsonArray();
        documents.forEach(document -> classes.add(document.serialize()));
        BufferedWriter writer = Files.newBufferedWriter(path);
        JsonWriter jsonWriter = ProbeJS.GSON_WRITER.newJsonWriter(writer);
        jsonWriter.setIndent("    ");
        ProbeJS.GSON_WRITER.toJson(classes, JsonArray.class, jsonWriter);
        jsonWriter.flush();
    }

    private static void exportSerializedClasses(List<DocumentClass> documents, List<DocumentClass> mergedDocuments) throws IOException {
        exportClasses(documents, KubeJSPaths.EXPORTED.resolve("javaClasses.json"));
        exportClasses(mergedDocuments, KubeJSPaths.EXPORTED.resolve("mergedClasses.json"));
    }

    public static void compile() throws IOException {
        DummyBindingEvent bindingEvent = new DummyBindingEvent(ServerScriptManager.instance.scriptManager);
        Map<ResourceLocation, RecipeTypeJS> typeMap = new HashMap<>();
        RegisterRecipeHandlersEvent recipeEvent = new RegisterRecipeHandlersEvent(typeMap);

        KubeJSPlugins.forEachPlugin(plugin -> plugin.addRecipes(recipeEvent));
        KubeJSPlugins.forEachPlugin(plugin -> plugin.addBindings(bindingEvent));

        //Fetch all cached classes
        Map<String, CapturedEvent> cachedEvents = readCachedEvents("cachedEvents.json");
        Map<String, Class<?>> cachedForgeEvents = readCachedForgeEvents("cachedForgeEvents.json");
        Set<Class<?>> cachedJavaClasses = readCachedClasses("cachedJava.json");
        Set<Class<?>> cachedClasses = new HashSet<>();
        cachedEvents.values().forEach(v -> cachedClasses.add(v.getCaptured()));
        cachedClasses.addAll(cachedForgeEvents.values());
        cachedClasses.addAll(cachedJavaClasses);
        cachedClasses.addAll(RegistryCompiler.getRegistryClasses());

        //Fetch all classes
        Set<Class<?>> globalClasses = DocCompiler.fetchClasses(typeMap, bindingEvent, cachedClasses);
        globalClasses.removeIf(c -> ClassResolver.skipped.contains(c));
        bindingEvent.getClassDumpMap().forEach((s, c) -> NameResolver.putResolvedName(c, s));
        SpecialTypes.processFunctionalInterfaces(globalClasses);
        SpecialTypes.processEnums(globalClasses);

        //Load and merge documents
        List<DocumentClass> documents = Manager.loadJavaClasses(globalClasses);
        //Marks up native java classes
        //documents.forEach(document -> document.addProperty(new PropertyComment("@nativeJava")));
        List<DocumentClass> modDocs = Manager.loadModDocuments();
        List<DocumentClass> userDocs = Manager.loadUserDocuments();
        @SuppressWarnings("unchecked")
        Map<String, DocumentClass> mergedDocsMap = Manager.mergeDocuments(documents, modDocs, userDocs);
        List<DocumentClass> mergedDocs = mergedDocsMap.values().stream().toList();
        NameResolver.priorSortClasses(mergedDocs).forEach(NameResolver::resolveName);

        //Compile things
        exportSerializedClasses(documents, mergedDocs);
        compileGlobal(mergedDocs);
        RegistryCompiler.compileRegistries();
        compileEvents(cachedEvents, cachedForgeEvents, mergedDocsMap);
        compileConstants(bindingEvent);
        compileJava(documents);
        compileAdditionalTypeNames();
        RawCompiler.compileRaw();
        compileJSConfig();
        compileVSCodeConfig();
        cachedJavaClasses.addAll(CapturedClasses.capturedJavaClasses);
        SchemaCompiler.compile(mergedDocs);
        DocCompiler.writeCachedEvents("cachedEvents.json", cachedEvents);
        DocCompiler.writeCachedForgeEvents("cachedForgedEvents.json", cachedForgeEvents);
        DocCompiler.writeCachedClasses("cachedJava.json", cachedJavaClasses);
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
}
