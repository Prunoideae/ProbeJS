/**
 * @mod extendedcrafting
 */
class RecipeHolder {
    readonly extendedcrafting: Document.ExtendedCraftingRecipes;
}

/**
 * @mod extendedcrafting
 */
class ExtendedCraftingRecipes {
    shaped_table(output: dev.latvian.mods.kubejs.item.ItemStackJS, pattern: string[], items: { [key: string]: Internal.IngredientJS_ }): dev.latvian.mods.kubejs.recipe.minecraft.ShapedRecipeJS;
    shapeless_table(output: dev.latvian.mods.kubejs.item.ItemStackJS, inputs: dev.latvian.mods.kubejs.item.ingredient.IngredientJS[]): dev.latvian.mods.kubejs.recipe.minecraft.ShapelessRecipeJS;
}