package com.probejs.compiler;

import com.probejs.ProbePaths;
import com.probejs.document.DocumentClass;
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
import com.probejs.info.type.TypeInfoClass;
import com.probejs.plugin.CapturedClasses;
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

    public static void compileGlobal(DummyBindingEvent bindingEvent, Set<Class<?>> globalClasses) throws IOException {

        bindingEvent.getClassDumpMap().forEach((s, c) -> NameResolver.putResolvedName(c, s));
        NameResolver.resolveNames(NameResolver.priorSortClasses(globalClasses));

        BufferedWriter writer = Files.newBufferedWriter(ProbePaths.GENERATED.resolve("globals.d.ts"));
        BufferedWriter writeJDoc = Files.newBufferedWriter(ProbePaths.GENERATED.resolve("globals-new.d.ts"));

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

    public static void compile() throws IOException {
        DummyBindingEvent bindingEvent = new DummyBindingEvent(ServerScriptManager.instance.scriptManager);
        Map<ResourceLocation, RecipeTypeJS> typeMap = new HashMap<>();
        RegisterRecipeHandlersEvent recipeEvent = new RegisterRecipeHandlersEvent(typeMap);

        KubeJSPlugins.forEachPlugin(plugin -> plugin.addRecipes(recipeEvent));
        KubeJSPlugins.forEachPlugin(plugin -> plugin.addBindings(bindingEvent));

        Map<String, CapturedEvent> cachedEvents = DocCompiler.readCachedEvents("cachedEvents.json");
        Map<String, Class<?>> cachedForgeEvents = DocCompiler.readCachedForgeEvents("cachedForgeEvents.json");
        Set<Class<?>> cachedJavaClasses = DocCompiler.readCachedClasses("cachedJava.json");
        Set<Class<?>> cachedClasses = new HashSet<>();

        cachedEvents.values().forEach(v -> cachedClasses.add(v.getCaptured()));
        cachedClasses.addAll(cachedForgeEvents.values());
        cachedClasses.addAll(cachedJavaClasses);
        cachedClasses.addAll(RegistryCompiler.getRegistryClasses());

        Set<Class<?>> globalClasses = DocCompiler.fetchClasses(typeMap, bindingEvent, cachedClasses);
        globalClasses.removeIf(c -> ClassResolver.skipped.contains(c));
        SpecialTypes.processFunctionalInterfaces(globalClasses);
        compileGlobal(bindingEvent, globalClasses);
        RegistryCompiler.compileRegistries();
        DocCompiler.compileEvents(cachedEvents, cachedForgeEvents);
        DocCompiler.compileConstants(bindingEvent);
        compileJava(globalClasses);
        compileAdditionalTypeNames();
        RawCompiler.compileRaw();
        DocCompiler.compileJSConfig();
        DocCompiler.compileVSCodeConfig();
        cachedJavaClasses.addAll(CapturedClasses.capturedJavaClasses);
        DocCompiler.writeCachedEvents("cachedEvents.json", cachedEvents);
        DocCompiler.writeCachedForgeEvents("cachedForgedEvents.json", cachedForgeEvents);
        DocCompiler.writeCachedClasses("cachedJava.json", cachedJavaClasses);
    }

}
