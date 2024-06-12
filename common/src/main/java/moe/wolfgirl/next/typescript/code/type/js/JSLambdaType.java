package moe.wolfgirl.next.typescript.code.type.js;

import com.mojang.datafixers.util.Pair;
import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.typescript.Declaration;
import moe.wolfgirl.next.typescript.code.member.ParamDecl;
import moe.wolfgirl.next.typescript.code.type.BaseType;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JSLambdaType extends BaseType {
    public final List<ParamDecl> params;
    public final BaseType returnType;
    // Mark the last argument as varArg, will there be a case that any argument will follow a varArg?
    public final boolean varArg;

    public JSLambdaType(List<ParamDecl> params, BaseType returnType, boolean varArg) {
        this.params = params;
        this.returnType = returnType;
        this.varArg = varArg;
    }

    @Override
    public Collection<ClassPath> getUsedClassPaths() {
        Set<ClassPath> classPaths = new HashSet<>(returnType.getUsedClassPaths());
        for (ParamDecl param : params) {
            classPaths.addAll(param.type().getUsedClassPaths());
        }
        return classPaths;
    }

    @Override
    public List<String> format(Declaration declaration, FormatType input) {
        // (arg0: type, arg1: type...) -> returnType
        return null;
    }
}
