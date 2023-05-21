package com.probejs.jdoc.jsgen;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface ProbeJSEvents {
    EventGroup GROUP = EventGroup.of("ProbeJSEvents");

    EventHandler DOC_GEN = GROUP.server("generateDoc", () -> DocGenerationEventJS.class);
}
