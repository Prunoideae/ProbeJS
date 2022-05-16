/**
 * @target dev.latvian.mods.kubejs.bindings.IngredientWrapper
 */
class IngredientWrapper {
    /**
     * @modify object dev.latvian.mods.kubejs.item.ingredient.IngredientJS
     */
    static of(object: java.lang.Object): dev.latvian.mods.kubejs.item.ingredient.IngredientJS;
    /**
     * @modify object dev.latvian.mods.kubejs.item.ingredient.IngredientJS
     */
    static of(object: java.lang.Object, count: int): dev.latvian.mods.kubejs.item.ingredient.IngredientJS;
}

/**
* @target dev.latvian.mods.kubejs.recipe.RecipeEventJS
* Fired when you need to modify recipes.
*/
class RecipeEventJS {
    /**
     * Holds all the recipes collected from documents.
     * @returns Document.RecipeHolder
     */
    getRecipes(): java.util.Map<java.lang.String, java.lang.Object>;

    /**
     * Remove recipe(s) by given recipe filter.
     * 
     * Please note that some of the recipes are not removable from KubeJS side.
     * 
     * An example is tipped arrows, because they do not have actual recipe registered in datapack.
     */
    remove(filter: dev.latvian.mods.kubejs.recipe.filter.RecipeFilter): int;

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
    shaped(output: dev.latvian.mods.kubejs.item.ItemStackJS, pattern: dev.latvian.mods.kubejs.item.ingredient.IngredientJS[][]): dev.latvian.mods.kubejs.recipe.minecraft.ShapedRecipeJS;
    /**
     * Adds a shaped crafting recipe.
     */
    shaped(output: dev.latvian.mods.kubejs.item.ItemStackJS, pattern: string[], items: { [key: string]: Internal.IngredientJS_ }): dev.latvian.mods.kubejs.recipe.minecraft.ShapedRecipeJS;
    /**
     * Adds a shapeless crafting recipe.
     */
    shapeless(output: dev.latvian.mods.kubejs.item.ItemStackJS, inputs: dev.latvian.mods.kubejs.item.ingredient.IngredientJS[]): dev.latvian.mods.kubejs.recipe.minecraft.ShapelessRecipeJS;
    /**
     * Adds a smelting recipe to Minecraft.
     * 
     * This is used by Campfire.
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
    crafting_shaped(output: dev.latvian.mods.kubejs.item.ItemStackJS, pattern: dev.latvian.mods.kubejs.item.ingredient.IngredientJS[][]): dev.latvian.mods.kubejs.recipe.minecraft.ShapedRecipeJS;
    /**
     * Adds a shaped crafting recipe.
     */
    crafting_shaped(output: dev.latvian.mods.kubejs.item.ItemStackJS, pattern: string[], items: { [key: string]: Internal.IngredientJS_ }): dev.latvian.mods.kubejs.recipe.minecraft.ShapedRecipeJS;
    /**
     * Adds a shapeless crafting recipe.
     */
    crafting_shapeless(output: dev.latvian.mods.kubejs.item.ItemStackJS, inputs: dev.latvian.mods.kubejs.item.ingredient.IngredientJS[]): dev.latvian.mods.kubejs.recipe.minecraft.ShapelessRecipeJS;
    /**
     * Adds a smelting recipe to Minecraft.
     * 
     * This is used by Campfire.
     */
    campfire_cooking(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.minecraft.CookingRecipeJS;
    /**
     * Adds a stonecutting recipe.
     */
    stonecutting(output: dev.latvian.mods.kubejs.item.ItemStackJS, inputs: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.minecraft.StonecuttingRecipeJS;
    /**
     * Adds a smithing recipe.
     */
    smithing(output: dev.latvian.mods.kubejs.item.ItemStackJS, base: dev.latvian.mods.kubejs.item.ingredient.IngredientJS, addition: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.minecraft.SmithingRecipeJS;
}
