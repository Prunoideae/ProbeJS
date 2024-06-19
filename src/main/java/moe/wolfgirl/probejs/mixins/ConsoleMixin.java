package moe.wolfgirl.probejs.mixins;

import dev.latvian.mods.kubejs.util.ConsoleJS;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ConsoleJS.class, remap = false)
public class ConsoleMixin {
}
