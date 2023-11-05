package com.probejs.recipe;

import com.probejs.jdoc.document.DocumentClass;
import com.probejs.jdoc.document.DocumentMethod;
import com.probejs.jdoc.java.ClassInfo;
import com.probejs.jdoc.property.PropertyType;
import dev.latvian.mods.kubejs.recipe.RecipesEventJS;

public class RecipeEventDocument {
    public static DocumentClass loadRecipeEventDocument() throws NoSuchMethodException {
        DocumentClass document = DocumentClass.fromJava(ClassInfo.getOrCache(RecipesEventJS.class));
        DocumentMethod getRecipes = document.methods
                .stream()
                .filter(documentMethod -> documentMethod.name.equals("getRecipes"))
                .findFirst()
                .get();
        getRecipes.returns = new PropertyType.Native("Special.DocumentedRecipes");
        return document;
    }
}
