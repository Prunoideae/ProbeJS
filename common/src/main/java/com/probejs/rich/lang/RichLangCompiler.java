package com.probejs.rich.lang;

import com.mojang.datafixers.util.Pair;
import com.probejs.ProbeJS;
import com.probejs.ProbePaths;
import com.probejs.compiler.formatter.formatter.special.FormatterLang;
import com.probejs.util.json.JArray;
import com.probejs.util.json.JObject;
import com.probejs.util.json.JPrimitive;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.locale.Language;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class RichLangCompiler {
    public static void compile() throws IOException {
        JArray langKeyArray = JArray.create();

        LanguageManager languageManager = Minecraft.getInstance().getLanguageManager();

        var selected = languageManager.getSelected();
        Map<String, Map<String, String>> storage = new HashMap<>();

        FormatterLang.getLangKeys(LanguageManager.DEFAULT_LANGUAGE_CODE)
                .forEach(entry ->
                        storage.computeIfAbsent(entry.getKey(), key -> new HashMap<>())
                                .put(LanguageManager.DEFAULT_LANGUAGE_CODE, entry.getValue())
                );

        if (!selected.getCode().equals(LanguageManager.DEFAULT_LANGUAGE_CODE)) {
            FormatterLang.getLangKeys(selected)
                    .forEach(entry ->
                            storage.computeIfAbsent(entry.getKey(), key -> new HashMap<>())
                                    .put(selected.getCode(), entry.getValue())
                    );
        }

        langKeyArray.addAll(
                storage.entrySet().stream().map(entry ->
                        JObject.create()
                                .add("key", JPrimitive.create(entry.getKey()))
                                .add("languages", JObject.create()
                                        .addAll(entry.getValue()
                                                .entrySet()
                                                .stream()
                                                .map(e -> new Pair<>(e.getKey(), JPrimitive.create(e.getValue())))))
                                .add("selected", JPrimitive.create(selected.getCode()))
                )
        );

        Path richFile = ProbePaths.WORKSPACE_SETTINGS.resolve("lang-keys.json");
        BufferedWriter writer = Files.newBufferedWriter(richFile);
        writer.write(ProbeJS.GSON.toJson(langKeyArray.serialize()));
        writer.close();
    }
}
