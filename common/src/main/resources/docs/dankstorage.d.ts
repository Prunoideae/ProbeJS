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
    upgrade(output: dev.latvian.mods.kubejs.item.ItemStackJS, pattern: string[], items: { [key: string]: Internal.IngredientJS_ }): dev.latvian.mods.kubejs.recipe.minecraft.ShapedRecipeJS;
}