package com.probejs.forge.mixin;

import com.probejs.ProbeConfig;
import com.probejs.ProbeJS;
import com.probejs.plugin.CapturedClasses;
import dev.latvian.mods.kubejs.forge.BuiltinKubeJSForgePlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BuiltinKubeJSForgePlugin.class)
public class OnForgeEventMixin {

    @Inject(method = "onPlatformEvent", at = @At("HEAD"), remap = false)
    private static void onPlatformEvent(BindingsEvent event, Object[] objects, CallbackInfoReturnable<Object> callback) {
        if (objects.length >= 2 && objects[0] instanceof CharSequence && !ProbeConfig.INSTANCE.disabled) {
            try {
                Class<?> forName = Class.forName(objects[0].toString());
                CapturedClasses.capturedRawEvents.put(objects[0].toString(), forName);
            } catch (Exception e) {
                ProbeJS.LOGGER.warn("Failed to get class instance of %s".formatted(objects[0]));
            }
        }
    }
}
