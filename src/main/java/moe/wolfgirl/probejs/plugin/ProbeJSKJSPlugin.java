package moe.wolfgirl.probejs.plugin;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.rhino.Undefined;
import moe.wolfgirl.probejs.utils.Require;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public class ProbeJSKJSPlugin implements KubeJSPlugin {

    @Override
    public void registerBindings(BindingRegistry bindings) {
        bindings.add("require", new Require());
        bindings.add("ProbeJS", FMLEnvironment.dist == Dist.CLIENT ? Probe.INSTANCE : Undefined.INSTANCE);
    }
}
