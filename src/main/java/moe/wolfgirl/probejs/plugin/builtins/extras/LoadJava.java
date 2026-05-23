package moe.wolfgirl.probejs.plugin.builtins.extras;

import dev.latvian.mods.kubejs.plugin.builtin.wrapper.JavaWrapper;
import moe.wolfgirl.probejs.typescript.ClassPath;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.plugin.builtins.alias.SpecialTypes;
import moe.wolfgirl.probejs.typescript.Documents;
import moe.wolfgirl.probejs.typescript.document.ClassDecl;
import moe.wolfgirl.probejs.typescript.document.Types;
import moe.wolfgirl.probejs.typescript.document.base.Code;
import moe.wolfgirl.probejs.typescript.document.members.MethodDecl;
import moe.wolfgirl.probejs.typescript.document.members.ParamDecl;

import java.util.List;
import java.util.Set;

public class LoadJava extends ProbeJSPlugin {
    public static final ClassPath JAVA_WRAPPER = new ClassPath(JavaWrapper.class);
    public static final ClassPath RESOLVE_JAVA_CLASS = JAVA_WRAPPER.getPackage().append("ResolveJavaClass");

    @Override
    public void modifyClasses(Documents.ClassAccessor classDocuments) {
        if (classDocuments.getDocument(JAVA_WRAPPER) instanceof ClassDecl classDecl) {
            for (Code member : classDecl.members) {
                if (member instanceof MethodDecl methodDecl && (
                        methodDecl.name.equals("loadClass") || methodDecl.name.equals("tryLoadClass")
                )) {
                    methodDecl.typeParams.add(Types.variable("N", SpecialTypes.CLASS_PATH));
                    methodDecl.params.set(0, new ParamDecl("name", Types.variable("N"), false));
                    methodDecl.returnType = Types.raw("ResolveJavaClass").withParams(
                            Types.raw("typeof import(\"@package\")"),
                            Types.variable("N")
                    );
                }
            }
        }

        classDocuments.addClassDocument(RESOLVE_JAVA_CLASS, new ResolveJavaClass());
    }

    private static class ResolveJavaClass extends Code {

        @Override
        public Set<ClassPath> getImports() {
            return Set.of();
        }

        @Override
        // type ResolveJavaClass<E, N extends string> = N extends `${infer H}.${infer T}` ? H extends keyof E ? ResolveJavaClass<E[H], T> : never : N extends keyof E ? E[N] : never;
        public List<String> format(int indent) {
            return List.of("%stype ResolveJavaClass<E, N extends string> = N extends `${infer H}.${infer T}` ? H extends keyof E ? ResolveJavaClass<E[H], T> : never : `$${N}` extends keyof E ? E[`$${N}`] : never;".formatted(" ".repeat(indent)));
        }
    }
}
