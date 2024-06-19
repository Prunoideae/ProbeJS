package moe.wolfgirl.probejs.docs;

import dev.latvian.mods.kubejs.forge.ForgeEventWrapper;
import moe.wolfgirl.probejs.GlobalStates;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.typescript.TypeScriptFile;
import moe.wolfgirl.probejs.lang.typescript.code.member.ClassDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.MethodDecl;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.GenericEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ForgeEventDoc extends ProbeJSPlugin {

    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        TypeScriptFile typeScriptFile = globalClasses.get(new ClassPath(ForgeEventWrapper.class));
        typeScriptFile.declaration.addClass(new ClassPath(GenericEvent.class));
        typeScriptFile.declaration.addClass(new ClassPath(Event.class));
        ClassDecl classDecl = typeScriptFile.findCode(ClassDecl.class).orElse(null);
        if (classDecl == null) return;

        for (MethodDecl method : classDecl.methods) {
            if (method.name.equals("onEvent")) {
                method.variableTypes.add(
                        Types.generic("T",
                                Types.typeOf(
                                        Types.parameterized(
                                                Types.type(Event.class),
                                                Types.UNKNOWN)
                                )
                        )
                );
                method.params.get(0).type = Types.generic("T");
                method.params.get(1).type = Types.lambda()
                        .param("event", Types.parameterized(Types.primitive("InstanceType"), Types.primitive("T")))
                        .build();
            } else if (method.name.equals("onGenericEvent")) {
                method.variableTypes.add(
                        Types.generic("T",
                                Types.typeOf(
                                        Types.parameterized(
                                                Types.type(GenericEvent.class),
                                                Types.UNKNOWN)
                                )
                        )
                );
                method.params.get(0).type = Types.generic("T");
                method.params.get(2).type = Types.lambda()
                        .param("event", Types.parameterized(Types.primitive("InstanceType"), Types.primitive("T")))
                        .build();
            }
        }
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        Set<Class<?>> classes = new HashSet<>(GlobalStates.KNOWN_EVENTS);
        classes.add(GenericEvent.class);
        return classes;
    }
}
