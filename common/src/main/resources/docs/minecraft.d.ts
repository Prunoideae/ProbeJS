/**
* @target dev.latvian.mods.kubejs.item.ItemStackJS
* @assign string
* @assign object
*/
class ItemStackJS {
}

/**
 * @target dev.latvian.mods.kubejs.item.ingredient.IngredientJS
 * @assign string
 * @assign object
 * @assign dev.latvian.mods.kubejs.item.ItemStackJS
 */
class IngredientJS {

}

/**
* @target dev.latvian.mods.kubejs.recipe.RecipeEventJS
*/
class RecipeEventJS {
    /**
     * Holds all the recipes collected from documents.
     * @returns Document.RecipeHolder
     */
    getRecipes(): java.util.Map<java.lang.String, java.lang.Object>;


    /**
     * @hidden
     */
    campfireCooking: dev.latvian.mods.kubejs.recipe.RecipeFunction;
    /**
     * @hidden
     */
    smithing: dev.latvian.mods.kubejs.recipe.RecipeFunction;
    /**
     * @hidden
     */
    stonecutting: dev.latvian.mods.kubejs.recipe.RecipeFunction;
    /**
     * @hidden
     */
    shaped: dev.latvian.mods.kubejs.recipe.RecipeFunction;
    /**
     * @hidden
     */
    smoking: dev.latvian.mods.kubejs.recipe.RecipeFunction;
    /**
     * @hidden
     */
    shapeless: dev.latvian.mods.kubejs.recipe.RecipeFunction;
    /**
     * @hidden
     */
    smelting: dev.latvian.mods.kubejs.recipe.RecipeFunction;
    /**
     * @hidden
     */
    blasting: dev.latvian.mods.kubejs.recipe.RecipeFunction;

    /**
     * Adds a smelting recipe to Minecraft.
     * 
     * This is used by Furnaces.
     */
    smelting(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.minecraft.CookingRecipeJS;
    /**
     * Adds a smelting recipe to Minecraft.
     * 
     * This is used by Smokers.
     */
    smoking(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.minecraft.CookingRecipeJS;
    /**
     * Adds a smelting recipe to Minecraft.
     * 
     * This is used by Blast Furnaces.
     */
    blasting(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.minecraft.CookingRecipeJS;

    /**
     * Adds a shaped crafting recipe.
     */
    shaped(output: dev.latvian.mods.kubejs.item.ItemStackJS, pattern: string[], items: java.util.Map<string, dev.latvian.mods.kubejs.item.ingredient.IngredientJS>): dev.latvian.mods.kubejs.recipe.minecraft.ShapedRecipeJS;
    /**
     * Adds a shapeless crafting recipe.
     */
    shapeless(output: dev.latvian.mods.kubejs.item.ItemStackJS, inputs: dev.latvian.mods.kubejs.item.ingredient.IngredientJS[]): dev.latvian.mods.kubejs.recipe.minecraft.ShapelessRecipeJS;
    /**
     * Adds a smelting recipe to Minecraft.
     * 
     * This is used by Camefire.
     */
    campfireCooking(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.minecraft.CookingRecipeJS;
    /**
     * Adds a stonecutting recipe.
     */
    stonecutting(output: dev.latvian.mods.kubejs.item.ItemStackJS, inputs: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.minecraft.StonecuttingRecipeJS;
    /**
     * Adds a smithing recipe.
     */
    smithing(output: dev.latvian.mods.kubejs.item.ItemStackJS, base: dev.latvian.mods.kubejs.item.ingredient.IngredientJS, addition: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.minecraft.SmithingRecipeJS;

}


class RecipeHolder {
    /**
     * All recipes from Minecraft.
     */
    readonly minecraft: Document.MinecraftRecipes;
}


class MinecraftRecipes {
    /**
     * Adds a smelting recipe to Minecraft.
     * 
     * This is used by Furnaces.
     */
    smelting(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.minecraft.CookingRecipeJS;
    /**
     * Adds a smelting recipe to Minecraft.
     * 
     * This is used by Smokers.
     */
    smoking(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.minecraft.CookingRecipeJS;
    /**
     * Adds a smelting recipe to Minecraft.
     * 
     * This is used by Blast Furnaces.
     */
    blasting(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.minecraft.CookingRecipeJS;

    /**
     * Adds a shaped crafting recipe.
     */
    crafting_shaped(output: dev.latvian.mods.kubejs.item.ItemStackJS, pattern: string[], items: java.util.Map<string, dev.latvian.mods.kubejs.item.ingredient.IngredientJS>): dev.latvian.mods.kubejs.recipe.minecraft.ShapedRecipeJS;
    /**
     * Adds a shapeless crafting recipe.
     */
    crafting_shapeless(output: dev.latvian.mods.kubejs.item.ItemStackJS, inputs: dev.latvian.mods.kubejs.item.ingredient.IngredientJS[]): dev.latvian.mods.kubejs.recipe.minecraft.ShapelessRecipeJS;
    /**
     * Adds a smelting recipe to Minecraft.
     * 
     * This is used by Camefire.
     */
    camefire_cooking(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.minecraft.CookingRecipeJS;
    /**
     * Adds a stonecutting recipe.
     */
    stonecutting(output: dev.latvian.mods.kubejs.item.ItemStackJS, inputs: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.minecraft.StonecuttingRecipeJS;
    /**
     * Adds a smithing recipe.
     */
    smithing(output: dev.latvian.mods.kubejs.item.ItemStackJS, base: dev.latvian.mods.kubejs.item.ingredient.IngredientJS, addition: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.minecraft.SmithingRecipeJS;
}
