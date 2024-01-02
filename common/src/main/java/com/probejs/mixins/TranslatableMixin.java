package com.probejs.mixins;

import com.probejs.compiler.formatter.formatter.special.FormatterLang;
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

    @Inject(method = "<init>(Ljava/lang/String;)V", at = @At("RETURN"))
    public void init(String string, CallbackInfo ci) {
        FormatterLang.modifyKeys(keys -> keys.add(key));
    }

    @Inject(method = "<init>(Ljava/lang/String;[Ljava/lang/Object;)V", at = @At("RETURN"))
    public void initVar(String string, Object[] objects, CallbackInfo ci) {
        FormatterLang.modifyKeys(keys -> keys.add(key));
    }
}
