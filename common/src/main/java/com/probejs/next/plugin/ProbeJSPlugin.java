package com.probejs.next.plugin;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.util.KubeJSPlugins;

import java.util.function.Consumer;

public class ProbeJSPlugin extends KubeJSPlugin {

    public static void forEachPlugin(Consumer<ProbeJSPlugin> consumer) {
        KubeJSPlugins.forEachPlugin(plugin -> {
            if (plugin instanceof ProbeJSPlugin probePlugin)
                consumer.accept(probePlugin);
        });
    }

    public void assignRegistryType() {

    }

    public void denyType() {

    }
}
