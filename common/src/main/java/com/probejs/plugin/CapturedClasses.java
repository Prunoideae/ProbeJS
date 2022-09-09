package com.probejs.plugin;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.probejs.event.CapturedEvent;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CapturedClasses {

    public static Map<String, CapturedEvent> capturedEvents = new HashMap<>();
    public static Map<String, Class<?>> capturedRawEvents = new HashMap<>();
    public static Set<Class<?>> capturedJavaClasses = new HashSet<>();
    public static Set<Class<?>> ignoredEvents = new HashSet<>();

    static {
        ignoredEvents.add(RegistryObjectBuilderTypes.RegistryEventJS.class);
    }

    public static boolean isEventIgnored(Class<?> clazz) {
        return ignoredEvents.stream().anyMatch(ignored -> ignored.isAssignableFrom(clazz));
    }

    public static Map<String, CapturedEvent> getCapturedEvents() {
        return ImmutableMap.copyOf(capturedEvents);
    }

    public static Map<String, Class<?>> getCapturedRawEvents() {
        return ImmutableMap.copyOf(capturedRawEvents);
    }

    public static Set<Class<?>> getCapturedJavaClasses() {
        return ImmutableSet.copyOf(capturedJavaClasses);
    }
}
