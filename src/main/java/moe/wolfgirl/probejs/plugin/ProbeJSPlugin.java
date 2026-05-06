package moe.wolfgirl.probejs.plugin;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugins;
import dev.latvian.mods.rhino.util.HideFromJS;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.utils.GameUtils;
import moe.wolfgirl.probejs.snippet.SnippetRegisterer;
import moe.wolfgirl.probejs.typescript.base.AliasRegistrar;
import moe.wolfgirl.probejs.typescript.Documents;
import moe.wolfgirl.probejs.typescript.base.DocumentRegistrar;

import java.util.*;
import java.util.function.Consumer;

public class ProbeJSPlugin implements KubeJSPlugin {
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

    /**
     * Expose more classes so that ProbeJS will try to generate TypeScript declaration for them (and classes referenced by them).
     */
    public Set<Class<?>> provideClassForDiscovery() {
        return Set.of();
    }

    /**
     * Allows a class in class scanning to be added to the discovery list.
     * <p>
     * This is useful when the class is not directly referenced by any other classes, but you still want to generate declaration for it.
     * For example, Neoforge event classes.
     *
     * @return
     */
    public boolean allowClassInDiscovery(Class<?> clazz) {
        return false;
    }

    public void addSnippets(SnippetRegisterer registerer) {

    }

    private static int getPriorityFor(ProbeJSPlugin plugin, String methodName) {
        Class<?> clazz = plugin.getClass();
        try {
            var method = Arrays.stream(clazz.getMethods()).filter(m -> m.getName().equals(methodName)).findFirst().orElseThrow();
            Priority priority = method.getAnnotation(Priority.class);
            return priority == null ? 0 : priority.value();
        } catch (Throwable t) {
            return 0;
        }
    }

    @HideFromJS
    public static void forEachWithPriority(String methodName, Consumer<ProbeJSPlugin> consumer) {
        List<ProbeJSPlugin> plugins = new ArrayList<>(ProbeBuiltinDocs.getAll());
        KubeJSPlugins.forEachPlugin(plugin -> {
            if (plugin instanceof ProbeJSPlugin probePlugin) plugins.add(probePlugin);
        });
        plugins.sort(Comparator.comparingInt(plugin -> -getPriorityFor(plugin, methodName)));
        for (ProbeJSPlugin plugin : plugins) {
            try {
                consumer.accept(plugin);
            } catch (Throwable t) {
                ProbeJS.LOGGER.error("Error when applying plugin: %s.%s".formatted(plugin.getClass(), methodName));
                GameUtils.logException(t);
                ProbeJS.LOGGER.error("If you found severe problem in generated docs (e.g. largely missing types), please report to ProbeJS's github!");
            }
        }
    }
}
