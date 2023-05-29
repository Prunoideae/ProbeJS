package com.probejs.recipe;

import com.probejs.compiler.formatter.formatter.IFormatter;
import com.probejs.compiler.formatter.formatter.jdoc.FormatterType;
import com.probejs.jdoc.Serde;
import com.probejs.jdoc.property.PropertyComment;
import com.probejs.jdoc.property.PropertyType;
import com.probejs.recipe.desc.Description;
import com.probejs.recipe.desc.DescriptionRegistry;
import com.probejs.recipe.desc.impl.DescriptionOptional;
import com.probejs.util.Util;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.schema.RecipeConstructor;
import dev.latvian.mods.kubejs.recipe.schema.RecipeNamespace;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaType;

import java.util.*;
import java.util.stream.Collectors;

public class FormatterRecipe implements IFormatter {
    private final String name;
    private final RecipeNamespace namespace;

    public FormatterRecipe(String name, RecipeNamespace namespace) {
        this.name = name;
        this.namespace = namespace;
    }

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        /*
         * Format the recipe as object. I don't want to add another 100 stub classes in the dump.
         *
         * Only the Internal.DocumentedRecipes will be used.
         *
         * Example:
         *
         * class DocumentedRecipes {
         * |-  minecraft: {
         * |       crafting_shaped(...): ShapedRecipeJS
         * |-  }
         * }
         */

        ArrayList<String> lines = new ArrayList<>();

        lines.add("%s %s: {".formatted(" ".repeat(indent), name));
        for (Map.Entry<String, RecipeSchemaType> entry : namespace.entrySet()) {
            String recipeName = entry.getKey();
            RecipeSchemaType recipe = entry.getValue();
            for (RecipeConstructor recipeConstructor : recipe.schema.constructors().values()) {
                PropertyComment comments = new PropertyComment();
                String method = "%s%s(%s):%s".formatted(
                        " ".repeat(indent + stepIndent),
                        recipeName,
                        Arrays.stream(recipeConstructor.keys())
                                // Ensure that the keys are sorted by index for weird situations
                                .sorted(Comparator.comparingInt(RecipeKey::index))
                                .map(key -> {
                                    String paramName = key.name();
                                    Description description = DescriptionRegistry.getDescription(key.component().description());
                                    if (description instanceof DescriptionOptional optional) {
                                        IFormatter defaultValue = optional.getDefaultValueFormatter();
                                        if (defaultValue != null) {
                                            comments.add("@param %s defaults to `%s`".formatted(paramName, defaultValue.formatFirst()));
                                        }
                                        paramName += "?";
                                    }
                                    PropertyType<?> type = description.describeType();
                                    FormatterType<?> formatter = Serde.getTypeFormatter(type);
                                    return "%s: %s".formatted(paramName, formatter.formatFirst());
                                }).collect(Collectors.joining(", ")),
                        //TODO: wait for the return type of schema to be implemented
                        Util.formatMaybeParameterized(RecipeJS.class)
                );
                if (!comments.isEmpty())
                    lines.addAll(comments.formatLines(indent + stepIndent));
                lines.add(method);
            }
        }
        lines.add("%s}".formatted(" ".repeat(indent)));
        return lines;
    }

    public static IFormatter formatRecipeNamespaces() {
        return (indent, stepIndent) -> {
            ArrayList<String> lines = new ArrayList<>();
            lines.add("%s class DocumentedRecipes {".formatted(" ".repeat(indent)));
            for (Map.Entry<String, RecipeNamespace> entry : RecipeNamespace.getAll().entrySet()) {
                String name = entry.getKey();
                RecipeNamespace namespace = entry.getValue();
                FormatterRecipe formatter = new FormatterRecipe(name, namespace);
                lines.addAll(formatter.format(indent + stepIndent, stepIndent));
            }
            lines.add("%s}".formatted(" ".repeat(indent)));
            return lines;
        };
    }
}
