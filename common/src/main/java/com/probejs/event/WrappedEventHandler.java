package com.probejs.event;

import com.probejs.plugin.CapturedClasses;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.event.IEventHandler;

public record WrappedEventHandler(String event, IEventHandler inner) implements IEventHandler {

    @Override
    public void onEvent(EventJS eventJS) {
        //Special handlers for registry events
        if (!(eventJS instanceof RegistryObjectBuilderTypes.RegistryEventJS))
            CapturedClasses.capturedEvents.put(this.event, eventJS.getClass());
        this.inner.onEvent(eventJS);
    }
}
