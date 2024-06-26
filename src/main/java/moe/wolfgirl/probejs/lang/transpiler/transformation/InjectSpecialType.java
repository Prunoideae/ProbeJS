package moe.wolfgirl.probejs.lang.transpiler.transformation;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.java.clazz.Clazz;
import moe.wolfgirl.probejs.lang.java.clazz.members.MethodInfo;
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

import java.util.Set;

public class InjectSpecialType implements ClassTransformer {
    private static final Set<ClassPath> NO_WRAPPING = Set.of(
            new ClassPath(ResourceKey.class),
            new ClassPath(TagKey.class),
            new ClassPath(HolderSet.class),
            new ClassPath(Holder.class)
    );


    @Override
    public void transformMethod(Clazz clazz, MethodInfo methodInfo, MethodDecl methodDecl) {
        for (ParamDecl param : methodDecl.params) {
            if (param.type instanceof TSParamType paramType
                    && paramType.baseType instanceof TSClassType baseClass
                    && NO_WRAPPING.contains(baseClass.classPath)) {
                param.type = new TSParamType(
                        paramType.baseType,
                        paramType.params.stream()
                                .map(c -> Types.ignoreContext(c, BaseType.FormatType.RETURN))
                                .toList()
                );
            }
        }
    }
}
