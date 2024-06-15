package moe.wolfgirl.probejs.mixins;

import moe.wolfgirl.probejs.GlobalStates;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TranslatableContents.class)
public abstract class TranslatableMixin {

    @Shadow
    @Final
    private String key;

    @Shadow
    @Final
    private String fallback;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void init(CallbackInfo ci) {
        // So we populate keys even if no client storage is present
        // I wonder if this is CPU intensive but probably not (that much)
        synchronized (GlobalStates.MIXIN_LANG_KEYS) {
            GlobalStates.MIXIN_LANG_KEYS.add(key);
            if (fallback != null) GlobalStates.MIXIN_LANG_KEYS.add(fallback);
        }
    }
}
