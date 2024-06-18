package moe.wolfgirl.probejs.lang.typescript.code.member.clazz;

import moe.wolfgirl.probejs.lang.typescript.code.member.ConstructorDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.ParamDecl;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.TSVariableType;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConstructorBuilder {
    public final List<TSVariableType> variableTypes = new ArrayList<>();
    public final List<ParamDecl> params = new ArrayList<>();

    public ConstructorBuilder typeVariables(String... symbols) {
        for (String symbol : symbols) {
            typeVariables(Types.generic(symbol));
        }
        return this;
    }

    public ConstructorBuilder typeVariables(TSVariableType... variableTypes) {
        this.variableTypes.addAll(Arrays.asList(variableTypes));
        return this;
    }

    public ConstructorBuilder param(String symbol, BaseType type) {
        return param(symbol, type, false);
    }

    public ConstructorBuilder param(String symbol, BaseType type, boolean isOptional) {
        return param(symbol, type, isOptional, false);
    }

    public ConstructorBuilder param(String symbol, BaseType type, boolean isOptional, boolean isVarArg) {
        params.add(new ParamDecl(symbol, type, isVarArg, isOptional));
        return this;
    }

    public final ConstructorDecl buildAsConstructor() {
        return new ConstructorDecl(variableTypes, params);
    }
}
