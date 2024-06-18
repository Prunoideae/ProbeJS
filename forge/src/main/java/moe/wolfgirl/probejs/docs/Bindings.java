package moe.wolfgirl.probejs.docs;

import dev.latvian.mods.kubejs.event.EventGroupWrapper;
import dev.latvian.mods.rhino.BaseFunction;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.NativeJavaClass;
import dev.latvian.mods.rhino.Scriptable;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.lang.transpiler.TypeConverter;
import moe.wolfgirl.probejs.lang.typescript.code.Code;
import moe.wolfgirl.probejs.lang.typescript.code.ts.ReexportDeclaration;
import moe.wolfgirl.probejs.lang.typescript.code.ts.VariableDeclaration;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;

import java.util.*;

/**
 * Adds bindings to some stuffs...
 */
public class Bindings extends ProbeJSPlugin {
    @Override
    public void addGlobals(ScriptDump scriptDump) {
        Context context = scriptDump.manager.context;
        Scriptable scope = scriptDump.manager.topLevelScope;
        TypeConverter converter = scriptDump.transpiler.typeConverter;
        Map<String, BaseType> exported = new HashMap<>();
        Map<String, BaseType> reexported = new HashMap<>(); // Namespaces

        for (Object o : scope.getIds(context)) {
            if (o instanceof String id) {
                Object value = scope.get(context, id, scope);
                if (value instanceof NativeJavaClass javaClass) {
                    value = javaClass.getClassObject();
                } else {
                    value = Context.jsToJava(context, value, Object.class);
                }

                if (value.getClass() == Class.class) {
                    if (((Class<?>) value).isInterface()) {
                        reexported.put(id, converter.convertType(Types.typeOf((Class<?>) value)));
                    } else {
                        exported.put(id, converter.convertType(Types.typeOf((Class<?>) value)));
                    }
                } else if (!(value instanceof BaseFunction || value instanceof EventGroupWrapper)) {
                    exported.put(id, converter.convertType(Types.typeMaybeGeneric(value.getClass())));
                }
            }
        }

        List<Code> codes = new ArrayList<>();
        for (Map.Entry<String, BaseType> entry : exported.entrySet()) {
            String symbol = entry.getKey();
            BaseType type = entry.getValue();
            codes.add(new VariableDeclaration(symbol, type));
        }
        for (Map.Entry<String, BaseType> entry : reexported.entrySet()) {
            String symbol = entry.getKey();
            BaseType type = entry.getValue();
            codes.add(new ReexportDeclaration(symbol, type));
        }
        scriptDump.addGlobal("bindings", exported.keySet(), codes.toArray(Code[]::new));
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        Set<Class<?>> classes = new HashSet<>();
        Context context = scriptDump.manager.context;
        Scriptable scope = scriptDump.manager.topLevelScope;

        for (Object o : scope.getIds(context)) {
            if (o instanceof String id) {
                Object value = scope.get(context, id, scope);
                if (value instanceof NativeJavaClass javaClass) {
                    value = javaClass.getClassObject();
                } else {
                    value = Context.jsToJava(context, value, Object.class);
                }

                if (value.getClass() == Class.class) {
                    classes.add((Class<?>) value);
                } else if (!(value instanceof BaseFunction || value instanceof EventGroupWrapper)) {
                    // No base function as don't know how to get type info
                    // No events because they will be dumped separately
                    classes.add(value.getClass());
                }
            }
        }
        return classes;
    }
}
