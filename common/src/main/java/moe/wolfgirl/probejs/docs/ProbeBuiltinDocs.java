package moe.wolfgirl.probejs.docs;

import moe.wolfgirl.probejs.snippet.SnippetDump;
import moe.wolfgirl.probejs.typescript.ScriptDump;
import moe.wolfgirl.probejs.docs.assignments.EnumTypes;
import moe.wolfgirl.probejs.docs.assignments.JavaPrimitives;
import moe.wolfgirl.probejs.docs.assignments.WorldTypes;
import moe.wolfgirl.probejs.docs.assignments.RecipeTypes;
import moe.wolfgirl.probejs.docs.events.RecipeEvents;
import moe.wolfgirl.probejs.docs.events.RegistryEvents;
import moe.wolfgirl.probejs.docs.events.TagEvents;
import moe.wolfgirl.probejs.java.clazz.ClassPath;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.transpiler.Transpiler;
import moe.wolfgirl.probejs.transpiler.TypeConverter;
import moe.wolfgirl.probejs.typescript.TypeScriptFile;

import java.util.*;

/**
 * Delegate calls to a set of internal ProbeJSPlugin to separate different
 * features
 */
public class ProbeBuiltinDocs extends ProbeJSPlugin {
    public static final ProbeBuiltinDocs INSTANCE = new ProbeBuiltinDocs();

    // So docs can be added because we don't have access to forge stuffs here
    public final static List<ProbeJSPlugin> BUILTIN_DOCS = new ArrayList<>(List.of(
            new RegistryTypes(),
            new SpecialTypes(),
            new Primitives(),
            new JavaPrimitives(),
            new RecipeTypes(),
            new WorldTypes(),
            new EnumTypes(),
            new Bindings(),
            new Events(),
            new TagEvents(),
            new RecipeEvents(),
            new RegistryEvents(),
            new ParamFix())
    );

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

    @Override
    public void addVSCodeSnippets(SnippetDump dump) {
        for (ProbeJSPlugin builtinDoc : BUILTIN_DOCS) {
            builtinDoc.addVSCodeSnippets(dump);
        }
    }
}
