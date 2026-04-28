package moe.wolfgirl.probejs.mixins;


import dev.latvian.mods.kubejs.plugin.builtin.wrapper.JavaWrapper;
import dev.latvian.mods.kubejs.script.KubeJSContext;
import dev.latvian.mods.rhino.NativeJavaClass;
import moe.wolfgirl.probejs.next.plugin.builtins.discovery.JavaLoaded;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = JavaWrapper.class, remap = false)
public interface JavaWrapperMixin {
    @Inject(method = "loadClass", remap = false, at = @At("RETURN"))
    private static void pjs$getClassLoaded(KubeJSContext cx, String className, CallbackInfoReturnable<Object> cir) {
        Object object = cir.getReturnValue();
        if (object instanceof NativeJavaClass njc) {
            JavaLoaded.ALL_LOADED.add(njc.getClassObject());
        }
    }

    @Inject(method = "tryLoadClass(Ldev/latvian/mods/kubejs/script/KubeJSContext;Ljava/lang/String;)Ljava/lang/Object;", remap = false, at = @At("RETURN"))
    private static void pjs$tryGetClassLoaded(KubeJSContext cx, String className, CallbackInfoReturnable<Object> cir) {
        Object object = cir.getReturnValue();
        if (object instanceof NativeJavaClass njc) {
            JavaLoaded.ALL_LOADED.add(njc.getClassObject());
        }
    }
}
