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
 * @target dev.latvian.mods.kubejs.item.ItemModificationEventJS
 * Fired when you need to modify properties of an item.
 * 
 * Supports to modify:
 * 
 * - burnTime: How long the item can burn as fuel, 0 means can not.
 * - craftingRemainder: What will remain when item is used in crafting.
 * - fireResistant: Can item resist fire, lava, etc.
 * - foodProperties: What food will item be.
 * - maxStackSize: The max stacksize of the item.
 * - tier: The tier of the item.
 * - maxDamage: The durability of the item.
 * - rarity: The Rarity of the item.
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
