package moe.wolfgirl.probejs.events;

import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;

public class TypeAssignmentEventJS extends ScriptEventJS {

    public TypeAssignmentEventJS(ScriptDump scriptDump) {
        super(scriptDump);
    }

    public void assignType(Class<?> clazz, BaseType baseType) {
        dump.assignType(clazz, baseType);
    }
}
