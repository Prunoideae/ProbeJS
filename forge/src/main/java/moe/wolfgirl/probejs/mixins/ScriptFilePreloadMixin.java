package moe.wolfgirl.probejs.mixins;

import dev.latvian.mods.kubejs.script.ScriptFileInfo;
import dev.latvian.mods.kubejs.script.ScriptSource;
import dev.latvian.mods.kubejs.util.UtilsJS;
import moe.wolfgirl.probejs.utils.NameUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;


@Mixin(ScriptFileInfo.class)
public class ScriptFilePreloadMixin {

    @Shadow
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

        // Transform import stuffs so that we can use imports
        for (int i = 0; i < lines.length; i++) {
            String tLine = lines[i].trim();
            if (tLine.contains("import") || tLine.contains("require")) {
                List<String> sb = new ArrayList<>();
                for (String s : tLine.split(";")) {
                    Matcher match = NameUtils.MATCH_IMPORT.matcher(s.trim());
                    if (match.matches()) {
                        String names = match.group(1);
                        String classPath = match.group(2);
                        sb.add("let {%s} = require(%s)".formatted(names, classPath));
                    } else {
                        Matcher requireMatch = NameUtils.MATCH_CONST_REQUIRE.matcher(s.trim());
                        if (requireMatch.matches()) {
                            String names = requireMatch.group(1);
                            String classPath = requireMatch.group(2);
                            sb.add("let {%s} = require(%s)".formatted(names, classPath));
                        } else {
                            sb.add(s);
                        }
                    }
                }
                lines[i] = String.join(";", sb);
            }
        }
    }
}
