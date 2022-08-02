package com.probejs.formatter.formatter.special;

import com.probejs.ProbeJS;
import com.probejs.formatter.formatter.IFormatter;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.locale.Language;

import java.util.List;
import java.util.stream.Collectors;

public class FormatterLang implements IFormatter {
    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        Language language = Language.getInstance();
        if (language instanceof ClientLanguage clientLanguage) {
            return List.of("%stype LangKey = %s".formatted(" ".repeat(indent),
                    clientLanguage.storage
                            .keySet()
                            .stream()
                            .filter(s -> !(s.startsWith("_") || s.startsWith("$")))
                            .map(ProbeJS.GSON::toJson)
                            .collect(Collectors.joining(" | "))
            ));
        }
        return List.of();
    }
}
