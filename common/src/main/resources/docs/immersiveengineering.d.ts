/**
 * @mod immersiveengineering
 * @mod kubejs_immersive_engineering
 */
class RecipeHolder {
    /**
     * All recipes from IE.
     */
    readonly immersiveengineering: Document.ImmersiveEngineeringRecipes;
}

/**
 * @mod immersiveengineering
 * @mod kubejs_immersive_engineering
 */
type CropRenderType = "crop" | "stacking" | "stem" | "generic"

/**
 * @mod immersiveengineering
 * @mod kubejs_immersive_engineering
 */
type CropRender = { type: Type.CropRenderType, block: Special.Block } | Special.Block

/**
 * @mod immersiveengineering
 * @mod kubejs_immersive_engineering
 */
class ImmersiveEngineeringRecipes {

    alloy(output: dev.latvian.mods.kubejs.item.ItemStackJS, input1: dev.latvian.mods.kubejs.item.ingredient.IngredientJS, input2: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.immersiveengineering.recipe.AlloyRecipeJS;

    blast_furnace(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.immersiveengineering.recipe.BlastFurnaceRecipeJS;
    blast_furnace(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS, slag: dev.latvian.mods.kubejs.item.ItemStackJS): dev.latvian.mods.kubejs.immersiveengineering.recipe.BlastFurnaceRecipeJS;
    blast_furnace_fuel(input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.immersiveengineering.recipe.BlastFurnaceFuelRecipeJS;

    coke_oven(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.immersiveengineering.recipe.CokeOvenRecipeJS;

    garden_clothe(output: dev.latvian.mods.kubejs.item.ItemStackJS[], input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS, soil: dev.latvian.mods.kubejs.item.ItemStackJS): dev.latvian.mods.kubejs.immersiveengineering.recipe.ClocheRecipeJS
    garden_clothe(output: dev.latvian.mods.kubejs.item.ItemStackJS[], input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS, soil: dev.latvian.mods.kubejs.item.ItemStackJS, render: Type.CropRender): dev.latvian.mods.kubejs.immersiveengineering.recipe.ClocheRecipeJS

    fertilizer(input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.immersiveengineering.recipe.ClocheFertilizerRecipeJS;

    metal_press(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS, mold: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.immersiveengineering.recipe.MetalPressRecipeJS;

    arc_furnace(outputs: dev.latvian.mods.kubejs.item.ItemStackJS[], input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.immersiveengineering.recipe.ArcFurnaceRecipeJS;
    arc_furnace(outputs: dev.latvian.mods.kubejs.item.ItemStackJS[], input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS, additives: dev.latvian.mods.kubejs.item.ingredient.IngredientJS[]): dev.latvian.mods.kubejs.immersiveengineering.recipe.ArcFurnaceRecipeJS;
    arc_furnace(outputs: dev.latvian.mods.kubejs.item.ItemStackJS[], input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS, additives: dev.latvian.mods.kubejs.item.ingredient.IngredientJS[], slag: dev.latvian.mods.kubejs.item.ItemStackJS): dev.latvian.mods.kubejs.immersiveengineering.recipe.ArcFurnaceRecipeJS;

    crusher(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.immersiveengineering.recipe.CrusherRecipeJS;
    crusher(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS, secondaries: dev.latvian.mods.kubejs.item.ItemStackJS[]): dev.latvian.mods.kubejs.immersiveengineering.recipe.CrusherRecipeJS;

    sawmill(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.immersiveengineering.recipe.SawmillRecipeJS;
    sawmill(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS, secondaries: { stripping: boolean, output: dev.latvian.mods.kubejs.item.ItemStackJS }[]): dev.latvian.mods.kubejs.immersiveengineering.recipe.SawmillRecipeJS;
    sawmill(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS, secondaries: { stripping: boolean, output: dev.latvian.mods.kubejs.item.ItemStackJS }[], stripped: dev.latvian.mods.kubejs.item.ItemStackJS): dev.latvian.mods.kubejs.immersiveengineering.recipe.SawmillRecipeJS;

}