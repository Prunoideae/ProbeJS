package com.probejs.plugin;

import dev.latvian.mods.kubejs.event.EventJS;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CapturedClasses {

    public static Map<String, Class<? extends EventJS>> capturedEvents = new HashMap<>();
    public static Map<String, Class<?>> capturedRawEvents = new HashMap<>();
    public static Set<Class<?>> capturedJavaClasses = new HashSet<>();

}
