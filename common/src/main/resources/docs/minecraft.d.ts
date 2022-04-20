/**
 * @target net.minecraft.resources.ResourceLocation
 * @assign string
 */
class ResourceLocation {

}

/**
 * @target dev.latvian.mods.kubejs.recipe.ingredientaction.IngredientActionFilter
 * @assign number
 * @assign dev.latvian.mods.kubejs.item.ingredient.IngredientJS
 * @assign string
 * @assign {item: Internal.IngredientJS_, index?: number}
 */
class IngredientActionFilter {

}

/**
 * @target dev.latvian.mods.kubejs.misc.EnchantmentBuilder
 */
class EnchantmentBuilder {
    /**
     * Set the callback when the enchanted item hits an entity.
     * 
     * WARN: This callback will ALWAYS be called TWICE, for both main hand and off hand.
     * This is an annoying code logic from Vanilla Minecraft. There's no way KubeJS can
     * change this.
     */
    doPostAttack(i: dev.latvian.mods.kubejs.misc.EnchantmentBuilder$PostFunction): dev.latvian.mods.kubejs.misc.EnchantmentBuilder;
}

/**
* @target dev.latvian.mods.kubejs.item.ItemStackJS
* @assign `${string}:${string}`
*/
class ItemStackJS {

}

/**
 * @target dev.latvian.mods.kubejs.item.ingredient.IngredientJS
 * @assign `#${string}`
 * @assign dev.latvian.mods.kubejs.item.ItemStackJS
 * @assign dev.latvian.mods.kubejs.fluid.FluidStackJS
 * @assign "*"
 * @assign `@${string}`
 * @assign `%${string}`
 * @assign `/${string}/`
 * @assign net.minecraft.world.item.crafting.Ingredient
 * @assign dev.latvian.mods.kubejs.item.ingredient.IngredientJS[]
 * @assign {type: "forge:nbt", item: `${string}:${string}` | {item: string, count?: number}, nbt: object}
 * @assign {item: Internal.ItemStackJS_, count?: number}
 * @assign {fluid: Internal.FluidStackJS_}
 * @assign {value: object}
 * @assign {ingredient: object}
 * Represents an Ingredient, which can match one or multiple ItemStacks.
 * 
 * Can be casted from several object, which has different usages.
 * 
 * If you want to specify nbt to check in ItemStack, use either Item.of() or {type: "forge:nbt"}.
 * 
 * Using {item: ItemStackJS} will NOT preserve NBT in any form.
 */
class IngredientJS {

}

/**
 * @target dev.latvian.mods.kubejs.recipe.filter.RecipeFilter
 * @assign dev.latvian.mods.kubejs.recipe.filter.RecipeFilter[]
 * @assign {exact?: boolean, not?: Internal.RecipeFilter_, or?: Internal.RecipeFilter_, id?: string | `/${string}/`, type?: string, group?: string, mod?: string, input?: Internal.IngredientJS_, output?: Internal.IngredientJS_}
 */
class RecipeFilter {

}

/**
* @target dev.latvian.mods.kubejs.recipe.RecipeEventJS
*/
class RecipeEventJS {
    /**
     * Holds all the recipes collected from documents.
     * @returns Document.RecipeHolder
     */
    getRecipes(): java.util.Map<java.lang.String, java.lang.Object>;

    /**
     * Remove recipe(s) by given recipe filter.
     * 
     * Please note that some of the recipes are not removable from KubeJS side.
     * 
     * An example is tipped arrows, because they do not have actual recipe registered in datapack.
     */
    remove(filter: dev.latvian.mods.kubejs.recipe.filter.RecipeFilter): int;

    /**
     * @hidden
     */
    campfireCooking: dev.latvian.mods.kubejs.recipe.RecipeFunction;
    /**
     * @hidden
     */
    smithing: dev.latvian.mods.kubejs.recipe.RecipeFunction;
    /**
     * @hidden
     */
    stonecutting: dev.latvian.mods.kubejs.recipe.RecipeFunction;
    /**
     * @hidden
     */
    shaped: dev.latvian.mods.kubejs.recipe.RecipeFunction;
    /**
     * @hidden
     */
    smoking: dev.latvian.mods.kubejs.recipe.RecipeFunction;
    /**
     * @hidden
     */
    shapeless: dev.latvian.mods.kubejs.recipe.RecipeFunction;
    /**
     * @hidden
     */
    smelting: dev.latvian.mods.kubejs.recipe.RecipeFunction;
    /**
     * @hidden
     */
    blasting: dev.latvian.mods.kubejs.recipe.RecipeFunction;

