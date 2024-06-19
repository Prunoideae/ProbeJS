package moe.wolfgirl.probejs.lang.transpiler.transformation;


import dev.latvian.mods.rhino.util.ReturnsSelf;
import moe.wolfgirl.probejs.lang.java.clazz.Clazz;
import moe.wolfgirl.probejs.lang.java.clazz.members.MethodInfo;
import moe.wolfgirl.probejs.lang.java.type.TypeDescriptor;
import moe.wolfgirl.probejs.lang.java.type.impl.ClassType;
import moe.wolfgirl.probejs.lang.java.type.impl.ParamType;
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
            if (getBaseType(methodInfo.returnType) == value) methodDecl.returnType = Types.THIS;
        }
    }

    private static Class<?> getBaseType(TypeDescriptor descriptor) {
        return switch (descriptor) {
            case ClassType classType -> classType.clazz;
            case ParamType paramType -> getBaseType(paramType.base);
            case null, default -> Object.class;
        };
    }
}
