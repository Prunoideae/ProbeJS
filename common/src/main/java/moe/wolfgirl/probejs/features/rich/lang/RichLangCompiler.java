package moe.wolfgirl.probejs.features.rich.lang;

import com.mojang.datafixers.util.Pair;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.ProbePaths;
import moe.wolfgirl.probejs.specials.special.FormatterLang;
import moe.wolfgirl.probejs.util.json.JArray;
import moe.wolfgirl.probejs.util.json.JObject;
import moe.wolfgirl.probejs.util.json.JPrimitive;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RichLangCompiler {
    public static void compile() throws IOException {
        JArray langKeyArray = JArray.create();

        LanguageManager languageManager = Minecraft.getInstance().getLanguageManager();

        var selected = languageManager.getSelected();
        var codeRegion = selected.contains("_") ? selected.split("_")[0] : selected.substring(0, 2);
        List<LanguageInfo> sameRegionLang = languageManager.getLanguages().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(codeRegion))
                .map(Map.Entry::getValue)
                .toList();

        Map<String, Map<String, String>> storage = new HashMap<>();

        FormatterLang.getLangKeys(LanguageManager.DEFAULT_LANGUAGE_CODE)
                .forEach(entry ->
                        storage.computeIfAbsent(entry.getKey(), key -> new HashMap<>())
                                .put(languageManager.getLanguage(LanguageManager.DEFAULT_LANGUAGE_CODE).name(), entry.getValue())
                );

        if (!selected.equals(LanguageManager.DEFAULT_LANGUAGE_CODE)) {
            FormatterLang.getLangKeys(selected)
                    .forEach(entry ->
                            storage.computeIfAbsent(entry.getKey(), key -> new HashMap<>())
                                    .put(languageManager.getLanguage(selected).name(), entry.getValue())
                    );
        }

        for (LanguageInfo lang : sameRegionLang) {
            FormatterLang.getLangKeys(lang)
                    .forEach(entry ->
                            storage.computeIfAbsent(entry.getKey(), key -> new HashMap<>())
                                    .put(lang.name(), entry.getValue())
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
                                .add("selected", JPrimitive.create(languageManager.getLanguage(selected).name()))
                )
        );

        Path richFile = ProbePaths.WORKSPACE_SETTINGS.resolve("lang-keys.json");
        BufferedWriter writer = Files.newBufferedWriter(richFile);
        writer.write(ProbeJS.GSON.toJson(langKeyArray.serialize()));
        writer.close();
    }
}
