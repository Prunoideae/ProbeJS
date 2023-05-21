package com.probejs;

import com.probejs.jdoc.jsgen.ProbeJSEvents;
import dev.latvian.mods.kubejs.KubeJSPlugin;

public class ProbeJSPlugin extends KubeJSPlugin {
    @Override
    public void registerEvents() {
        ProbeJSEvents.GROUP.register();
    }
}
