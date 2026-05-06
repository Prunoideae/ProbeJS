package moe.wolfgirl.probejs.plugin.builtins.alias;

import dev.latvian.mods.rhino.type.EnumTypeInfo;
import dev.latvian.mods.rhino.type.TypeInfo;
import moe.wolfgirl.probejs.ClassPath;
import moe.wolfgirl.probejs.java.ClassRegistry;
import moe.wolfgirl.probejs.java.members.ClassInfo;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.typescript.Documents;
import moe.wolfgirl.probejs.typescript.base.AliasRegistrar;
import moe.wolfgirl.probejs.typescript.document.Types;
import moe.wolfgirl.probejs.typescript.document.members.MethodDecl;

import java.util.concurrent.locks.ReentrantLock;

public class EnumTypes extends ProbeJSPlugin {
    private static final ReentrantLock LOCK = new ReentrantLock();
    private static final ClassPath ENUM = new ClassPath(Enum.class);

    @Override
    public void addTypeAlias(AliasRegistrar registrar) {
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
                        .forEach(alias -> registrar.addInputAlias(classInfo.clazz(), alias));
            } catch (Throwable ignore) {
            }
        }
        LOCK.unlock();
    }

    @Override
    public void transformClass(Documents.ClassDocument document) {
        if (!document.classInfo().classPath().equals(ENUM)) return;
        document.document().members.removeIf(code -> code instanceof MethodDecl methodDecl && methodDecl.name.equals("valueOf") && methodDecl.isStatic);
    }
}
