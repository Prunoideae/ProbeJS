package moe.wolfgirl.probejs.docs;

import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.docs.assignments.*;
import moe.wolfgirl.probejs.docs.events.ViewerEvents;
import moe.wolfgirl.probejs.lang.schema.SchemaDump;
import moe.wolfgirl.probejs.lang.snippet.SnippetDump;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.docs.events.RecipeEvents;
import moe.wolfgirl.probejs.docs.events.RegistryEvents;
import moe.wolfgirl.probejs.docs.events.TagEvents;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.lang.transpiler.Transpiler;
import moe.wolfgirl.probejs.lang.transpiler.TypeConverter;
import moe.wolfgirl.probejs.lang.typescript.TypeScriptFile;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Delegate calls to a set of internal ProbeJSPlugin to separate different
 * features
 */
public class ProbeBuiltinDocs extends ProbeJSPlugin {
    public static final ProbeBuiltinDocs INSTANCE = new ProbeBuiltinDocs();

    // So docs can be added stateless
    public final static List<Supplier<ProbeJSPlugin>> BUILTIN_DOCS = new ArrayList<>(List.of(
            RegistryTypes::new,
            SpecialTypes::new,
            Primitives::new,
            JavaPrimitives::new,
            RecipeTypes::new,
            WorldTypes::new,
            EnumTypes::new,
            RecordTypes::new,
            Bindings::new,
            Events::new,
            TagEvents::new,
            RecipeEvents::new,
            RegistryEvents::new,
            ParamFix::new,
            Snippets::new,
            ForgeEventDoc::new,
            AdditionalTypes::new,
            ViewerEvents::new
    ));

    private static void forEach(Consumer<ProbeJSPlugin> consumer) {
        for (Supplier<ProbeJSPlugin> builtinDoc : BUILTIN_DOCS) {
            try {
                consumer.accept(builtinDoc.get());
            } catch (Throwable t) {
                ProbeJS.LOGGER.error("Error when applying builtin doc: %s".formatted(builtinDoc.get().getClass()));
                ProbeJS.LOGGER.error(t.getMessage());
                for (StackTraceElement stackTraceElement : t.getStackTrace()) {
                    ProbeJS.LOGGER.error(stackTraceElement.toString());
                }
                ProbeJS.LOGGER.error("If you found any problem in generated docs, please report to ProbeJS's github!");
            }
        }
    }

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        forEach(builtinDoc -> builtinDoc.addGlobals(scriptDump));
    }

    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        forEach(builtinDoc -> builtinDoc.modifyClasses(scriptDump, globalClasses));

    }

    @Override
    public void assignType(ScriptDump scriptDump) {
        forEach(builtinDoc -> builtinDoc.assignType(scriptDump));

    }

    @Override
    public void addPredefinedTypes(TypeConverter converter) {
        forEach(builtinDoc -> builtinDoc.addPredefinedTypes(converter));

    }

    @Override
    public void denyTypes(Transpiler transpiler) {
        forEach(builtinDoc -> builtinDoc.denyTypes(transpiler));
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        Set<Class<?>> allClasses = new HashSet<>();
        forEach(builtinDoc -> allClasses.addAll(builtinDoc.provideJavaClass(scriptDump)));
        return allClasses;
    }

    @Override
    public void addVSCodeSnippets(SnippetDump dump) {
        forEach(builtinDoc -> builtinDoc.addVSCodeSnippets(dump));
    }

    @Override
    public void addJsonSchema(SchemaDump dump) {
        forEach(builtinDoc -> builtinDoc.addJsonSchema(dump));
    }
}
