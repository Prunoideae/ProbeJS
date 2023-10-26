package com.probejs.mixins;

import com.probejs.compiler.DocCompiler;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.rhino.NativeJavaClass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScriptManager.class)
public class OnJavaMixin {
    @Inject(method = "loadJavaClass", at = @At("RETURN"), remap = false)
    public void loadJavaClass(String name0, boolean warn, CallbackInfoReturnable<NativeJavaClass> cir) {
        DocCompiler.CapturedClasses.capturedJavaClasses.add(cir.getReturnValue().getClassObject());
    }
}
