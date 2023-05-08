package com.probejs.plugin;

import com.probejs.jsgen.ProbeJSEvents;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;

public class ProbeJSPlugin extends KubeJSPlugin {
    @Override
    public void registerEvents() {
        ProbeJSEvents.GROUP.register();
    }
}
