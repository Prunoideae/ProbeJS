package moe.wolfgirl.probejs.next.plugin;


import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.next.plugin.builtins.InjectAnnotations;
import moe.wolfgirl.probejs.next.plugin.builtins.InjectBeans;
import moe.wolfgirl.probejs.next.plugin.builtins.InjectInputs;
import moe.wolfgirl.probejs.next.plugin.builtins.TestDocument;
import moe.wolfgirl.probejs.next.plugin.builtins.alias.EnumTypes;
import moe.wolfgirl.probejs.next.plugin.builtins.alias.RecordTypes;
import moe.wolfgirl.probejs.next.plugin.builtins.alias.RegistryTypes;
import moe.wolfgirl.probejs.next.plugin.builtins.alias.WorldTypes;
import moe.wolfgirl.probejs.next.plugin.builtins.discovery.ClassScanning;
import moe.wolfgirl.probejs.next.plugin.builtins.discovery.JavaLoaded;
import moe.wolfgirl.probejs.next.typescript.base.AliasRegistrar;
import moe.wolfgirl.probejs.next.typescript.Documents;
import moe.wolfgirl.probejs.next.typescript.base.DocumentRegistrar;
import moe.wolfgirl.probejs.legacy.utils.GameUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This is a plugin, but it is not exposed to the KubeJS plugin system, as it
 * provides fundamental typing transformation and type alias for docs
 * to be correct.
 */
public class ProbeBuiltinDocs extends ProbeJSPlugin {
    public static final ProbeBuiltinDocs INSTANCE = new ProbeBuiltinDocs();

    public static final List<Supplier<ProbeJSPlugin>> BUILTIN_DOCS = new ArrayList<>(List.of(
            // discovery
            ClassScanning::new,
            JavaLoaded::new,

            // alias
            EnumTypes::new,
            RecordTypes::new,
            RegistryTypes::new,
            WorldTypes::new,

            // transformations
            InjectInputs::new,
            InjectAnnotations::new,
            InjectBeans::new,
            TestDocument::new
    ));

    public static void forEach(Consumer<ProbeJSPlugin> consumer) {
        for (Supplier<ProbeJSPlugin> builtinDoc : BUILTIN_DOCS) {
            try {
                consumer.accept(builtinDoc.get());
            } catch (Throwable t) {
                ProbeJS.LOGGER.error("Error when applying builtin doc: %s".formatted(builtinDoc.get().getClass()));
                GameUtils.logException(t);
                ProbeJS.LOGGER.error("If you found severe problem in generated docs (e.g. largely missing types), please report to ProbeJS's github!");
            }
        }
    }

    @Override
    public void transformClass(Documents.ClassDocument document) {
        forEach(plugin -> plugin.transformClass(document));
    }

    @Override
    public void modifyClasses(Documents.ClassAccessor classDocuments) {
        forEach(plugin -> plugin.modifyClasses(classDocuments));
    }

    @Override
    public void addTypeAlias(AliasRegistrar registrar) {
        forEach(plugin -> plugin.addTypeAlias(registrar));
    }

    @Override
    public void addSpecialDocuments(DocumentRegistrar registrar) {
        forEach(plugin -> plugin.addSpecialDocuments(registrar));
    }

    @Override
    public void addSidedDocuments(DocumentRegistrar registrar) {
        forEach(plugin -> plugin.addSidedDocuments(registrar));
    }

    @Override
    public Set<Class<?>> provideClassForDiscovery() {
        Set<Class<?>> result = new HashSet<>();
        forEach(plugin -> result.addAll(plugin.provideClassForDiscovery()));
        return result;
    }
}
