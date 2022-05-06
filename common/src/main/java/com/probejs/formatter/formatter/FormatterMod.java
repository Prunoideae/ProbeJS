package com.probejs.formatter.formatter;

import com.google.gson.Gson;
import dev.architectury.platform.Platform;

import java.util.List;
import java.util.stream.Collectors;

public class FormatterMod implements IFormatter{
    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        Gson g = new Gson();
        return List.of("%stype Mod = %s".formatted(" ".repeat(indent), Platform.getModIds().stream().map(g::toJson).collect(Collectors.joining(" | "))));
    }
}
