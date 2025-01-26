package moe.wolfgirl.probejs.lang.transpiler.transformation;


import dev.latvian.mods.rhino.util.ReturnsSelf;
import moe.wolfgirl.probejs.lang.java.clazz.Clazz;
import moe.wolfgirl.probejs.lang.java.clazz.members.MethodInfo;
import moe.wolfgirl.probejs.lang.typescript.code.member.MethodDecl;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;

public class InjectSelf implements ClassTransformer {

    @Override
    public void transformMethod(Clazz clazz, MethodInfo methodInfo, MethodDecl methodDecl) {
        if (methodInfo.hasAnnotation(ReturnsSelf.class)) {
            methodDecl.returnType = Types.THIS;
        } else if (clazz.hasAnnotation(ReturnsSelf.class)) {
            Class<?> value = clazz.getAnnotation(ReturnsSelf.class).value();
            if (value == Object.class) value = clazz.original;
            if (methodInfo.returnType.asClass() == value) methodDecl.returnType = Types.THIS;
        }
    }

}
