package moe.wolfgirl.probejs.next.plugin;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugins;
import dev.latvian.mods.rhino.util.HideFromJS;
import moe.wolfgirl.probejs.lang.typescript.code.member.ClassDecl;
import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.java.members.ClassInfo;
import moe.wolfgirl.probejs.next.typescript.Documents;

import java.util.Map;
import java.util.function.Consumer;

public class ProbeJSPlugin implements KubeJSPlugin {

    @HideFromJS
    public static void forEachPlugin(Consumer<ProbeJSPlugin> consumer) {
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
    public void transformClass(ClassInfo classInfo, ClassDecl classDecl) {

    }

    /**
     * Called when all the classes are transpiled into TypeScript declaration. Supports
     * adding / removing classes by mutating the globalDecls.
     */
    public void modifyClasses(Map<ClassPath, ClassInfo> globalClasses, Map<ClassPath, ClassDecl> globalDecls) {

    }

    /**
     * Add a type alias to the classPath.
     */
    public void addTypeAlias(Documents.AliasRegistrar registrar) {

    }
}
