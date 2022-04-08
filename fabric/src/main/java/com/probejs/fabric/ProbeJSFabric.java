package com.probejs.fabric;

import com.probejs.ProbeJS;
import net.fabricmc.api.ModInitializer;

public class ProbeJSFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ProbeJS.init();
    }
}
