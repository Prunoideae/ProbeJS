package moe.wolfgirl.probejs.mixins;

import com.google.gson.JsonNull;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.kubejs.server.ServerScriptManager;
import moe.wolfgirl.probejs.GlobalStates;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {ScriptManager.class, ServerScriptManager.class}, remap = false)
public abstract class ScriptManagerMixin {

    @Inject(method = "reload", remap = false, at = @At("HEAD"))
    public void reloadStart(CallbackInfo ci) {
        if (GlobalStates.WS_SERVER != null) {
            GlobalStates.WS_SERVER.broadcast("clear_error", JsonNull.INSTANCE);
        }
    }
}
