package moe.wolfgirl.specials.special.recipe.component;

import moe.wolfgirl.docs.formatter.formatter.FormatterNamespace;
import moe.wolfgirl.docs.formatter.formatter.IFormatter;
import moe.wolfgirl.util.Util;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.schema.RecipeNamespace;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//Generates fake recipe schema class with builders
public class FormatterRecipeSchema implements IFormatter {
    private final String className;
    private final String mod;
    private final RecipeSchema schema;

    public FormatterRecipeSchema(String mod, String loc, RecipeSchema schema) {
        this.schema = schema;
        this.mod = Util.snakeToTitle(mod);
        this.className = Util.pathToTitle(loc);
    }

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        ArrayList<String> lines = new ArrayList<>();

        lines.add("class %s extends %s {".formatted(className + mod, Util.formatMaybeParameterized(schema.recipeType)));
        for (RecipeKey<?> key : schema.keys) {
            FormatterRecipeKey keyFormatter = new FormatterRecipeKey(key);
            lines.addAll(keyFormatter.getBuilder().format(indent + stepIndent, stepIndent));
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
