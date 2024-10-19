package moe.wolfgirl.probejs.lang.typescript.code.member.clazz;

import moe.wolfgirl.probejs.lang.typescript.code.member.MethodDecl;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.TSVariableType;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;

public class MethodBuilder extends ConstructorBuilder {
    public final String name;
    public BaseType returnType = Types.VOID;
    public boolean isAbstract = false;
    public boolean isStatic = false;


    public MethodBuilder(String name) {
        this.name = name;
    }

    @Override
    public MethodBuilder typeVariables(TSVariableType... variableTypes) {
        super.typeVariables(variableTypes);
        return this;
    }

    @Override
    public MethodBuilder typeVariables(String... symbols) {
        super.typeVariables(symbols);
        return this;
    }

    @Override
    public MethodBuilder param(String symbol, BaseType type) {
        super.param(symbol, type);
        return this;
    }

    @Override
    public MethodBuilder param(String symbol, BaseType type, boolean isOptional) {
        super.param(symbol, type, isOptional);
        return this;
    }

    @Override
    public MethodBuilder param(String symbol, BaseType type, boolean isOptional, boolean isVarArg) {
        super.param(symbol, type, isOptional, isVarArg);
        return this;
    }

    public MethodBuilder returnType(BaseType type) {
        this.returnType = type;
        return this;
    }

    public MethodBuilder abstractMethod() {
        this.isAbstract = true;
        return this;
    }

    public MethodBuilder staticMethod() {
        this.isStatic = true;
        return this;
    }

    public MethodDecl buildAsMethod() {
        var decl = new MethodDecl(name, variableTypes, params, returnType);
        decl.isAbstract = isAbstract;
        decl.isStatic = isStatic;
        return decl;
    }
}
