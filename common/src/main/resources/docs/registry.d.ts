/**
 * @target dev.latvian.mods.kubejs.misc.MobEffectBuilder
 */
class MobEffectBuilder {
    /**
    * @modify attribute net.minecraft.world.entity.ai.attributes.Attribute
    */
    modifyAttribute(attribute: net.minecraft.resources.ResourceLocation, identifier: java.lang.String, d: double, operation: net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation): dev.latvian.mods.kubejs.misc.MobEffectBuilder;
}

/**
 * @target dev.latvian.mods.kubejs.block.BlockBuilder
 */
class BlockBuilder {
    /**
     * @modify tex Special.Texture
     */
    textureAll(tex: java.lang.String): dev.latvian.mods.kubejs.block.BlockBuilder;
    /**
     * @modify tex Special.Texture
     */
    texture(id: java.lang.String, tex: java.lang.String): dev.latvian.mods.kubejs.block.BlockBuilder;
    /**
     * @modify tex Special.Texture
     */
    textureSide(direction: net.minecraft.core.Direction, tex: java.lang.String): dev.latvian.mods.kubejs.block.BlockBuilder;
}

/**
 * @target dev.latvian.mods.kubejs.item.ItemBuilder
 */
class ItemBuilder {
    /**
     * @modify attribute net.minecraft.world.entity.ai.attributes.Attribute
     */
    modifyAttribute(attribute: net.minecraft.resources.ResourceLocation, identifier: java.lang.String, d: double, operation: net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation): dev.latvian.mods.kubejs.item.ItemBuilder;
    /**
     * @modify tex Special.Texture
     */
    texture(tex: java.lang.String): dev.latvian.mods.kubejs.item.ItemBuilder;
    /**
     * @modify tex Special.Texture
     */
    texture(key: java.lang.String, tex: java.lang.String): dev.latvian.mods.kubejs.item.ItemBuilder;
}

/**
 * @target dev.latvian.mods.kubejs.item.custom.RecordItemJS$Builder
 */
class RecordItem {
    /**
     * @modify s net.minecraft.sounds.SoundEvent
     */
    song(s: net.minecraft.resources.ResourceLocation): dev.latvian.mods.kubejs.item.custom.RecordItemJS$Builder;
}

/**
 * @target dev.latvian.mods.kubejs.BuilderBase
 */
class BuilderBase {
    /**
     * @modify key Special.LangKey
     */
    translationKey(key: java.lang.String): dev.latvian.mods.kubejs.BuilderBase<T>
}