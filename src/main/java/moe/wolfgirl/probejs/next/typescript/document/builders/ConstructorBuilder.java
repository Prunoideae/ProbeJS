package moe.wolfgirl.probejs.next.typescript.document.builders;

import moe.wolfgirl.probejs.next.typescript.document.Types;
import moe.wolfgirl.probejs.next.typescript.document.base.Type;
import moe.wolfgirl.probejs.next.typescript.document.members.ConstructorDecl;
import moe.wolfgirl.probejs.next.typescript.document.members.ParamDecl;
import moe.wolfgirl.probejs.next.typescript.document.types.VariableType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ConstructorBuilder {
    private final List<VariableType> typeParams = new ArrayList<>();
    private final List<ParamDecl> params = new ArrayList<>();

    public ConstructorBuilder typeParam(String name, @Nullable VariableType bound) {
        this.typeParams.add(Types.variable(name, bound));
        return this;
    }

    public ConstructorBuilder typeParam(String name) {
        return typeParam(name, null);
    }

    public ConstructorBuilder param(String name, Type type, boolean varArgs) {
        this.params.add(new ParamDecl(name, type, varArgs));
        return this;
    }

    public ConstructorBuilder param(String name, Type type) {
        return param(name, type, false);
    }

    public ConstructorDecl build() {
        return new ConstructorDecl(typeParams, params);
    }

}
