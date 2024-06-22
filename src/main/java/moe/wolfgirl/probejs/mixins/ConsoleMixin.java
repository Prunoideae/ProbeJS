package moe.wolfgirl.probejs.mixins;

import dev.latvian.mods.kubejs.script.ConsoleLine;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.rhino.RhinoException;
import moe.wolfgirl.probejs.GlobalStates;
import moe.wolfgirl.probejs.lang.linter.LintingWarning;
import moe.wolfgirl.probejs.utils.FileUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;
import java.util.regex.Pattern;

@Mixin(value = ConsoleJS.class, remap = false)
public class ConsoleMixin {

    @Inject(
            method = "error(Ljava/lang/String;Ljava/lang/Throwable;Ljava/util/regex/Pattern;)Ldev/latvian/mods/kubejs/script/ConsoleLine;",
            remap = false,
            at = @At("HEAD"))
    public void reportError(String message, Throwable error, Pattern exitPattern, CallbackInfoReturnable<ConsoleLine> cir) {
        if (error instanceof RhinoException rhinoException) {
            Path path = FileUtils.parseSourcePath(rhinoException.sourceName());
            if (path == null) return;
            LintingWarning warning = new LintingWarning(path, LintingWarning.Level.ERROR,
                    rhinoException.lineNumber(), rhinoException.columnNumber(),
                    rhinoException.details()
            );

            if (GlobalStates.SERVER != null) {
                GlobalStates.SERVER.broadcast("accept_error", warning.asPayload());
            }
        }
    }
}
