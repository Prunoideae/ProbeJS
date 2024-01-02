package com.probejs.util.special_docs;

import com.probejs.jdoc.document.DocumentClass;
import com.probejs.jdoc.document.DocumentMethod;
import com.probejs.jdoc.java.ClassInfo;
import com.probejs.jdoc.property.PropertyType;
import com.probejs.util.PlatformSpecial;
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
