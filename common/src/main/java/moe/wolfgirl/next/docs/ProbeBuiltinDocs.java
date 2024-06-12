package moe.wolfgirl.next.docs;

import moe.wolfgirl.next.ScriptDump;
import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.plugin.ProbeJSPlugin;
import moe.wolfgirl.next.transpiler.Transpiler;
import moe.wolfgirl.next.transpiler.TypeConverter;
import moe.wolfgirl.next.typescript.TypeScriptFile;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Delegate calls to a set of internal ProbeJSPlugin to separate different
 * features
 */
public class ProbeBuiltinDocs extends ProbeJSPlugin {
    public static final ProbeBuiltinDocs INSTANCE = new ProbeBuiltinDocs();

    public final ProbeJSPlugin[] BUILTIN_DOCS = new ProbeJSPlugin[]{
            new RegistryTypes(),
            new Primitives(),
            new Bindings(),
    };

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        for (ProbeJSPlugin builtinDoc : BUILTIN_DOCS) {
            builtinDoc.addGlobals(scriptDump);
        }
    }

    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        for (ProbeJSPlugin builtinDoc : BUILTIN_DOCS) {
            builtinDoc.modifyClasses(scriptDump, globalClasses);
        }
    }

    @Override
    public void assignType(ScriptDump scriptDump) {
        for (ProbeJSPlugin builtinDoc : BUILTIN_DOCS) {
            builtinDoc.assignType(scriptDump);
        }
    }

    @Override
    public void addPredefinedTypes(TypeConverter converter) {
        for (ProbeJSPlugin builtinDoc : BUILTIN_DOCS) {
            builtinDoc.addPredefinedTypes(converter);
        }
    }

    @Override
    public void denyTypes(Transpiler transpiler) {
        for (ProbeJSPlugin builtinDoc : BUILTIN_DOCS) {
            builtinDoc.denyTypes(transpiler);
        }
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        Set<Class<?>> allClasses = new HashSet<>();
        for (ProbeJSPlugin builtinDoc : BUILTIN_DOCS) {
            allClasses.addAll(builtinDoc.provideJavaClass(scriptDump));
        }
        return allClasses;
    }
}
