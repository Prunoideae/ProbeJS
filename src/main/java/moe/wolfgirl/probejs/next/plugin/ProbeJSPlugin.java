package moe.wolfgirl.probejs.next.plugin;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugins;
import dev.latvian.mods.rhino.util.HideFromJS;
import moe.wolfgirl.probejs.next.typescript.base.AliasRegistrar;
import moe.wolfgirl.probejs.next.typescript.Documents;
import moe.wolfgirl.probejs.next.typescript.base.DocumentRegistrar;

import java.util.function.Consumer;

public class ProbeJSPlugin implements KubeJSPlugin {

    @HideFromJS
    public static void forEachPlugin(Consumer<ProbeJSPlugin> consumer) {
        consumer.accept(ProbeBuiltinDocs.INSTANCE);
        KubeJSPlugins.forEachPlugin(plugin -> {
            if (plugin instanceof ProbeJSPlugin probePlugin)
                consumer.accept(probePlugin);
        });
    }

    /**
     * Called right after a class is transpiled into TypeScript declaration, but before
     * all the classes are transpiled. This is used to apply a general transformation to
     * the class. E.g. setting input/output types, beans, etc.
     */
    public void transformClass(Documents.ClassDocument document) {

    }

    /**
     * Called when all the classes are transpiled into TypeScript declaration. Supports
     * adding / removing classes by mutating the globalDecls.
     */
    public void modifyClasses(Documents.ClassAccessor classDocuments) {

    }

    /**
     * Add a type alias to the classPath.
     */
    public void addTypeAlias(AliasRegistrar registrar) {

    }

    /**
     * Add a document under the @special path.
     */
    public void addSpecialDocuments(DocumentRegistrar registrar) {

    }

    /**
     * Add a document under the @sided path. The class path of the document must be created by ClassPath.sided.
     */
    public void addSidedDocuments(DocumentRegistrar registrar) {

    }
}
