/**
 * @mod dankstorage
 */
 class RecipeHolder {
    readonly dankstorage: Document.DankStorageRecipes;
}

/**
 * @mod dankstorage
 */
class DankStorageRecipes {
    upgrade(output: dev.latvian.mods.kubejs.item.ItemStackJS, pattern: string[], items: { [key: string]: dev.latvian.mods.kubejs.item.ingredient.IngredientJS }): dev.latvian.mods.kubejs.recipe.minecraft.ShapedRecipeJS;
}