package com.probejs.mixins;

import com.probejs.ProbeConfig;
import com.probejs.plugin.CapturedClasses;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.rhino.NativeJavaClass;
import dev.latvian.mods.rhino.Scriptable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScriptManager.class)
public class OnJavaMixin {
    @Inject(method = "loadJavaClass", at = @At("RETURN"), remap = false)
    public void loadJavaClass(BindingsEvent event, String name0, boolean warn, CallbackInfoReturnable<NativeJavaClass> cir) {
        if (!ProbeConfig.INSTANCE.noAggressiveProbing)
            CapturedClasses.capturedJavaClasses.add(cir.getReturnValue().getClassObject());
    }
}
