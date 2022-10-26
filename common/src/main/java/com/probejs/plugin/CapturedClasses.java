package com.probejs.plugin;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CapturedClasses {

    public static Map<String, Class<?>> capturedRawEvents = new ConcurrentHashMap<>();
    public static Set<Class<?>> capturedJavaClasses = new HashSet<>();
    public static Set<Class<?>> ignoredEvents = new HashSet<>();

    static {
        ignoredEvents.add(RegistryObjectBuilderTypes.RegistryEventJS.class);
    }

    public static Map<String, Class<?>> getCapturedRawEvents() {
        return ImmutableMap.copyOf(capturedRawEvents);
    }

    public static Set<Class<?>> getCapturedJavaClasses() {
        return ImmutableSet.copyOf(capturedJavaClasses);
    }
}
