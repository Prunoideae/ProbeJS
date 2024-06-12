package moe.wolfgirl.probejs.next.docs.assignments;

import dev.latvian.mods.rhino.util.EnumTypeWrapper;
import moe.wolfgirl.probejs.next.ScriptDump;
import moe.wolfgirl.probejs.next.java.clazz.Clazz;
import moe.wolfgirl.probejs.next.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.next.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.next.typescript.code.type.Types;
import moe.wolfgirl.probejs.next.typescript.code.type.js.JSPrimitiveType;

import java.util.List;

public class EnumTypes extends ProbeJSPlugin {
    @Override
    public void assignType(ScriptDump scriptDump) {
        for (Clazz recordedClass : scriptDump.recordedClasses) {
            if (!recordedClass.original.isEnum()) continue;
            EnumTypeWrapper<?> typeWrapper = EnumTypeWrapper.get(recordedClass.original);
            BaseType[] types = typeWrapper.nameValues
                    .keySet()
                    .stream()
                    .map(Types::literal)
                    .toArray(BaseType[]::new);
            scriptDump.assignType(recordedClass.classPath, Types.or(types));
        }
    }
}
