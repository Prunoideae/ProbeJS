package moe.wolfgirl.probejs.next.plugin.builtins.discovery;

import moe.wolfgirl.probejs.next.plugin.ProbeJSPlugin;

import java.util.HashSet;
import java.util.Set;

public class JavaLoaded extends ProbeJSPlugin {
    public static Set<Class<?>> ALL_LOADED = new HashSet<>();

    @Override
    public Set<Class<?>> provideClassForDiscovery() {
        return ALL_LOADED;
    }
}
