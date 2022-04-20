package com.probejs.event;

import dev.latvian.mods.kubejs.event.EventJS;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapturedEvent {
    @Nonnull
    private final Class<? extends EventJS> captured;
    @Nullable
    private final String sub;
    @Nonnull
    private final String id;
    public CapturedEvent(@Nonnull Class<? extends EventJS> captured, String id, @Nullable String sub) {
        this.captured = captured;
        this.sub = sub;
        this.id = id;
    }

    public boolean hasSub() {
        return sub != null;
    }

    @Nullable
    public String getSub() {
        return sub;
    }

    @Nonnull
    public String getId() {
        return id;
    }

    @Nonnull
    public Class<? extends EventJS> getCaptured() {
        return captured;
    }
}
