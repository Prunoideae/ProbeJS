package moe.wolfgirl.probejs.specials;

import moe.wolfgirl.probejs.ProbeCommands;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.docs.formatter.formatter.IFormatter;
import moe.wolfgirl.probejs.jdoc.java.ClassInfo;
import moe.wolfgirl.probejs.specials.special.*;
import moe.wolfgirl.probejs.specials.special.recipe.FormatterRecipe;
import moe.wolfgirl.probejs.specials.special.recipe.component.FormatterRecipeSchema;
import moe.wolfgirl.probejs.util.PlatformSpecial;
import moe.wolfgirl.probejs.util.RLHelper;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ClassFilter;
import dev.latvian.mods.kubejs.util.KubeJSPlugins;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SpecialCompiler {
    public static final List<IFormatter> specialCompilers = new ArrayList<>();

    private static List<FormatterTag> getTagFormatters() {
        List<FormatterTag> formatters = new ArrayList<>();
        ProbeCommands.COMMAND_LEVEL.registryAccess().registries().forEach(entry -> {
            ResourceKey<?> key = entry.key();
            Registry<?> registry = entry.value();
            formatters.add(new FormatterTag(RLHelper.finalComponentToTitle(key.location().getPath()) + "Tag", registry));
        });
        return formatters;
    }

    public static List<IFormatter> compileSpecial() {
        List<IFormatter> formatters = new ArrayList<>();
        formatters.add(new FormatterMod());
        formatters.add(new FormatterIngredient());
        formatters.add(new FormatterAdvancement());
        formatters.add(new FormatterRecipeId());
        formatters.add(new FormatterLang());
        formatters.add(new FormatterLootTable());
        formatters.add(new FormatterTexture());
        formatters.add(new FormatterModel());
        formatters.add(new FormatterComponents());
        formatters.addAll(getTagFormatters());
        formatters.add(FormatterRecipe.formatRecipeNamespaces());
        formatters.add(FormatterRecipeSchema.formatRecipeClasses());

        ClassFilter filter = KubeJSPlugins.createClassFilter(ScriptType.STARTUP);
        formatters.add((i, d) -> List.of("%stype JavaClass = %s".formatted(
                " ".repeat(i),
                ClassInfo.CLASS_CACHE.values()
                        .stream()
                        .map(ClassInfo::getName)
                        .filter(filter::isAllowed)
                        .map(ProbeJS.GSON::toJson)
                        .collect(Collectors.joining(" | "))
        )));
        formatters.addAll(PlatformSpecial.INSTANCE.get().getPlatformFormatters());
        formatters.addAll(specialCompilers);
        return formatters;
    }


    public static List<String> compileTagEvents() {
        return List.of();
    }
}
