package com.probejs.compiler.formatter.formatter.special;

import com.probejs.ProbeJS;
import com.probejs.compiler.formatter.formatter.IFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.locale.Language;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FormatterLang implements IFormatter {

    private static final Set<String> ALL_KEYS = new HashSet<>();

    public static synchronized void modifyKeys(Consumer<Set<String>> access) {
        synchronized (ALL_KEYS) {
            access.accept(ALL_KEYS);
        }
    }

    public static Set<String> getAllKeys() {
        HashSet<String> keys;
        synchronized (ALL_KEYS) {
            keys = new HashSet<>(ALL_KEYS);
        }
        return keys;
    }

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
                .stream()
                // if language is not found, MC will throw an exception, so we check each language instead
                // though I soon realized that I can just catch the exception, but I'm too lazy to change it
                .filter(lang -> lang.getCode().equals(language))
                .findFirst()
                .map(FormatterLang::getLangKeys)
                .orElse(Stream.empty());
    }

    public static Stream<Map.Entry<String, String>> getLangKeys(LanguageInfo language) {
        Minecraft mc = Minecraft.getInstance();
        LanguageManager manager = mc.getLanguageManager();
        LanguageInfo english = manager.getLanguage(LanguageManager.DEFAULT_LANGUAGE_CODE);
        List<LanguageInfo> languages = language.getCode().equals(LanguageManager.DEFAULT_LANGUAGE_CODE)
                ? List.of(english)
                : List.of(english, language);

        ClientLanguage clientLanguage = ClientLanguage.loadFrom(mc.getResourceManager(), languages);

        Map<String, String> storage = clientLanguage.storage;

        if (!ALL_KEYS.isEmpty()) {
            storage = new HashMap<>(storage);
            for (String key : getAllKeys()) {
                if (!storage.containsKey(key)) {
                    storage.put(key, key);
                }
            }
        }

        return storage.entrySet().stream();
    }
}
