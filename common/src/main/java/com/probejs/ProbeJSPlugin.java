package com.probejs;

import com.probejs.features.plugin.ProbeJSEvents;
import dev.latvian.mods.kubejs.KubeJSPlugin;

public class ProbeJSPlugin extends KubeJSPlugin {
    @Override
    public void registerEvents() {
        ProbeJSEvents.GROUP.register();
    }
}
