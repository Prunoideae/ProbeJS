package moe.wolfgirl.probejs.docs;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventGroups;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.typings.Info;
import moe.wolfgirl.probejs.lang.transpiler.transformation.InjectSpecialType;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.typescript.code.type.TSClassType;
import moe.wolfgirl.probejs.lang.typescript.code.type.TSParamType;
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

        for (Map.Entry<String, EventGroup> entry : EventGroups.ALL.get().map().entrySet()) {
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
                if (handler.target != null) {
                    groupNamespace.addCode(formatEvent(converter, handler, true));
                }
                if (!handler.targetRequired) groupNamespace.addCode(formatEvent(converter, handler, false));
            }
            codes.add(groupNamespace);
        }

        scriptDump.addGlobal("events", codes.toArray(Code[]::new));
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        HashSet<Class<?>> classes = new HashSet<>();

        for (EventGroup group : EventGroups.ALL.get().map().values()) {
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
            BaseType extraType = converter.convertType(handler.target.describeType);
            if (extraType instanceof TSParamType paramType &&
                    paramType.params.size() == 1 &&
                    paramType.baseType instanceof TSClassType classType) {
                if (InjectSpecialType.NO_WRAPPING.contains(classType.classPath)) {
                    paramType.params.set(0, Types.ignoreContext(paramType.params.getFirst(), BaseType.FormatType.RETURN));
                }
            }
            builder.param("extra", extraType);
        }
        Class<?> eventClass = handler.eventType.get();
        JSLambdaType callback = Types.lambda()
                .param("event", Types.typeMaybeGeneric(eventClass))
                .build();
        builder.param("handler", callback);


        MethodDeclaration methodDeclaration = builder.build();
        for (Info info : eventClass.getAnnotationsByType(Info.class)) {
            methodDeclaration.addComment(info.value());
        }
        return methodDeclaration;
    }

    private static Set<Pair<String, String>> getDisabledEvents(ScriptDump dump) {
        Set<Pair<String, String>> events = new HashSet<>();
        ProbeJSPlugin.forEachPlugin(plugin -> events.addAll(plugin.disableEventDumps(dump)));
        return events;
    }
}
