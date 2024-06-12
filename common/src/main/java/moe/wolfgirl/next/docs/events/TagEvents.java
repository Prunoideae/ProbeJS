package moe.wolfgirl.next.docs.events;

import moe.wolfgirl.next.ScriptDump;
import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.plugin.ProbeJSPlugin;
import moe.wolfgirl.next.typescript.TypeScriptFile;

import java.util.Map;

public class TagEvents extends ProbeJSPlugin {
    // Create TagEventProbe<T, I> and TagWrapperProbe<T, I>
    // Generate string overrides for all registry types
    // tags(extra: "item", handler: (event: TagEventProbe<Special.ItemTag, Special.Item>) => void)


    @Override
    public void addGlobals(ScriptDump scriptDump) {
        super.addGlobals(scriptDump);
    }

    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        super.modifyClasses(scriptDump, globalClasses);
    }
}
