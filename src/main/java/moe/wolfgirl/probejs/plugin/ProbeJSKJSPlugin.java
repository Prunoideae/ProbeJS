package moe.wolfgirl.probejs.plugin;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.rhino.Undefined;
import moe.wolfgirl.probejs.utils.ImageUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public class ProbeJSKJSPlugin implements KubeJSPlugin {
    @Override
    public void registerBindings(BindingRegistry bindings) {
        bindings.add("require", new Require(bindings.context()));
        if (FMLEnvironment.dist == Dist.CLIENT) {
            bindings.add("probejs", Probe.INSTANCE);
        }else {
            bindings.add("probejs", Undefined.INSTANCE);
        }

        bindings.add("ImageUtils", ImageUtils.class);
    }
}
