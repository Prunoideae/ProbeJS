package moe.wolfgirl.probejs.next.plugin.builtins.alias;

import dev.latvian.mods.rhino.type.EnumTypeInfo;
import dev.latvian.mods.rhino.type.TypeInfo;
import moe.wolfgirl.probejs.next.java.ClassRegistry;
import moe.wolfgirl.probejs.next.java.members.ClassInfo;
import moe.wolfgirl.probejs.next.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.next.typescript.Documents;
import moe.wolfgirl.probejs.next.typescript.document.Types;

import java.util.concurrent.locks.ReentrantLock;

public class EnumTypes extends ProbeJSPlugin {
    private static final ReentrantLock LOCK = new ReentrantLock();

    @Override
    public void addTypeAlias(Documents.AliasRegistrar registrar) {
        LOCK.lock();
        for (ClassInfo classInfo : ClassRegistry.INSTANCE.getAllClasses().values()) {
            try {
                if (!classInfo.attributes().isEnum()) continue;
                EnumTypeInfo typeInfo = (EnumTypeInfo) TypeInfo.of(classInfo.clazz());
                typeInfo.enumConstants()
                        .stream()
                        .map(EnumTypeInfo::getName)
                        .map(String::toLowerCase)
                        .map(Types::literal)
                        .forEach(alias -> registrar.addAlias(classInfo.clazz(), alias));
            } catch (Throwable ignore) {
            }
        }
        LOCK.unlock();
    }
}
