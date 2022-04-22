/**
 * @target dev.latvian.mods.kubejs.item.ItemLeftClickEventJS
 * Fired when player left clicked air on client side.
 */
class ItemLeftClickEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.entity.LivingEntityHurtEventJS
 * Fired when a living entity is hurt.
 * 
 * If cancelled, the attack will do no effect.
 */
class LivingEntityHurtEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.level.ExplosionEventJS$Post
 * Fired when an explosion occurred in the level.
 * 
 * This event is not cancellable.
 */
class ExplosionEventJS$Post {

}

/**
 * @target dev.latvian.mods.kubejs.level.ExplosionEventJS$Pre
 * Fired when an explosion is about to happen in the level.
 * 
 * If cancelled, the explosion will not occur.
 */
class ExplosionEventJS$Pre {

}

/**
 * @target dev.latvian.mods.kubejs.entity.forge.CheckLivingEntitySpawnEventJS
 * Fired when checking a place for the entity to spawn.
 * 
 * The entity will not spawn if the event is cancelled.
 */
class CheckLivingEntitySpawnEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.item.forge.ItemDestroyedEventJS
 * Fired when an item on player's hand is destroyed, mostly due to out of durability.
 * 
 * This event is not cancellable.
 */
class ItemDestroyedEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.item.ItemPickupEventJS
 * Fired when an item is about to be picked up by the player.
 */
class ItemPickupEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.player.InventoryEventJS
 * Fired when the player:
 * - opens the inventory
 * - closes the inventory
 */
class InventoryEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.block.BlockBreakEventJS
 * Fired when a block is broken by player. This will not be fired by other means, like TNT.
 * 
 * On fabric, the experience returned is always `null`.
 */
class BlockBreakEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.item.ItemTossEventJS
 * Fired when the player tossed an item.
 */
class ItemTossEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.player.InventoryChangedEventJS
 * Fired when the inventory of player is changed.
 */
class InventoryChangedEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.block.BlockPlaceEventJS
 * Fired when a block is placed. 
 * 
 * Note that the entity can be `null`, if the placer is a dispenser, etc.
 */
class BlockPlaceEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.level.gen.WorldgenAddEventJS
 * Fired when you need to remove worldgen.
 */
class WorldgenAddEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.level.gen.WorldgenRemoveEventJS
 * Fired when you need to remove worldgen.
 */
class WorldgenRemoveEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.entity.forge.LivingEntityDropsEventJS
 * Fired when the drops of an entity is determined.
 */
class LivingEntityDropsEventJS {
    /**
     * The list of item drops to be summoned.
     * 
     * Changing this will result in modification of drops.
     * 
     * However, it is advised to use .addDrop() to add a drop.
     */
    getDrops(): java.util.List<dev.latvian.mods.kubejs.entity.ItemEntityJS>;
}

/**
 * @target dev.latvian.mods.kubejs.item.ItemTooltipEventJS
 * Fired to register special handlers for tooltips on items.
 */
class ItemTooltipEventJS {

    /**
     * @modify item dev.latvian.mods.kubejs.item.ingredient.IngredientJS
     * @modify text net.minecraft.network.chat.Component | net.minecraft.network.chat.Component[]
     * 
     * Adds a line (or lines) to the end of tooltip.
     */
    add(item: java.lang.Object, text: java.lang.Object): void;

    /**
     * @modify item dev.latvian.mods.kubejs.item.ingredient.IngredientJS
     * @modify handler (itemstack: Internal.ItemStackJS, advanced: boolean, lines: Internal.List<string>) => void
     * Adds a callback to the item, will be invoked when the tooltip is rendering.
     */
    addAdvanced(item: java.lang.Object, handler: dev.latvian.mods.kubejs.item.ItemTooltipEventJS$StaticTooltipHandlerFromJS): void;
}

/**
 * @target dev.latvian.mods.kubejs.loot.BlockLootEventJS
 * Fired when the block loot table is registering.
 */
class BlockLootEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.loot.EntityLootEventJS
 * Fired when the entity loot table is registering.
 */
class EntityLootEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.loot.GenericLootEventJS
 * Fired when the generic loot table is registering.
 * Note that this is unused.
 */
class GenericLootEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.loot.FishingLootEventJS
 * Fired when the fishing loot table is registering.
 */
class FishingLootEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.loot.GiftLootEventJS
 * Fired when the gift loot table (cat, villager, etc.) is registering.
 */
class GiftLootEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.loot.ChestLootEventJS
 * Fired when the chest loot table is registering.
 */
class ChestLootEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.net.NetworkEventJS
 * Fired when the server received data from kubejs.
 * 
 * Can be specified with sub ids for looking up specific channel.
 */
class NetworkEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.client.ClientEventJS
 * Fired when the client is initalizing.
 */
class ClientEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.client.ClientLoggedInEventJS
 * Fired by different tag when the client is:
 * - logged in
 * - logged out
 */
class ClientLoggedInEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.level.SimpleLevelEventJS
 * Fired by different tag when the level:
 * - loads
 * - ticks
 * - unloads
 */
class SimpleLevelEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.server.ServerEventJS
 * Fired by different tag when the server:
 * - loads
 * - ticks
 * - unloads
 */
class ServerEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.block.BlockModificationEventJS
 * Fired when you need to modify properties of a block.
 */
class BlockModificationEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.item.ItemModificationEventJS
 * Fired when you need to modify properties of an item.
 */
class ItemModificationEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.event.StartupEventJS
 * Fired by different tag when the game:
 * - is initializing
 * - is post-initializing
 */
class StartupEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.entity.LivingEntityDeathEventJS
 * Fired when a living entity is about to die.
 * 
 * If the entity's HP <= 0, the entity will fall and get removed.
 * 
 * If the event is not cancelled, the entity will die and drop loots.
 */
class LivingEntityDeathEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.player.SimplePlayerEventJS
 * Fired by different tag everytime when a player:
 * - ticks
 * - logged in
 * - logged out
 */
class SimplePlayerEventJS {

}
/**
 * @target dev.latvian.mods.kubejs.entity.EntitySpawnedEventJS
 * Fired on an entity is spawned.
 */
class EntitySpawnedEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.client.DebugInfoEventJS
 * Fired on every tick the debug info is rendered.
 */
class DebugInfoEventJS {

}
/**
 * @target dev.latvian.mods.kubejs.script.data.DataPackEventJS
 * Fired when you need to add some datapack json to the server.
 * 
 * Low priority event is fired first, as they will be overriden later.
 * 
 * And vise versa for high priority events.
 */
class DataPackEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.server.TagEventJS
 * Fired when you need to add, change tags of things.
 * 
 * However, due to the type erasure of Java, Probe can not determine what 
 * can be used here, please refer to the tag string itself.
 */
class TagEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.block.BlockLeftClickEventJS
 * Fired when player clicked a non-air, non-fluid block.
 * 
 * This event is called only once.
 */
class BlockLeftClickEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.client.ClientTickEventJS
 * Fired when ticking on client side.
 */
class ClientTickEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.item.ItemEntityInteractEventJS
 * Fired when player right clicks on an Entity.
 *
 * Note that both main hand and off hand will be called if not cancelled.
 *
 * On cancellation, Result.SUCCESS is returned, thus making player swing his arm.
 */
class ItemEntityInteractEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.block.BlockRightClickEventJS
 * Fired when player right clicks on an Block that is not fluid.
 *
 * Note that both main hand and off hand will be called if not cancelled.
 *
 * On cancellation, Result.SUCCESS is returned, thus making player swing his arm.
 */
class BlockRightClickEventJS {

}

/**
 * @target dev.latvian.mods.kubejs.item.ItemRightClickEventJS
 * Fired when player right clicks with item on hand.
 *
 * Note that this will only be fired when hand has item.
 *
 * On cancellation, Result.SUCCESS is returned, thus making player swing his arm.
 */
class ItemRightClickEventJS {

}
