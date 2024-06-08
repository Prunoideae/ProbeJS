package moe.wolfgirl;

import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.util.ClassWrapper;
import moe.wolfgirl.features.plugin.ProbeJSEvents;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ClassFilter;
import moe.wolfgirl.next.decompiler.ProbeDecompiler;
import moe.wolfgirl.next.java.ClassRegistry;
import moe.wolfgirl.next.plugin.ProbeJSPlugin;
import moe.wolfgirl.next.transpiler.Transpiler;
import moe.wolfgirl.next.transpiler.TypeConverter;
import moe.wolfgirl.next.typescript.code.type.TSPrimitiveType;
import moe.wolfgirl.next.utils.Require;

import java.util.HashMap;

public class ProbeJSKJSPlugin extends ProbeJSPlugin {
    @Override
    public void registerEvents() {
        ProbeJSEvents.GROUP.register();
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
    public void addPredefinedTypes(TypeConverter converter) {
        converter.addType(Object.class, TSPrimitiveType.ANY);

        converter.addType(String.class, TSPrimitiveType.STRING);
        converter.addType(CharSequence.class, TSPrimitiveType.STRING);
        converter.addType(Character.class, TSPrimitiveType.STRING);
        converter.addType(Character.TYPE, TSPrimitiveType.STRING);

        converter.addType(Void.class, TSPrimitiveType.VOID);
        converter.addType(Void.TYPE, TSPrimitiveType.VOID);

        converter.addType(Long.class, TSPrimitiveType.NUMBER);
        converter.addType(Long.TYPE, TSPrimitiveType.NUMBER);
        converter.addType(Integer.class, TSPrimitiveType.NUMBER);
        converter.addType(Integer.TYPE, TSPrimitiveType.NUMBER);
        converter.addType(Short.class, TSPrimitiveType.NUMBER);
        converter.addType(Short.TYPE, TSPrimitiveType.NUMBER);
        converter.addType(Byte.class, TSPrimitiveType.NUMBER);
        converter.addType(Byte.TYPE, TSPrimitiveType.NUMBER);
        converter.addType(Number.class, TSPrimitiveType.NUMBER);
        converter.addType(Double.class, TSPrimitiveType.NUMBER);
        converter.addType(Double.TYPE, TSPrimitiveType.NUMBER);
        converter.addType(Float.class, TSPrimitiveType.NUMBER);
        converter.addType(Float.TYPE, TSPrimitiveType.NUMBER);

        converter.addType(Boolean.class, TSPrimitiveType.BOOLEAN);
        converter.addType(Boolean.TYPE, TSPrimitiveType.BOOLEAN);
    }

    @Override
    public void denyTypes(Transpiler transpiler) {
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
}
