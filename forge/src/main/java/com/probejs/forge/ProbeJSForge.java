package com.probejs.forge;

import com.probejs.ProbeConfig;
import com.probejs.forge.event.ProbeJSForgeEventListener;
import dev.architectury.platform.forge.EventBuses;
import com.probejs.ProbeJS;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ProbeJS.MOD_ID)
public class ProbeJSForge {
    public ProbeJSForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(ProbeJS.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        if (!ProbeConfig.INSTANCE.noAggressiveProbing) {
            ProbeJS.LOGGER.info("Listening to EVERY forge event since aggressive probing is active.");
            MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, true, Event.class, ProbeJSForgeEventListener::onEvent);
        }
        ProbeJS.init();
    }
}
