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

import java.util.EnumSet;

@Mixin(EventJS.class)
public class OnEventMixin {

    @Inject(method = "post(Ldev/latvian/mods/kubejs/script/ScriptType;Ljava/lang/String;)Z", at = @At("HEAD"), remap = false)
    private void post(ScriptType t, String id, CallbackInfoReturnable<Boolean> returns) {
        EventJS event = (EventJS) ((Object) this);
        if (!ProbeConfig.INSTANCE.noAggressiveProbing && !CapturedClasses.isEventIgnored(this.getClass())) {
            if (!CapturedClasses.capturedEvents.containsKey(id)) {
                CapturedClasses.capturedEvents.put(id, new CapturedEvent(event.getClass(), id, null, EnumSet.of(t), event.canCancel()));
            } else {
                CapturedClasses.capturedEvents.get(id).getScriptTypes().add(t);
            }
        }
    }

    @Inject(method = "post(Ldev/latvian/mods/kubejs/script/ScriptType;Ljava/lang/String;Ljava/lang/String;)Z", at = @At("HEAD"), remap = false)
    private void post(ScriptType t, String id, String sub, CallbackInfoReturnable<Boolean> returns) {
        EventJS event = (EventJS) ((Object) this);
        if (!ProbeConfig.INSTANCE.noAggressiveProbing && !CapturedClasses.isEventIgnored(this.getClass()))
            if (!CapturedClasses.capturedEvents.containsKey(id)) {
                CapturedClasses.capturedEvents.put(id + "." + sub, new CapturedEvent(event.getClass(), id, sub, EnumSet.of(t), event.canCancel()));
            } else {
                CapturedClasses.capturedEvents.get(id).getScriptTypes().add(t);
            }
    }

}
