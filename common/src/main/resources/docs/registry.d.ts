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
 * @target dev.latvian.mods.kubejs.item.ItemBuilder
 */
class ItemBuilder {
    /**
     * @modify attribute net.minecraft.world.entity.ai.attributes.Attribute
     */
    modifyAttribute(attribute: net.minecraft.resources.ResourceLocation, identifier: java.lang.String, d: double, operation: net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation): dev.latvian.mods.kubejs.item.ItemBuilder;
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