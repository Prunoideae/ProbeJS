package moe.wolfgirl.specials.special.recipe;

import moe.wolfgirl.docs.formatter.formatter.IFormatter;
import moe.wolfgirl.jdoc.property.PropertyComment;
import moe.wolfgirl.specials.special.recipe.component.FormatterRecipeKey;
import moe.wolfgirl.util.Util;
import dev.latvian.mods.kubejs.recipe.schema.JsonRecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeConstructor;
import dev.latvian.mods.kubejs.recipe.schema.RecipeNamespace;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaType;
import dev.latvian.mods.kubejs.recipe.schema.minecraft.SpecialRecipeSchema;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import net.minecraft.resources.ResourceLocation;

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
                        "Special.Recipes.%s%s".formatted(Util.snakeToTitle(recipeName), Util.snakeToTitle(namespace.name))
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
            //Collect all recipe serializers' ids, so they're a set of all available mods
            Set<String> serializerIds = RegistryInfo.RECIPE_SERIALIZER.getVanillaRegistry()
                    .keySet()
                    .stream()
                    .map(ResourceLocation::getNamespace)
                    .collect(Collectors.toSet());

            ArrayList<String> lines = new ArrayList<>();
            lines.add("%sclass DocumentedRecipes {".formatted(" ".repeat(indent)));
            for (Map.Entry<String, RecipeNamespace> entry : RecipeNamespace.getAll().entrySet()) {
                String name = entry.getKey();
                // Skip all namespaces that are not from mods as registration of recipe is decoupled from the mod
                if (!serializerIds.contains(name))
                    continue;
                RecipeNamespace namespace = entry.getValue();
                FormatterRecipe formatter = new FormatterRecipe(name, namespace);
                var formattedRecipes = formatter.format(indent + stepIndent, stepIndent);
                // Also skip all namespaces that have no recipes
                if (formattedRecipes.size() > 2)
                    lines.addAll(formattedRecipes);
            }
            lines.add("%s}".formatted(" ".repeat(indent)));
            return lines;
        };
    }
}
