/**
* @mod mekanism
* @mod kubejs_mekanism
*/
class RecipeHolder {
    /**
     * All recipes from Mekanism.
     */
    readonly mekanism: Document.MekanismRecipes;
}

/**
* @mod mekanism
* @mod kubejs_mekanism
*/
type GasStack = { gas: Special.Gas, amount: number }

/**
* @mod mekanism
* @mod kubejs_mekanism
*/
type InfusionStack = { infuse_type: Special.InfuseType, amount: number }

/**
* @mod mekanism
* @mod kubejs_mekanism
*/
class MekanismRecipes {

    chemical_infusing(output: Type.GasStack, input1: Type.GasStack, input2: Type.GasStack): dev.latvian.kubejs.mekanism.recipe.ChemicalInfusingRecipeJS;

    combining(output: dev.latvian.mods.kubejs.item.ItemStackJS, input1: dev.latvian.mods.kubejs.item.ingredient.IngredientJS, input2: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.kubejs.mekanism.recipe.CombiningRecipeJS;

    dissolution(output: Type.GasStack, input: Type.GasStack, addition: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.kubejs.mekanism.recipe.CrystallizingRecipeJS;

    metallurgic_infusing(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.kubejs.mekanism.recipe.MetallurgicInfusingRecipeJS;
    metallurgic_infusing(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS, infusion: Type.InfusionStack): dev.latvian.kubejs.mekanism.recipe.MetallurgicInfusingRecipeJS;

    sawing(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.kubejs.mekanism.recipe.SawingRecipeJS;
    sawing(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS, bonus: dev.latvian.mods.kubejs.item.ItemStackJS): dev.latvian.kubejs.mekanism.recipe.SawingRecipeJS;
}