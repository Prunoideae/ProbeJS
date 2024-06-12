package moe.wolfgirl.probejs.specials.special;

import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.docs.formatter.formatter.IFormatter;
import dev.architectury.platform.Platform;

import java.util.List;
import java.util.stream.Collectors;

public class FormatterMod implements IFormatter {
    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        return List.of("%stype Mod = %s".formatted(" ".repeat(indent), Platform.getModIds().stream().map(ProbeJS.GSON::toJson).collect(Collectors.joining(" | "))));
    }
}
