package moe.wolfgirl.probejs.lang.transpiler.transformation;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.java.clazz.Clazz;
import moe.wolfgirl.probejs.lang.java.clazz.members.ConstructorInfo;
import moe.wolfgirl.probejs.lang.java.clazz.members.MethodInfo;
import moe.wolfgirl.probejs.lang.java.clazz.members.ParamInfo;
import moe.wolfgirl.probejs.lang.java.type.impl.ClassType;
import moe.wolfgirl.probejs.lang.java.type.impl.ParamType;
import moe.wolfgirl.probejs.lang.typescript.code.member.ConstructorDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.MethodDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.ParamDecl;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.TSClassType;
import moe.wolfgirl.probejs.lang.typescript.code.type.TSParamType;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class InjectSpecialType implements ClassTransformer {
    public static final Set<ClassPath> NO_WRAPPING = Set.of(
            new ClassPath(ResourceKey.class),
            new ClassPath(TagKey.class),
            new ClassPath(HolderSet.class),
            new ClassPath(Holder.class)
    );

    public static void modifyWrapping(ParamDecl param) {
        if (param.type instanceof TSParamType paramType &&
                paramType.baseType instanceof TSClassType baseClass &&
                NO_WRAPPING.contains(baseClass.classPath)) {
            param.type = new TSParamType(
                    paramType.baseType,
                    paramType.params.stream()
                            .map(c -> Types.ignoreContext(c, BaseType.FormatType.RETURN))
                            .toList()
            );
        }
    }

    private static int findReturnTypeIndex(Class<?> clazz) {
        Method functional = Arrays.stream(clazz.getMethods()).filter(m -> {
            int modifiers = m.getModifiers();
            return Modifier.isAbstract(modifiers);
        }).findFirst().orElse(null);
        if (functional == null) return -1;
        if (functional.getGenericReturnType() instanceof TypeVariable<?> typeVariable) {
            TypeVariable<?>[] typeVars = clazz.getTypeParameters();
            for (int i = 0; i < typeVars.length; i++) {
                if (typeVars[i].getName().equals(typeVariable.getName())) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static void modifyLambda(ParamDecl param, ParamInfo info) {
        if (info.type instanceof ParamType paramType &&
                paramType.base instanceof ClassType classType &&
                classType.clazz.isAnnotationPresent(FunctionalInterface.class) &&
                param.type instanceof TSParamType tsParamType) {

            List<BaseType> params = new ArrayList<>(tsParamType.params);
            int returnIndex = findReturnTypeIndex(classType.clazz);
            for (int i = 0; i < params.size(); i++) {
                BaseType p = params.get(i);
                params.set(i, Types.ignoreContext(p, returnIndex == i ?
                        BaseType.FormatType.INPUT :
                        BaseType.FormatType.RETURN));
            }

            param.type = new TSParamType(tsParamType.baseType, params);
        }
    }

    @Override
    public void transformConstructor(ConstructorInfo constructorInfo, ConstructorDecl constructorDecl) {
        for (int i = 0; i < constructorDecl.params.size(); i++) {
            var param = constructorDecl.params.get(i);
            modifyWrapping(param);
            modifyLambda(param, constructorInfo.params.get(i));
        }
    }

    @Override
    public void transformMethod(Clazz clazz, MethodInfo methodInfo, MethodDecl methodDecl) {
        for (int i = 0; i < methodDecl.params.size(); i++) {
            var param = methodDecl.params.get(i);
            modifyWrapping(param);
            modifyLambda(param, methodInfo.params.get(i));
        }
    }
}
