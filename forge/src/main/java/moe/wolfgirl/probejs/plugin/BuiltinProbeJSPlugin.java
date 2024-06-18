package moe.wolfgirl.probejs.plugin;

import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ClassFilter;
import moe.wolfgirl.probejs.events.ProbeEvents;
import moe.wolfgirl.probejs.docs.ProbeBuiltinDocs;
import moe.wolfgirl.probejs.events.SnippetGenerationEventJS;
import moe.wolfgirl.probejs.events.TypeAssignmentEventJS;
import moe.wolfgirl.probejs.events.TypingModificationEventJS;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.snippet.SnippetDump;
import moe.wolfgirl.probejs.lang.transpiler.Transpiler;
import moe.wolfgirl.probejs.lang.transpiler.TypeConverter;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.typescript.TypeScriptFile;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import moe.wolfgirl.probejs.utils.Require;

import java.util.Map;
import java.util.Set;

public class BuiltinProbeJSPlugin extends ProbeJSPlugin {
    @Override
    public void registerEvents() {
        ProbeEvents.GROUP.register();
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        if (event.manager.scriptType == ScriptType.CLIENT) event.add("Types", Types.class);
        event.add("require", new Require(event.manager));
    }

    @Override
    public void registerClasses(ScriptType type, ClassFilter filter) {
        // lol
        filter.deny("com.probejs");
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
}
