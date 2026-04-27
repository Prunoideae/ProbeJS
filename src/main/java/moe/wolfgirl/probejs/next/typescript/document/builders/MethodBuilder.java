package moe.wolfgirl.probejs.next.typescript.document.builders;

import moe.wolfgirl.probejs.next.typescript.document.Types;
import moe.wolfgirl.probejs.next.typescript.document.base.Code;
import moe.wolfgirl.probejs.next.typescript.document.base.KindAware;
import moe.wolfgirl.probejs.next.typescript.document.base.Type;
import moe.wolfgirl.probejs.next.typescript.document.members.MethodDecl;
import moe.wolfgirl.probejs.next.typescript.document.members.ParamDecl;
import moe.wolfgirl.probejs.next.typescript.document.types.VariableType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MethodBuilder {
    private final String identifier;
    private boolean isStatic = false;
    private final List<VariableType> typeParams = new ArrayList<>();
    private final List<ParamDecl> params = new ArrayList<>();
    private Code returnType = Types.VOID;

    public MethodBuilder(String identifier) {
        this.identifier = identifier;
    }

    public MethodBuilder staticMethod() {
        this.isStatic = true;
        return this;
    }

    public MethodBuilder typeParam(String name, @Nullable VariableType bound) {
        this.typeParams.add(Types.variable(name, bound));
        return this;
    }

    public MethodBuilder typeParam(String name) {
        return typeParam(name, null);
    }

    public MethodBuilder param(String name, Type type, boolean varArgs) {
        this.params.add(new ParamDecl(name, type, varArgs));
        return this;
    }

    public MethodBuilder param(String name, Type type) {
        return param(name, type, false);
    }

    public MethodBuilder returnType(Type returnType) {
        this.returnType = returnType;
        return this;
    }

    public MethodBuilder returnType(Class<?> returnType) {
        return returnType(Types.clazz(returnType));
    }

    public MethodDecl build(KindAware.Kind kind) {
        var methodDecl = new MethodDecl(identifier, typeParams, params, returnType, isStatic);
        methodDecl.setKind(kind);
        return methodDecl;
    }

    public MethodDecl build() {
        return build(KindAware.Kind.CLASS);
    }
}
