package com.probejs.forge;

import dev.architectury.platform.forge.EventBuses;
import com.probejs.ProbeJS;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ProbeJS.MOD_ID)
public class ProbeJSForge {
    public ProbeJSForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(ProbeJS.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        ProbeJS.init();
    }
}
