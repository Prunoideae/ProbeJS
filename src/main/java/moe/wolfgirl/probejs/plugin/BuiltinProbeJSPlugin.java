package moe.wolfgirl.probejs.plugin;

import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.ClassFilter;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import moe.wolfgirl.probejs.events.ProbeEvents;
import moe.wolfgirl.probejs.docs.ProbeBuiltinDocs;
import moe.wolfgirl.probejs.events.SnippetGenerationEventJS;
import moe.wolfgirl.probejs.events.TypeAssignmentEventJS;
import moe.wolfgirl.probejs.events.TypingModificationEventJS;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.schema.SchemaDump;
import moe.wolfgirl.probejs.lang.snippet.SnippetDump;
import moe.wolfgirl.probejs.lang.transpiler.Transpiler;
import moe.wolfgirl.probejs.lang.transpiler.TypeConverter;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.typescript.TypeScriptFile;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;

import java.util.Map;
import java.util.Set;

public class BuiltinProbeJSPlugin extends ProbeJSPlugin {

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(ProbeEvents.GROUP);
    }

    @Override
    public void registerBindings(BindingRegistry bindings) {
        bindings.add("Types", Types.class);
    }

    @Override
    public void registerClasses(ClassFilter filter) {
        // lol
        filter.deny("org.jetbrains.java.decompiler");
        filter.deny("com.github.javaparser");
        filter.deny("org.java_websocket");
    }

    @Override
    public void assignType(ScriptDump scriptDump) {
        ProbeBuiltinDocs.INSTANCE.assignType(scriptDump);
        ProbeEvents.ASSIGN_TYPE.post(new TypeAssignmentEventJS(scriptDump));
    }

    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        ProbeBuiltinDocs.INSTANCE.modifyClasses(scriptDump, globalClasses);
        ProbeEvents.MODIFY_DOC.post(new TypingModificationEventJS(scriptDump, globalClasses));
    }

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        ProbeBuiltinDocs.INSTANCE.addGlobals(scriptDump);
    }

    @Override
    public void addPredefinedTypes(TypeConverter converter) {
        ProbeBuiltinDocs.INSTANCE.addPredefinedTypes(converter);
    }

    @Override
    public void denyTypes(Transpiler transpiler) {
        ProbeBuiltinDocs.INSTANCE.denyTypes(transpiler);

        transpiler.reject(Object.class);

        transpiler.reject(String.class);
        transpiler.reject(Character.class);
        transpiler.reject(Character.TYPE);

        transpiler.reject(Void.class);
        transpiler.reject(Void.TYPE);

        transpiler.reject(Long.class);
        transpiler.reject(Long.TYPE);
        transpiler.reject(Integer.class);
        transpiler.reject(Integer.TYPE);
        transpiler.reject(Short.class);
        transpiler.reject(Short.TYPE);
        transpiler.reject(Byte.class);
        transpiler.reject(Byte.TYPE);
        transpiler.reject(Number.class);
        transpiler.reject(Double.class);
        transpiler.reject(Double.TYPE);
        transpiler.reject(Float.class);
        transpiler.reject(Float.TYPE);

        transpiler.reject(Boolean.class);
        transpiler.reject(Boolean.TYPE);
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        return ProbeBuiltinDocs.INSTANCE.provideJavaClass(scriptDump);
    }

    @Override
    public void addVSCodeSnippets(SnippetDump dump) {
        ProbeBuiltinDocs.INSTANCE.addVSCodeSnippets(dump);
        ProbeEvents.SNIPPETS.post(new SnippetGenerationEventJS(dump));
    }

    @Override
    public void addJsonSchema(SchemaDump dump) {
        ProbeBuiltinDocs.INSTANCE.addJsonSchema(dump);
    }

    @Override
    public Set<Class<?>> filterScannedClasses(Set<Class<?>> clazz) {
        return ProbeBuiltinDocs.INSTANCE.filterScannedClasses(clazz);
    }
}
