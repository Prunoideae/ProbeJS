package moe.wolfgirl.probejs.plugin;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.web.LocalWebServerRegistry;
import moe.wolfgirl.probejs.misc.Require;
import moe.wolfgirl.probejs.misc.endpoints.ProbeJSWeb;

public class ProbeJSKJSPlugin implements KubeJSPlugin {
    @Override
    public void registerBindings(BindingRegistry bindings) {
        bindings.add("require", new Require.Wrapper());
    }

    @Override
    public void registerLocalWebServer(LocalWebServerRegistry registry) {
        ProbeJSWeb.register(registry);
    }

    @Override
    public void registerLocalWebServerWithAuth(LocalWebServerRegistry registry) {
        ProbeJSWeb.registerWithAuth(registry);
    }
}
