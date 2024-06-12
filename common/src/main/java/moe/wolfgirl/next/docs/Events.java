package moe.wolfgirl.next.docs;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import moe.wolfgirl.next.ScriptDump;
import moe.wolfgirl.next.plugin.ProbeJSPlugin;
import moe.wolfgirl.next.typescript.code.ts.MethodDeclaration;

import java.util.*;

public class Events extends ProbeJSPlugin {
    @Override
    public void addGlobals(ScriptDump scriptDump) {

        Multimap<String, EventHandler> availableHandlers = ArrayListMultimap.create();
        Set<Pair<String, String>> disabled = getDisabledEvents(scriptDump);

        for (Map.Entry<String, EventGroup> entry : EventGroup.getGroups().entrySet()) {
            String groupName = entry.getKey();
            EventGroup group = entry.getValue();

            for (EventHandler handler : group.getHandlers().values()) {
                if (!handler.scriptTypePredicate.test(scriptDump.scriptType)) continue;
                if (disabled.contains(new Pair<>(groupName, handler.name))) continue;
                availableHandlers.put(groupName, handler);
            }
        }
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

    private static MethodDeclaration formatEvent(EventHandler handler) {

        return null;
    }

    private static Set<Pair<String, String>> getDisabledEvents(ScriptDump dump) {
        Set<Pair<String, String>> events = new HashSet<>();
        ProbeJSPlugin.forEachPlugin(plugin -> events.addAll(plugin.disableEventDumps(dump)));
        return events;
    }
}
