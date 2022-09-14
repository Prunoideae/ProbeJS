/**
 * @mod thermal
 * @mod kubejs_thermal
 */
class RecipeHolder {
    /**
     * All recipes from Thermal Expansion
     */
    readonly thermal: Document.ThermalRecipes;
}

/**
 * @mod thermal
 * @mod kubejs_thermal
 */
type TEMixedOutput = dev.latvian.mods.kubejs.item.ItemStackJS | dev.latvian.mods.kubejs.fluid.FluidStackJS

/**
 * @mod thermal
 * @mod kubejs_thermal
 */
type TEMixedInput = dev.latvian.mods.kubejs.item.ingredient.IngredientJS | dev.latvian.mods.kubejs.fluid.FluidStackJS

/**
 * @mod thermal
 * @mod kubejs_thermal
 */
class ThermalRecipes {
    sawmill(outputs: Type.SelfOrArray<dev.latvian.mods.kubejs.item.ItemStackJS>, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.thermal.BasicThermalRecipeJS;
    pulverizer(outputs: Type.SelfOrArray<dev.latvian.mods.kubejs.item.ItemStackJS>, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.thermal.BasicThermalRecipeJS;

    centrifuge(outputs: Type.SelfOrArray<Type.TEMixedOutput>, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.thermal.BasicThermalRecipeJS;
    pyrolyzer(outputs: Type.SelfOrArray<Type.TEMixedOutput>, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.thermal.BasicThermalRecipeJS;

    press(outputs: Type.SelfOrArray<Type.TEMixedOutput>, input: Type.SelfOrArray<dev.latvian.mods.kubejs.item.ingredient.IngredientJS>): dev.latvian.mods.kubejs.thermal.BasicThermalRecipeJS;
    refinery(outputs: Type.SelfOrArray<Type.TEMixedOutput>, input: dev.latvian.mods.kubejs.fluid.FluidStackJS): dev.latvian.mods.kubejs.thermal.BasicThermalRecipeJS;

    crucible(output: dev.latvian.mods.kubejs.fluid.FluidStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.thermal.BasicThermalRecipeJS;

    smelter(outputs: Type.SelfOrArray<dev.latvian.mods.kubejs.item.ItemStackJS>, input: Type.SelfOrArray<dev.latvian.mods.kubejs.item.ingredient.IngredientJS>): dev.latvian.mods.kubejs.thermal.BasicThermalRecipeJS;

    brewer(output: dev.latvian.mods.kubejs.fluid.FluidStackJS, input: Type.SelfOrArray<Type.TEMixedInput>): dev.latvian.mods.kubejs.thermal.BasicThermalRecipeJS;
    bottler(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: Type.SelfOrArray<Type.TEMixedInput>): dev.latvian.mods.kubejs.thermal.BasicThermalRecipeJS;
    insolator(output: Type.SelfOrArray<dev.latvian.mods.kubejs.item.ItemStackJS>, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.thermal.InsolatorRecipeJS;
    pulverizer_catalyst(input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.thermal.CatalystRecipeJS;
    smelter_catalyst(input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.thermal.CatalystRecipeJS;
    insolator_catalyst(input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.thermal.CatalystRecipeJS;
    stirling_fuel(input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.thermal.FuelRecipeJS;
    compression_fuel(input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.thermal.FuelRecipeJS;
    magmatic_fuel(input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.thermal.FuelRecipeJS;
    numismatic_fuel(input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.thermal.FuelRecipeJS;
    lapidary_fuel(input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.thermal.FuelRecipeJS;
}

