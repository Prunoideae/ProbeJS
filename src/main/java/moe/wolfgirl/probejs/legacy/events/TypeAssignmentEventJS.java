package moe.wolfgirl.probejs.legacy.events;

import moe.wolfgirl.probejs.legacy.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.legacy.lang.typescript.code.type.BaseType;

public class TypeAssignmentEventJS extends ScriptEventJS {

    public TypeAssignmentEventJS(ScriptDump scriptDump) {
        super(scriptDump);
    }

    public void assignType(Class<?> clazz, BaseType baseType) {
        dump.assignType(clazz, baseType);
    }
}
