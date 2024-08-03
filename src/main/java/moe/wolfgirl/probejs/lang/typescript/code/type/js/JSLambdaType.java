package moe.wolfgirl.probejs.lang.typescript.code.type.js;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.typescript.Declaration;
import moe.wolfgirl.probejs.lang.typescript.code.member.MethodDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.ParamDecl;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;

import java.util.*;

import javax.annotation.Nullable;

public class JSLambdaType extends BaseType {
    public final List<ParamDecl> params;
    public final BaseType returnType;

    public JSLambdaType(List<ParamDecl> params, BaseType returnType) {
        this.params = params;
        this.returnType = returnType;
    }

    @Override
    public Collection<ClassPath> getUsedClassPaths() {
        Set<ClassPath> classPaths = new HashSet<>(returnType.getUsedClassPaths());
        for (ParamDecl param : params) {
            classPaths.addAll(param.type.getUsedClassPaths());
        }
        return classPaths;
    }

    @Override
    public List<String> format(Declaration declaration, FormatType input) {
        // (arg0: type, arg1: type...) => returnType
        return List.of("%s => %s".formatted(
            //when formatType is INPUT, aka this lambda is a param itself, params of this lambda should be concrete ...
            ParamDecl.formatParams(params, declaration, input == FormatType.INPUT ? FormatType.RETURN : FormatType.INPUT),
            //...and return type should be duck type
            returnType.line(declaration, input)
        ));
    }

    public String formatWithName(String name, Declaration declaration, FormatType input) {
        return "%s%s: %s".formatted(name, ParamDecl.formatParams(params, declaration), returnType.line(declaration, FormatType.RETURN));
    }

    public MethodDecl asMethod(String methodName) {
        return new MethodDecl(methodName, List.of(), params, returnType);
    }

    public static class Builder {
        public final List<ParamDecl> params = new ArrayList<>();
        public BaseType returnType = Types.VOID;
        @Nullable
        public FormatType forceFormatType = null;

        public Builder returnType(BaseType type) {
            if (forceFormatType != null) {
                type = Types.ignoreContext(type, forceFormatType);
            }
            this.returnType = type;
            return this;
        }

        public Builder param(String symbol, BaseType type) {
            return param(symbol, type, false);
        }

        public Builder param(String symbol, BaseType type, boolean isOptional) {
            return param(symbol, type, isOptional, false);
        }

        public Builder param(String symbol, BaseType type, boolean isOptional, boolean isVarArg) {
            if (forceFormatType != null) {
                type = Types.ignoreContext(type, forceFormatType);
            }
            params.add(new ParamDecl(symbol, type, isVarArg, isOptional));
            return this;
        }

        public Builder methodTypeStyle() {
            forceFormatType = FormatType.RETURN;
            return this;
        }

        public Builder lambdaTypeStyle() {
            forceFormatType = FormatType.INPUT;
            return this;
        }

        public Builder defaultTypeStyle() {
            forceFormatType = null;
            return this;
        }

        public JSLambdaType build() {
            return new JSLambdaType(params, returnType);
        }
    }
}
