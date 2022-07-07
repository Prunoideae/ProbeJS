/**
 * @mod botanypots
 */
class RecipeHolder {
    readonly botanypots: Document.BotanyPotsRecipes;
}

/**
 * @mod botanypots
 */
type BotanyStack = dev.latvian.mods.kubejs.item.ItemStackJS;
/**
 * @mod botanypots
 */
type BotanyPotRoll = { item: Type.BotanyStack, minRolls: number, maxRolls: number };

/**
 * @mod botanypots
 */
class BotanyPotsRecipes {
    /**
     * @param outputs any of the `ItemStackJS`, or `{item: ItemStackJS, minRolls: number, maxRolls: number}`
     */
    crop(outputs: Type.BotanyPotRoll | dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.mod.BotanyPotsCropRecipeJS;
}