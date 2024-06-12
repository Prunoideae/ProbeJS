package moe.wolfgirl.probejs.util.special_docs;

import moe.wolfgirl.probejs.jdoc.document.DocumentClass;
import moe.wolfgirl.probejs.jdoc.document.DocumentMethod;
import moe.wolfgirl.probejs.jdoc.java.ClassInfo;
import moe.wolfgirl.probejs.jdoc.property.PropertyType;
import moe.wolfgirl.probejs.util.PlatformSpecial;
import dev.latvian.mods.kubejs.recipe.RecipesEventJS;

public class RecipeEventDocument {
    public static DocumentClass loadRecipeEventDocument() throws NoSuchMethodException {
        DocumentClass recipeEventJS = DocumentClass.fromJava(ClassInfo.getOrCache(RecipesEventJS.class));
        DocumentMethod getRecipes = PlatformSpecial.getMethodDocument(RecipesEventJS.class, "getRecipes");
        getRecipes.returns = new PropertyType.Native("Special.DocumentedRecipes");
        recipeEventJS.methods.add(getRecipes);
        return recipeEventJS;
    }
}
