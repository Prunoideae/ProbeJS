package moe.wolfgirl.probejs.docs;

import dev.latvian.mods.kubejs.bindings.JavaWrapper;
import moe.wolfgirl.probejs.lang.java.ClassRegistry;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.java.clazz.Clazz;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.typescript.TypeScriptFile;
import moe.wolfgirl.probejs.lang.typescript.code.ImportInfo;
import moe.wolfgirl.probejs.lang.typescript.code.member.ClassDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.MethodDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.TypeDecl;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.TSStaticType;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import moe.wolfgirl.probejs.lang.typescript.code.type.js.JSObjectType;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;

import java.util.Collection;
import java.util.Map;

public class LoadClass extends ProbeJSPlugin {
    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        // loadClass<T extends ClassPath>(classPath: T): LoadClass<T>;
        ClassPath javaWrapper = new ClassPath(JavaWrapper.class);
        var classFile = globalClasses.get(javaWrapper);
        if (classFile == null) return;
        ClassDecl classDecl = classFile.findCode(ClassDecl.class).orElseThrow();
        for (MethodDecl method : classDecl.methods) {
            if (method.name.toLowerCase().contains("loadclass")) {
                method.variableTypes.add(Types.generic("T", Types.primitive("ClassPath")));
                method.params.getFirst().type = Types.generic("T");
                method.returnType = Types.parameterized(
                        Types.primitive("LoadClass"),
                        Types.generic("T")
                );
            }
        }
    }

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        // import {} from ...
        // type GlobalClasses = { path: Class };
        // type ClassPath = keyof GlobalClasses;
        // type LoadClass<T> = T extends ClassPath ? GlobalClasses[T] : never;
        // ↑ exported as global
        // 20k class imports my beloved...

        JSObjectType.Builder typeDict = Types.object();

        for (Clazz clazz : ClassRegistry.REGISTRY.getFoundClasses()) {
            ClassPath classPath = clazz.classPath;

            if (clazz.attribute.isInterface) {
                typeDict.member(classPath.getClassPathJava(), Types.typeOf(new TSStaticType(classPath)));
            } else {
                typeDict.member(classPath.getClassPathJava(), Types.typeOf(classPath));
            }
        }

        scriptDump.addGlobal("load_class",
                new SpecialDecl("GlobalClasses", Types.ignoreContext(typeDict.buildIndexed(), BaseType.FormatType.RETURN)),
                new TypeDecl("ClassPath", Types.primitive("keyof GlobalClasses")),
                new TypeDecl("LoadClass<T>", Types.primitive("T extends ClassPath ? GlobalClasses[T] : never"))
        );
    }

    private static class SpecialDecl extends TypeDecl {
        public SpecialDecl(String symbol, BaseType type) {
            super(symbol, type);
        }

        @Override
        public Collection<ImportInfo> getUsedImports() {
            return type.getUsedImports();
        }
    }
}
