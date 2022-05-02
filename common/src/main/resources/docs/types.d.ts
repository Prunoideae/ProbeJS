/**
 * @target dev.latvian.mods.kubejs.fluid.FluidStackJS
 * @assign dev.architectury.fluid.FluidStack
 * @assign net.minecraft.world.level.material.Fluid
 * @assign com.google.gson.JsonObject
 * @assign {fluid: ResourceLocation_, amount?: number, nbt?: Internal.CompoundTag_}
 * @assign "" | "-" | "empty" | "minecraft:empty"
 * @assign `${string} ${number}`
 */
class FluidStackJS {

}

/**
 * @target dev.latvian.mods.kubejs.block.state.BlockStatePredicate
 * @assign string
 * @assign dev.latvian.mods.kubejs.block.state.BlockStatePredicate[]
 * @assign {or?: Internal.BlockStatePredicate_[], not?: Internal.BlockStatePredicate_}
 * @assign net.minecraft.world.level.block.Block
 * @assign net.minecraft.world.level.block.state.BlockState
 * @assign RegExp
 */
class BlockStatePredicate {

}
/**
 * @target net.minecraft.nbt.CompoundTag
 * @assign string
 * @assign {[string]: string | number | boolean | Internal.CompoundTag_}
 */
class CompoundTag {

}

/**
 * @target dev.latvian.mods.kubejs.text.Text
 * @assign string
 */
class Text {

}

/**
 * @target net.minecraft.network.chat.Component
 * @assign dev.latvian.mods.kubejs.text.Text
 */
class Component {

}

/**
 * @target net.minecraft.world.item.ItemStack
 * @assign dev.latvian.mods.kubejs.item.ItemStackJS
 */
class ItemStack {

}

/**
 * @target net.minecraft.world.entity.EntityType
 * @assign string
 */
class EntityType {

}

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
* @assign net.minecraft.world.item.Item
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
 * @assign RegExp
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
 * @assign {exact?: boolean, not?: Internal.RecipeFilter_, or?: Internal.RecipeFilter_[], id?: string | `/${string}/`, type?: string, group?: string, mod?: string, input?: Internal.IngredientJS_, output?: Internal.IngredientJS_}
 */
class RecipeFilter {

}