/**
 * @mod ars_nouveau
 */
class RecipeHolder {
    ars_nouveau: Document.ArsNouveauRecipes;
}

/**
 * @mod ars_nouveau
 */
class ArsNouveauRecipes {
    enchanting_apparatus(output: dev.latvian.mods.kubejs.item.ItemStackJS, reagent: dev.latvian.mods.kubejs.item.ingredient.IngredientJS, inputs: dev.latvian.mods.kubejs.item.ingredient.IngredientJS[]): dev.latvian.mods.kubejs.recipe.mod.ArsNouveauEnchantingApparatusRecipeJS;

    enchantment(enchantment: string, level: number, inputs: dev.latvian.mods.kubejs.item.ingredient.IngredientJS[]): dev.latvian.mods.kubejs.recipe.mod.ArsNouveauEnchantmentRecipeJS;
    enchantment(enchantment: string, level: number, inputs: dev.latvian.mods.kubejs.item.ingredient.IngredientJS[], mana: number): dev.latvian.mods.kubejs.recipe.mod.ArsNouveauEnchantmentRecipeJS;

    glyph_recipe(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ItemStackJS, tier: string): dev.latvian.mods.kubejs.recipe.mod.ArsNouveauEnchantmentRecipeJS;
}