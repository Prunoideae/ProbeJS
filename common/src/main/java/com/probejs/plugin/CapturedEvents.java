package com.probejs.plugin;

import dev.latvian.mods.kubejs.event.EventJS;

import java.util.HashMap;
import java.util.Map;

public class CapturedEvents {

    public static Map<String, Class<? extends EventJS>> capturedEvents = new HashMap<>();
    public static Map<String, Class<?>> capturedRawEvents = new HashMap<>();
}
