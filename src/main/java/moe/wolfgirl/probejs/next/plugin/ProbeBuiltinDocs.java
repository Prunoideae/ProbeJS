package moe.wolfgirl.probejs.next.plugin;


import moe.wolfgirl.probejs.next.plugin.builtins.InjectAnnotations;
import moe.wolfgirl.probejs.next.plugin.builtins.InjectBeans;
import moe.wolfgirl.probejs.next.plugin.builtins.InjectInputs;
import moe.wolfgirl.probejs.next.plugin.builtins.alias.*;
import moe.wolfgirl.probejs.next.plugin.builtins.discovery.ByMod;
import moe.wolfgirl.probejs.next.plugin.builtins.discovery.JavaLoaded;
import moe.wolfgirl.probejs.next.plugin.builtins.events.Events;
import moe.wolfgirl.probejs.next.plugin.builtins.events.RecipeEvents;
import moe.wolfgirl.probejs.next.plugin.builtins.events.RegistryEvents;
import moe.wolfgirl.probejs.next.plugin.builtins.events.TagEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * This is a plugin, but it is not exposed to the KubeJS plugin system, as it
 * provides fundamental typing transformation and type alias for docs
 * to be correct.
 */
public class ProbeBuiltinDocs extends ProbeJSPlugin {
    public static final ProbeBuiltinDocs INSTANCE = new ProbeBuiltinDocs();

    private static final List<Supplier<ProbeJSPlugin>> BUILTIN_DOCS = new ArrayList<>(List.of(
            // discovery
            JavaLoaded::new,
            ByMod::new,

            // alias
            SpecialTypes::new,
            JavaPrimitiveTypes::new,
            RecordTypes::new,
            EnumTypes::new,
            RecipeTypes::new,
            RegistryTypes::new,
            WorldTypes::new,
            InterfaceTypes::new,

            // transformations
            InjectInputs::new,
            InjectAnnotations::new,
            InjectBeans::new,

            // events
            Events::new,
            RegistryEvents::new,
            RecipeEvents::new,
            TagEvents::new
    ));

    public static List<ProbeJSPlugin> getAll() {
        List<ProbeJSPlugin> result = new ArrayList<>();
        for (Supplier<ProbeJSPlugin> builtinDoc : BUILTIN_DOCS) {
            result.add(builtinDoc.get());
        }
        return result;
    }
}
