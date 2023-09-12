package com.probejs.compiler.formatter.formatter.special;

import com.probejs.ProbeJS;
import com.probejs.compiler.formatter.formatter.IFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.locale.Language;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FormatterLang implements IFormatter {
    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        if (Language.getInstance() instanceof ClientLanguage) {
            return List.of("%stype LangKey = %s".formatted(" ".repeat(indent),
                    getLangKeys(LanguageManager.DEFAULT_LANGUAGE_CODE)
                            .map(Map.Entry::getKey)
                            .map(ProbeJS.GSON::toJson)
                            .collect(Collectors.joining(" | "))
            ));
        }
        return List.of();
    }

    public static Stream<Map.Entry<String, String>> getLangKeys(String language) {
        LanguageManager manager = Minecraft.getInstance().getLanguageManager();
        return manager.getLanguages()
                .values()
                .stream()
                // if language is not found, MC will throw an exception, so we check each language instead
                // though I soon realized that I can just catch the exception, but I'm too lazy to change it
                .filter(lang -> lang.region().equals(language))
                .findFirst()
                .map(FormatterLang::getLangKeys)
                .orElse(Stream.empty());
    }

    public static Stream<Map.Entry<String, String>> getLangKeys(LanguageInfo language) {
        Minecraft mc = Minecraft.getInstance();
        LanguageManager manager = mc.getLanguageManager();
        LanguageInfo english = manager.getLanguage(LanguageManager.DEFAULT_LANGUAGE_CODE);
        List<LanguageInfo> languages = language.region().equals(LanguageManager.DEFAULT_LANGUAGE_CODE)
                ? List.of(english)
                : List.of(english, language);

        ClientLanguage clientLanguage = ClientLanguage.loadFrom(
                mc.getResourceManager(),
                languages.stream().map(LanguageInfo::name).collect(Collectors.toList()),
                english.bidirectional()
        );
        return clientLanguage.storage.entrySet().stream();
    }
}
