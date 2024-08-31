package moe.wolfgirl.probejs.docs;


import dev.latvian.mods.kubejs.neoforge.NativeEventWrapper;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.typescript.code.ImportInfo;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.typescript.TypeScriptFile;
import moe.wolfgirl.probejs.lang.typescript.code.member.ClassDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.MethodDecl;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import net.neoforged.bus.api.Event;


import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ForgeEventDoc extends ProbeJSPlugin {

    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        TypeScriptFile typeScriptFile = globalClasses.get(new ClassPath(NativeEventWrapper.class));
        typeScriptFile.declaration.addClass(new ImportInfo(new ClassPath(Event.class), ImportInfo.Type.ORIGINAL));
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
                if (method.params.getFirst().name.equals("priority")) {
                    method.params.get(1).type = Types.generic("T");
                    method.params.get(2).type = Types.lambda()
                            .param("event", Types.parameterized(Types.primitive("InstanceType"), Types.primitive("T")))
                            .build();
                } else {
                    method.params.get(0).type = Types.generic("T");
                    method.params.get(1).type = Types.lambda()
                            .param("event", Types.parameterized(Types.primitive("InstanceType"), Types.primitive("T")))
                            .build();
                }
            }
        }
    }

    @Override
    public Set<Class<?>> filterScannedClasses(Set<Class<?>> clazz) {
        return clazz.stream().filter(Event.class::isAssignableFrom).collect(Collectors.toSet());
    }
}
