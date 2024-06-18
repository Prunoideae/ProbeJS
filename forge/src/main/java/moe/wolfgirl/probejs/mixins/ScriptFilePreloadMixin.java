package moe.wolfgirl.probejs.mixins;

import dev.latvian.mods.kubejs.script.ScriptFileInfo;
import dev.latvian.mods.kubejs.script.ScriptSource;
import dev.latvian.mods.kubejs.util.UtilsJS;
import moe.wolfgirl.probejs.lang.transformer.KubeJSScript;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.List;


@Mixin(ScriptFileInfo.class)
public class ScriptFilePreloadMixin {

    @Shadow(remap = false)
    public String[] lines;

    @Inject(method = "preload",
            at = @At(value = "RETURN"),
            remap = false)
    private void probejs$$preloadFile(ScriptSource source, CallbackInfo ci) throws IOException {

        // I don't know why it won't work... But The fact is that I have to reload
        // stuffs again.
        lines = source.readSource((ScriptFileInfo) (Object) this).toArray(UtilsJS.EMPTY_STRING_ARRAY);
        for (int i = 0; i < lines.length; i++) {
            var tline = lines[i].trim();
            if (tline.startsWith("//")) {
                lines[i] = "";
            }
        }

        // Transform import stuffs so that we can use imports etc
        lines = (new KubeJSScript(List.of(lines))).transform();
    }
}
