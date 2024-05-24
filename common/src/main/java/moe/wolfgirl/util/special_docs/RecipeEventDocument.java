package moe.wolfgirl.util.special_docs;

import moe.wolfgirl.jdoc.document.DocumentClass;
import moe.wolfgirl.jdoc.document.DocumentMethod;
import moe.wolfgirl.jdoc.java.ClassInfo;
import moe.wolfgirl.jdoc.property.PropertyType;
import moe.wolfgirl.util.PlatformSpecial;
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
