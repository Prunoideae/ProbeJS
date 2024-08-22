package moe.wolfgirl.probejs.docs.assignments;

import dev.latvian.mods.rhino.type.EnumTypeInfo;
import dev.latvian.mods.rhino.type.TypeInfo;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.java.clazz.Clazz;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;

import java.util.concurrent.locks.ReentrantLock;

public class EnumTypes extends ProbeJSPlugin {
    // EnumTypeWrapper is not thread-safe
    private static final ReentrantLock LOCK = new ReentrantLock();

    @Override
    public void assignType(ScriptDump scriptDump) {
        LOCK.lock();
        for (Clazz recordedClass : scriptDump.recordedClasses) {
            try {
                if (!recordedClass.original.isEnum()) continue;
                EnumTypeInfo typeWrapper = (EnumTypeInfo) TypeInfo.of(recordedClass.original);
                BaseType[] types = typeWrapper.enumConstants()
                        .stream()
                        .map(EnumTypeInfo::getName)
                        .map(String::toLowerCase)
                        .map(Types::literal)
                        .toArray(BaseType[]::new);
                scriptDump.assignType(recordedClass.classPath, Types.or(types));
            } catch (Throwable ignore) {
            }
        }
        LOCK.unlock();
    }
}
