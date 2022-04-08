/**
 * @mod botania
 */
class RecipeHolder {
    botania: Document.BotaniaRecipes;
}

/**
 * @mod botania
 */
class BotaniaRecipes {
    runic_altar(output: dev.latvian.mods.kubejs.item.ItemStackJS, inputs: dev.latvian.mods.kubejs.item.ingredient.IngredientJS[]): dev.latvian.mods.kubejs.recipe.mod.BotaniaRunicAltarRecipeJS;
    runic_altar(output: dev.latvian.mods.kubejs.item.ItemStackJS, inputs: dev.latvian.mods.kubejs.item.ingredient.IngredientJS[], mana: number): dev.latvian.mods.kubejs.recipe.mod.BotaniaRunicAltarRecipeJS;
}