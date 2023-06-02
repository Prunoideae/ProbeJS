package com.probejs.recipe.component;

import com.probejs.ProbePaths;
import com.probejs.compiler.formatter.formatter.FormatterNamespace;
import com.probejs.compiler.formatter.formatter.IFormatter;
import com.probejs.util.Util;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.schema.RecipeNamespace;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//Generates fake recipe schema class with builders
public class FormatterRecipeSchema implements IFormatter {
    private final String className;
    private final RecipeSchema schema;

    public FormatterRecipeSchema(String loc, RecipeSchema schema) {
        this.schema = schema;
        this.className = Util.snakeToTitle(loc);
    }

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        ArrayList<String> lines = new ArrayList<>();

        lines.add("class %s extends %s {".formatted(className, Util.formatMaybeParameterized(schema.recipeType)));
        for (RecipeKey<?> key : schema.keys) {
            FormatterRecipeKey keyFormatter = new FormatterRecipeKey(key);
            keyFormatter.getBuilders().forEach(builder -> lines.addAll(builder.format(indent + stepIndent, stepIndent)));
        }
        lines.add("}");
        return lines;
    }

    public static IFormatter formatRecipeClasses() {
        return new FormatterNamespace("Recipes",
                RecipeNamespace.getAll().values().stream().flatMap(n -> n.entrySet().stream())
                        .map(e -> new FormatterRecipeSchema(e.getKey(), e.getValue().schema))
                        .collect(Collectors.toList())
        );
    }
}
