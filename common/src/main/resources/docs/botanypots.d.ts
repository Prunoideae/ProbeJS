/**
 * @mod botanypots
 */
class RecipeHolder {
    botanypots: Document.BotanyPotsRecipes;
}

/**
 * @mod botanypots
 */
class BotanyPotsRecipes {
    /**
     * @param outputs any of the `ItemStackJS`, or `{item: ItemStackJS, minRolls: number, maxRolls: number}`
     */
    crop(outputs: object | dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.mod.BotanyPotsCropRecipeJS;
}