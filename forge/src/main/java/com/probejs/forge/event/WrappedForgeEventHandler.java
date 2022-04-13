package com.probejs.forge.event;

import com.probejs.plugin.CapturedClasses;
import dev.latvian.mods.kubejs.forge.KubeJSForgeEventHandlerWrapper;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public record WrappedForgeEventHandler(
        String className, KubeJSForgeEventHandlerWrapper inner) implements KubeJSForgeEventHandlerWrapper {

    @Override
    public void accept(Event event) {
        CapturedClasses.capturedRawEvents.put(className, event.getClass());
        inner.accept(event);
    }

    @NotNull
    @Override
    public Consumer<Event> andThen(@NotNull Consumer<? super Event> after) {
        return inner.andThen(after);
    }
}
