package com.probejs.forge.event;

import com.probejs.plugin.CapturedClasses;
import net.minecraftforge.eventbus.api.Event;


public class ProbeJSForgeEventListener {

    public static void onEvent(Event event) {
        CapturedClasses.capturedRawEvents.put(event.getClass().getName(), event.getClass());
    }
}
