package com.probejs.recipe.component;

import com.probejs.ProbePaths;
import com.probejs.compiler.formatter.formatter.FormatterNamespace;
import com.probejs.compiler.formatter.formatter.IFormatter;
import com.probejs.util.Util;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.schema.RecipeNamespace;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaType;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//Generates fake recipe schema class with builders
public class FormatterRecipeSchema implements IFormatter {
    private final String className;
    private final String mod;
    private final RecipeSchema schema;

    public FormatterRecipeSchema(String mod, String loc, RecipeSchema schema) {
        this.schema = schema;
        this.mod = Util.snakeToTitle(mod);
        this.className = Util.snakeToTitle(loc);
    }

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        ArrayList<String> lines = new ArrayList<>();

        lines.add("class %s extends %s {".formatted(className + mod, Util.formatMaybeParameterized(schema.recipeType)));
        for (RecipeKey<?> key : schema.keys) {
            FormatterRecipeKey keyFormatter = new FormatterRecipeKey(key);
            keyFormatter.getBuilders().forEach(builder -> lines.addAll(builder.format(indent + stepIndent, stepIndent)));
        }
        lines.add("}");
        return lines;
    }

    public static IFormatter formatRecipeClasses() {
        List<IFormatter> recipeFormatters = new ArrayList<>();

        for (Map.Entry<String, RecipeNamespace> entry : RecipeNamespace.getAll().entrySet()) {
            String key = entry.getKey();
            RecipeNamespace namespace = entry.getValue();
            for (Map.Entry<String, RecipeSchemaType> e : namespace.entrySet()) {
                String loc = e.getKey();
                RecipeSchemaType value = e.getValue();
                recipeFormatters.add(new FormatterRecipeSchema(key, loc, value.schema));
            }
        }
        return new FormatterNamespace("Recipes", recipeFormatters);
    }
}
