/**
* @mod create
* @mod kubejs_create
*/
class RecipeHolder {
    /**
     * All recipes from Create.
     */
    readonly create: Document.CreateRecipes;
}

/**
* @mod create
* @mod kubejs_create
*/
type ItemStackOrFluid = dev.latvian.mods.kubejs.item.ItemStackJS | dev.latvian.mods.kubejs.fluid.FluidStackJS;
/**
* @mod create
* @mod kubejs_create
*/
type IngredientOrFluid = dev.latvian.mods.kubejs.item.ingredient.IngredientJS | dev.latvian.mods.kubejs.fluid.FluidStackJS;

/**
* @mod create
* @mod kubejs_create
*/
class CreateRecipes {
    /**
     * Creates a recipe for Crushing Wheels.
     * 
     * Specifying chances on outputs will make them output with chance.
     */
    crushing(outputs: dev.latvian.mods.kubejs.item.ItemStackJS[], input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.create.ProcessingRecipeJS;
    /**
     * Creates a recipe for Millstone.
     * 
     * Specifying chances on outputs will make them output with chance.
     */
    milling(outputs: dev.latvian.mods.kubejs.item.ItemStackJS[], input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.create.ProcessingRecipeJS;
    /**
     * Creates a recipe for Compacting.
     */
    compacting(output: Type.ItemStackOrFluid, inputs: Type.IngredientOrFluid[]): dev.latvian.mods.kubejs.create.ProcessingRecipeJS;
    /**
     * Creates a recipe for Mixing.
     */
    mixing(output: Type.ItemStackOrFluid, inputs: Type.IngredientOrFluid[]): dev.latvian.mods.kubejs.create.ProcessingRecipeJS;
    /**
     * Creates a recipe for Pressing.
     * 
     * Pressing uses Depot or Belt as container, and can only have 1 item slot as input.
     * 
     * Pressing is available as an Assembly step.
     */
    pressing(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.create.ProcessingRecipeJS;
    /**
     * Creates a recipe for Deploying.
     * 
     * Deploying is available as an Assembly step.
     */
    deploying(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.create.ProcessingRecipeJS;
    /**
     * Creates a recipe for Cutting.
     * 
     * Cutting is available as an Assembly step.
     */
    cutting(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.create.ProcessingRecipeJS;
    /**
     * Creates a recipe for Filling.
     * 
     * Filling is available as an Assembly step.
     */
    filling(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: Type.IngredientOrFluid[]): dev.latvian.mods.kubejs.create.ProcessingRecipeJS;
    /**
     * Creates a recipe for Sequenced Assembly.
     * 
     * The sequnce must use recipes which is available for Assembly.
     */
    sequenced_assembly(output: dev.latvian.mods.kubejs.item.ItemStackJS[], input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS, sequence: dev.latvian.mods.kubejs.create.ProcessingRecipeJS[]): dev.latvian.mods.kubejs.create.SequencedAssemblyRecipeJS;
    /**
     * Creates a recipe for Splashing.
     */
    splashing(output: dev.latvian.mods.kubejs.item.ItemStackJS[], input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.create.ProcessingRecipeJS;
    /**
     * Creates a recipe for Haunting.
     */
    haunting(output: dev.latvian.mods.kubejs.item.ItemStackJS[], input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.create.ProcessingRecipeJS;
    /**
     * Creates a recipe for Sandpaper Polishing.
     */
    sandpaper_polishing(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.create.ProcessingRecipeJS;
    /**
     * Creates a recipe for Mechanical Crafting.
     */
    mechanical_crafting(output: dev.latvian.mods.kubejs.item.ItemStackJS, pattern: string[], items: java.util.Map<string, dev.latvian.mods.kubejs.item.ingredient.IngredientJS>): dev.latvian.mods.kubejs.create.ProcessingRecipeJS;

    /**
     * Creates a recipe for Emptying.
     */
    emptying(output: Type.ItemStackOrFluid[], input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.create.ProcessingRecipeJS;
}
