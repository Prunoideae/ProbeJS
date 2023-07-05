package com.probejs.recipe;

import com.probejs.ProbeJS;
import com.probejs.compiler.formatter.formatter.IFormatter;
import com.probejs.jdoc.property.PropertyComment;
import com.probejs.recipe.component.FormatterRecipeKey;
import com.probejs.util.Util;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.schema.*;
import dev.latvian.mods.kubejs.recipe.schema.minecraft.SpecialRecipeSchema;

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
            if (recipe.schema == SpecialRecipeSchema.SCHEMA)
                continue;
            if (recipe.schema == JsonRecipeSchema.SCHEMA)
                continue;
            for (RecipeConstructor recipeConstructor : recipe.schema.constructors().values()) {
                List<PropertyComment> comments = new ArrayList<>();
                String method = "%s%s(%s):%s".formatted(
                        " ".repeat(indent + stepIndent),
                        recipeName,
                        Arrays.stream(recipeConstructor.keys())
                                .filter(key -> !key.excluded)
                                .map(FormatterRecipeKey::new)
                                .peek(key -> comments.add(key.getComments(recipe)))
                                .map(IFormatter::formatFirst)
                                .collect(Collectors.joining(", ")),
                        "Special.Recipes.%s".formatted(Util.snakeToTitle(recipeName))
                );
                List<String> paramLines = new ArrayList<>();
                for (PropertyComment comment : comments) {
                    paramLines.addAll(comment.getLines());
                }
                if (!paramLines.isEmpty()) {
                    PropertyComment params = new PropertyComment(paramLines.toArray(new String[0]));
                    lines.addAll(params.formatLines(indent + stepIndent));
                }
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
