/**
 * @mod extendedcrafting
 */
class RecipeHolder {
    extendedcrafting: Document.ExtendedCraftingRecipes;
}

/**
 * @mod extendedcrafting
 */
class ExtendedCraftingRecipes {
    shaped_table(output: dev.latvian.mods.kubejs.item.ItemStackJS, pattern: string[], items: java.util.Map<string, dev.latvian.mods.kubejs.item.ingredient.IngredientJS>): dev.latvian.mods.kubejs.recipe.minecraft.ShapedRecipeJS;
    shapeless_table(output: dev.latvian.mods.kubejs.item.ItemStackJS, inputs: dev.latvian.mods.kubejs.item.ingredient.IngredientJS[]): dev.latvian.mods.kubejs.recipe.minecraft.ShapelessRecipeJS;
}