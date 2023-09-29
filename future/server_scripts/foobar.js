import { Minecraft } from "@java/net/minecraft/client/Minecraft";
import { StartupEvent } from "@kubejs/eventgroups";

StartupEvent.create("foobar", mc => {
    cb(mc, "apple")
    EvalError
})

/**
 * 
 * @param {Minecraft} mc 
 * @param {Special.Item} name
 */
function cb(mc, name) {
    mc.languageManager
}
