/**
 * @mod botanypots
 */
class RecipeHolder {
    readonly botanypots: Document.BotanyPotsRecipes;
}

/**
 * @mod botanypots
 */
class BotanyPotsRecipes {
    /**
     * @param outputs any of the `ItemStackJS`, or `{item: ItemStackJS, minRolls: number, maxRolls: number}`
     */
    crop(outputs: { item: dev.latvian.mods.kubejs.item.ItemStackJS, minRolls: number, maxRolls: number } | dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.mod.BotanyPotsCropRecipeJS;
}