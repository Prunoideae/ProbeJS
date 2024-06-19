package moe.wolfgirl.probejs.events;

import dev.latvian.mods.kubejs.event.EventJS;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;

public class TypeAssignmentEventJS extends EventJS {
    private final ScriptDump scriptDump;

    public TypeAssignmentEventJS(ScriptDump scriptDump) {
        this.scriptDump = scriptDump;
    }

    public void assignType(Class<?> clazz, BaseType baseType) {
        scriptDump.assignType(clazz, baseType);
    }
}
