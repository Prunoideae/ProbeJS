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
