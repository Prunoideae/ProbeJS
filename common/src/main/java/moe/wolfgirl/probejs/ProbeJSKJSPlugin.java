package moe.wolfgirl.probejs;

import dev.latvian.mods.kubejs.script.BindingsEvent;
import moe.wolfgirl.probejs.features.plugin.ProbeJSEvents;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ClassFilter;
import moe.wolfgirl.probejs.next.snippet.SnippetDump;
import moe.wolfgirl.probejs.next.typescript.ScriptDump;
import moe.wolfgirl.probejs.next.ProbeEvents;
import moe.wolfgirl.probejs.next.decompiler.ProbeDecompiler;
import moe.wolfgirl.probejs.next.docs.ProbeBuiltinDocs;
import moe.wolfgirl.probejs.next.java.ClassRegistry;
import moe.wolfgirl.probejs.next.java.clazz.ClassPath;
import moe.wolfgirl.probejs.next.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.next.transpiler.Transpiler;
import moe.wolfgirl.probejs.next.transpiler.TypeConverter;
import moe.wolfgirl.probejs.next.typescript.TypeScriptFile;
import moe.wolfgirl.probejs.next.utils.Require;

import java.util.Map;
import java.util.Set;

public class ProbeJSKJSPlugin extends ProbeJSPlugin {
    @Override
    public void registerEvents() {
        ProbeJSEvents.GROUP.register();
        ProbeEvents.GROUP.register();
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("Transpiler", Transpiler.class);
        event.add("ClassRegistry", ClassRegistry.class);
        event.add("Decompiler", ProbeDecompiler.class);
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
    }

    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        ProbeBuiltinDocs.INSTANCE.modifyClasses(scriptDump, globalClasses);
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
    }
}
