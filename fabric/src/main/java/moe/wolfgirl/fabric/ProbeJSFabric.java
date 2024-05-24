package moe.wolfgirl.fabric;

import moe.wolfgirl.ProbeJS;
import net.fabricmc.api.ModInitializer;

public class ProbeJSFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ProbeJS.init();
    }
}
