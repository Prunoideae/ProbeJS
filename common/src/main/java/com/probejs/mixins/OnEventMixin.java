package com.probejs.mixins;

import com.probejs.ProbeConfig;
import com.probejs.event.CapturedEvent;
import com.probejs.plugin.CapturedClasses;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.script.ScriptType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EventJS.class)
public class OnEventMixin {

    @Inject(method = "post(Ldev/latvian/mods/kubejs/script/ScriptType;Ljava/lang/String;)Z", at = @At("HEAD"), remap = false)
    private void post(ScriptType t, String id, CallbackInfoReturnable<Boolean> returns) {
        if (!ProbeConfig.INSTANCE.mixinDisabled && !CapturedClasses.isEventIgnored(this.getClass()))
            CapturedClasses.capturedEvents.put(id, new CapturedEvent(((EventJS) ((Object) this)).getClass(), id, null));
    }

    @Inject(method = "post(Ldev/latvian/mods/kubejs/script/ScriptType;Ljava/lang/String;Ljava/lang/String;)Z", at = @At("HEAD"), remap = false)
    private void post(ScriptType t, String id, String sub, CallbackInfoReturnable<Boolean> returns) {
        if (!ProbeConfig.INSTANCE.mixinDisabled && !CapturedClasses.isEventIgnored(this.getClass()))
            CapturedClasses.capturedEvents.put(id + "." + sub, new CapturedEvent(((EventJS) ((Object) this)).getClass(), id, sub));
    }

}
