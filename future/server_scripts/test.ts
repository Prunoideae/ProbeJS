import { Minecraft } from "@java/net/minecraft/client/Minecraft";
import { StartupEvent } from "@kubejs/eventgroups";
import { sus } from "@scripts/foobar";

StartupEvent.create("foobar", mc => {
    test(mc)
})

function test(mc: Minecraft) {
    let a: Special.Item = "apple";
}

sus()