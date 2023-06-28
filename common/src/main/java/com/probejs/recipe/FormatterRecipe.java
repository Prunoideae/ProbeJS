package com.probejs.recipe;

import com.probejs.compiler.formatter.formatter.IFormatter;
import com.probejs.jdoc.property.PropertyComment;
import com.probejs.recipe.component.FormatterRecipeKey;
import com.probejs.util.Util;
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
         * |       crafting_shaped(...): Recipe.CraftingShaped
         * |-  }
         * }
         */

        ArrayList<String> lines = new ArrayList<>();

        lines.add("%s%s: {".formatted(" ".repeat(indent), name));
        for (Map.Entry<String, RecipeSchemaType> entry : namespace.entrySet()) {
            String recipeName = entry.getKey();
            RecipeSchemaType recipe = entry.getValue();
            for (RecipeConstructor recipeConstructor : recipe.schema.constructors().values()) {
                PropertyComment comments = new PropertyComment();
                String method = "%s%s(%s):%s".formatted(
                        " ".repeat(indent + stepIndent),
                        recipeName,
                        Arrays.stream(recipeConstructor.keys())
                                .filter(key -> !key.excluded)
                                .map(FormatterRecipeKey::new)
                                .peek(key -> comments.merge(key.getComments(recipe)))
                                .map(IFormatter::formatFirst)
                                .collect(Collectors.joining(", ")),
                        "Special.Recipes.%s".formatted(Util.snakeToTitle(recipeName))
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
            lines.add("%sclass DocumentedRecipes {".formatted(" ".repeat(indent)));
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
