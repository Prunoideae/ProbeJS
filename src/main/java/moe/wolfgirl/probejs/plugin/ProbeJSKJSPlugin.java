package moe.wolfgirl.probejs.plugin;

import dev.latvian.apps.tinyserver.ServerRegistry;
import dev.latvian.apps.tinyserver.ws.WSSession;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.web.KJSHTTPRequest;
import dev.latvian.mods.rhino.Undefined;
import moe.wolfgirl.probejs.GlobalStates;
import moe.wolfgirl.probejs.features.web.KubedexHandler;
import moe.wolfgirl.probejs.utils.ImageUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public class ProbeJSKJSPlugin implements KubeJSPlugin {
    @Override
    public void registerBindings(BindingRegistry bindings) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            bindings.add("probejs", Probe.INSTANCE);
        } else {
            bindings.add("probejs", Undefined.INSTANCE);
        }
    }

    @Override
    public void registerLocalWebServer(ServerRegistry<KJSHTTPRequest> registry) {
        GlobalStates.KUBEDEX = registry.ws("probejs/kubedex", KubedexHandler::new);
    }
}