    /**
     * Adds a smelting recipe to Minecraft.
     * 
     * This is used by Furnaces.
     */
    smelting(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.minecraft.CookingRecipeJS;
    /**
     * Adds a smelting recipe to Minecraft.
     * 
     * This is used by Smokers.
     */
    smoking(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.minecraft.CookingRecipeJS;
    /**
     * Adds a smelting recipe to Minecraft.
     * 
     * This is used by Blast Furnaces.
     */
    blasting(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.minecraft.CookingRecipeJS;
    /**
     * Adds a shaped crafting recipe.
     */
    shaped(output: dev.latvian.mods.kubejs.item.ItemStackJS, pattern: dev.latvian.mods.kubejs.item.ingredient.IngredientJS[][]): dev.latvian.mods.kubejs.recipe.minecraft.ShapedRecipeJS;
    /**
     * Adds a shaped crafting recipe.
     */
    shaped(output: dev.latvian.mods.kubejs.item.ItemStackJS, pattern: string[], items: {[string]: Internal.IngredientJS_}): dev.latvian.mods.kubejs.recipe.minecraft.ShapedRecipeJS;
    /**
     * Adds a shapeless crafting recipe.
     */
    shapeless(output: dev.latvian.mods.kubejs.item.ItemStackJS, inputs: dev.latvian.mods.kubejs.item.ingredient.IngredientJS[]): dev.latvian.mods.kubejs.recipe.minecraft.ShapelessRecipeJS;
    /**
     * Adds a smelting recipe to Minecraft.
     * 
     * This is used by Campfire.
     */
    campfireCooking(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.minecraft.CookingRecipeJS;
    /**
     * Adds a stonecutting recipe.
     */
    stonecutting(output: dev.latvian.mods.kubejs.item.ItemStackJS, inputs: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.minecraft.StonecuttingRecipeJS;
    /**
     * Adds a smithing recipe.
     */
    smithing(output: dev.latvian.mods.kubejs.item.ItemStackJS, base: dev.latvian.mods.kubejs.item.ingredient.IngredientJS, addition: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.minecraft.SmithingRecipeJS;

}


class RecipeHolder {
    /**
     * All recipes from Minecraft.
     */
    readonly minecraft: Document.MinecraftRecipes;
}


class MinecraftRecipes {
    /**
     * Adds a smelting recipe to Minecraft.
     * 
     * This is used by Furnaces.
     */
    smelting(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.minecraft.CookingRecipeJS;
    /**
     * Adds a smelting recipe to Minecraft.
     * 
     * This is used by Smokers.
     */
    smoking(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.minecraft.CookingRecipeJS;
    /**
     * Adds a smelting recipe to Minecraft.
     * 
     * This is used by Blast Furnaces.
     */
    blasting(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.minecraft.CookingRecipeJS;
    /**
     * Adds a shaped crafting recipe.
     */
    crafting_shaped(output: dev.latvian.mods.kubejs.item.ItemStackJS, pattern: dev.latvian.mods.kubejs.item.ingredient.IngredientJS[][]): dev.latvian.mods.kubejs.recipe.minecraft.ShapedRecipeJS;
    /**
     * Adds a shaped crafting recipe.
     */
    crafting_shaped(output: dev.latvian.mods.kubejs.item.ItemStackJS, pattern: string[], items: {[string]: Internal.IngredientJS_}): dev.latvian.mods.kubejs.recipe.minecraft.ShapedRecipeJS;
    /**
     * Adds a shapeless crafting recipe.
     */
    crafting_shapeless(output: dev.latvian.mods.kubejs.item.ItemStackJS, inputs: dev.latvian.mods.kubejs.item.ingredient.IngredientJS[]): dev.latvian.mods.kubejs.recipe.minecraft.ShapelessRecipeJS;
    /**
     * Adds a smelting recipe to Minecraft.
     * 
     * This is used by Campfire.
     */
    campfire_cooking(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.minecraft.CookingRecipeJS;
    /**
     * Adds a stonecutting recipe.
     */
    stonecutting(output: dev.latvian.mods.kubejs.item.ItemStackJS, inputs: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.minecraft.StonecuttingRecipeJS;
    /**
     * Adds a smithing recipe.
     */
    smithing(output: dev.latvian.mods.kubejs.item.ItemStackJS, base: dev.latvian.mods.kubejs.item.ingredient.IngredientJS, addition: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.recipe.minecraft.SmithingRecipeJS;
}
