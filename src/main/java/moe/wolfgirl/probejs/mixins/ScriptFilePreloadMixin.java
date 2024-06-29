package moe.wolfgirl.probejs.mixins;

import dev.latvian.mods.kubejs.script.ScriptFile;
import dev.latvian.mods.kubejs.script.ScriptFileInfo;
import dev.latvian.mods.kubejs.script.ScriptPack;
import moe.wolfgirl.probejs.lang.transformer.KubeJSScript;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.List;


@Mixin(value = ScriptFile.class, remap = false)
public class ScriptFilePreloadMixin {

    @Shadow(remap = false)
    public String[] lines;

    @Inject(method = "<init>",
            at = @At(value = "RETURN"),
            remap = false)
    private void probejs$$preloadFile(ScriptPack pack, ScriptFileInfo info, CallbackInfo ci) throws IOException {
        // Transform import stuffs so that we can use require etc
        lines = (new KubeJSScript(List.of(lines))).transform();
    }
}
