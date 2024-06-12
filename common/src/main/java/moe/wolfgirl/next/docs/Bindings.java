package moe.wolfgirl.next.docs;

import dev.latvian.mods.kubejs.event.EventGroupWrapper;
import dev.latvian.mods.rhino.BaseFunction;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.NativeJavaClass;
import dev.latvian.mods.rhino.Scriptable;
import moe.wolfgirl.next.ScriptDump;
import moe.wolfgirl.next.plugin.ProbeJSPlugin;
import moe.wolfgirl.next.transpiler.TypeConverter;
import moe.wolfgirl.next.typescript.code.Code;
import moe.wolfgirl.next.typescript.code.ts.VariableDeclaration;
import moe.wolfgirl.next.typescript.code.type.BaseType;
import moe.wolfgirl.next.typescript.code.type.Types;

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

        for (Object o : scope.getIds(context)) {
            if (o instanceof String id) {
                Object value = scope.get(context, id, scope);
                if (value instanceof NativeJavaClass javaClass) {
                    value = javaClass.getClassObject();
                } else {
                    value = Context.jsToJava(context, value, Object.class);
                }

                if (value.getClass() == Class.class) {
                    exported.put(id, converter.convertType(Types.typeOf((Class<?>) value)));
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
