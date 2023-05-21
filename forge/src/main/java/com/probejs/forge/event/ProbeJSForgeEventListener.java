package com.probejs.forge.event;

import com.probejs.compiler.DocCompiler;
import net.minecraftforge.eventbus.api.Event;


public class ProbeJSForgeEventListener {

    public static void onEvent(Event event) {
        DocCompiler.CapturedClasses.capturedRawEvents.put(event.getClass().getName(), event.getClass());
    }
}
