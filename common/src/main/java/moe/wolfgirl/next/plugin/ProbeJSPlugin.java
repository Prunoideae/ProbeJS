package moe.wolfgirl.next.plugin;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.util.KubeJSPlugins;
import moe.wolfgirl.next.transpiler.Transpiler;
import moe.wolfgirl.next.transpiler.TypeConverter;

import java.util.Map;
import java.util.function.Consumer;

/**
 * A plugin for ProbeJS that is able to alter how ProbeJS works.
 * <br>
 * Different method calls might have same parameter/controller,
 * but it is advised to call different methods and their own stage
 * in order to prevent unexpected behavior.
 */
public class ProbeJSPlugin extends KubeJSPlugin {

    public static void forEachPlugin(Consumer<ProbeJSPlugin> consumer) {
        KubeJSPlugins.forEachPlugin(plugin -> {
            if (plugin instanceof ProbeJSPlugin probePlugin)
                consumer.accept(probePlugin);
        });
    }

    /**
     * Used to add forcefully-converted types in order to prevent transient types
     * like boolean / string from showing up.
     */
    public void addPredefinedTypes(TypeConverter converter) {

    }

    /**
     * Used to prevent some types from showing up in the dump, e.g. primitives.
     */
    public void denyTypes(Transpiler transpiler) {

    }
}
