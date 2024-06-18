package moe.wolfgirl.probejs.plugin;

import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.util.KubeJSPlugins;
import moe.wolfgirl.probejs.lang.snippet.SnippetDump;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.transpiler.Transpiler;
import moe.wolfgirl.probejs.lang.transpiler.TypeConverter;
import moe.wolfgirl.probejs.lang.typescript.TypeScriptFile;

import java.util.Map;
import java.util.Set;
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

    /**
     * Used to modify the classes that will be dumped to a certain script type.
     * <br>
     * Can add / remove dumps by mutating the globalClasses.
     */
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {

    }

    /**
     * Used to add code to global namespace.
     * <br>
     * Globals are available without any imports, so it must be ensured that the
     * added code is either:
     * 1. a type
     * 2. a binding (though it's not very needed for most people)
     */
    public void addGlobals(ScriptDump scriptDump) {

    }

    /**
     * Adds a convertible type to a classPath.
     * <br>
     * e.g. Item can be assigned with any item name string.
     */
    public void assignType(ScriptDump scriptDump) {

    }

    /**
     * Provides Java classes for the class registry to discover.
     */
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        return Set.of();
    }

    /**
     * Provides events that should be disabled for custom support.
     */
    public Set<Pair<String, String>> disableEventDumps(ScriptDump dump) {
        return Set.of();
    }

    public void addVSCodeSnippets(SnippetDump dump) {

    }
}
