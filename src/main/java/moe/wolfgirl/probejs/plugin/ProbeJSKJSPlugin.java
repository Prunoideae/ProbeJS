package moe.wolfgirl.probejs.plugin;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import moe.wolfgirl.probejs.utils.Require;

public class ProbeJSKJSPlugin implements KubeJSPlugin {
    @Override
    public void registerBindings(BindingRegistry bindings) {
        bindings.add("require", new Require(bindings.context()));
    }
}
