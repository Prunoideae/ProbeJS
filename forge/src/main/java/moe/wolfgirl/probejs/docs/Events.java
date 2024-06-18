package moe.wolfgirl.probejs.docs;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.typings.desc.TypeDescJS;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.lang.transpiler.TypeConverter;
import moe.wolfgirl.probejs.lang.typescript.code.Code;
import moe.wolfgirl.probejs.lang.typescript.code.ts.MethodDeclaration;
import moe.wolfgirl.probejs.lang.typescript.code.ts.Statements;
import moe.wolfgirl.probejs.lang.typescript.code.ts.Wrapped;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import moe.wolfgirl.probejs.lang.typescript.code.type.js.JSLambdaType;

import java.util.*;

public class Events extends ProbeJSPlugin {
    @Override
    public void addGlobals(ScriptDump scriptDump) {

        Multimap<String, EventHandler> availableHandlers = ArrayListMultimap.create();
        Set<Pair<String, String>> disabled = getDisabledEvents(scriptDump);
        TypeConverter converter = scriptDump.transpiler.typeConverter;

        for (Map.Entry<String, EventGroup> entry : EventGroup.getGroups().entrySet()) {
            String groupName = entry.getKey();
            EventGroup group = entry.getValue();

            for (EventHandler handler : group.getHandlers().values()) {
                if (!handler.scriptTypePredicate.test(scriptDump.scriptType)) continue;
                if (disabled.contains(new Pair<>(groupName, handler.name))) continue;
                availableHandlers.put(groupName, handler);
            }
        }

        List<Code> codes = new ArrayList<>();
        for (Map.Entry<String, Collection<EventHandler>> entry : availableHandlers.asMap().entrySet()) {
            String group = entry.getKey();
            Collection<EventHandler> handlers = entry.getValue();

            Wrapped.Namespace groupNamespace = new Wrapped.Namespace(group);
            for (EventHandler handler : handlers) {
                if (handler.extra != null) {
                    groupNamespace.addCode(formatEvent(converter, handler, true));
                    if (handler.extra.required) continue;
                }
                groupNamespace.addCode(formatEvent(converter, handler, false));
            }
            codes.add(groupNamespace);
        }

        scriptDump.addGlobal("events", codes.toArray(Code[]::new));
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        HashSet<Class<?>> classes = new HashSet<>();

        for (EventGroup group : EventGroup.getGroups().values()) {
            for (EventHandler handler : group.getHandlers().values()) {
                if (!handler.scriptTypePredicate.test(scriptDump.scriptType)) continue;
                classes.add(handler.eventType.get());
            }
        }

        return classes;
    }

    private static MethodDeclaration formatEvent(TypeConverter converter, EventHandler handler, boolean useExtra) {
        var builder = Statements.method(handler.name);
        if (useExtra) {
            TypeDescJS typeDesc = handler.extra.describeType.apply(TypeConverter.PROBEJS);
            BaseType extraType = converter.convertType(typeDesc);
            builder.param("extra", extraType);
        }
        JSLambdaType callback = Types.lambda()
                .param("event", Types.typeMaybeGeneric(handler.eventType.get()))
                .build();
        builder.param("handler", callback);
        return builder.build();
    }

    private static Set<Pair<String, String>> getDisabledEvents(ScriptDump dump) {
        Set<Pair<String, String>> events = new HashSet<>();
        ProbeJSPlugin.forEachPlugin(plugin -> events.addAll(plugin.disableEventDumps(dump)));
        return events;
    }
}
