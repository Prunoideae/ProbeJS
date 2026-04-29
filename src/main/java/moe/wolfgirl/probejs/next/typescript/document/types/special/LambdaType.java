package moe.wolfgirl.probejs.next.typescript.document.types.special;

import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.typescript.document.Types;
import moe.wolfgirl.probejs.next.typescript.document.base.Code;
import moe.wolfgirl.probejs.next.typescript.document.base.InputAliased;
import moe.wolfgirl.probejs.next.typescript.document.base.Type;

import java.util.*;

public class LambdaType extends Type {
    protected final List<ObjectType.ParamType> params;
    protected Type returnType;

    public LambdaType(List<ObjectType.ParamType> params, Type returnType) {
        this.params = params;
        this.returnType = returnType;
    }

    @Override
    public Set<ClassPath> getImports() {
        Set<ClassPath> classPaths = new HashSet<>();
        for (ObjectType.ParamType param : params) {
            classPaths.addAll(param.getImports());
        }
        classPaths.addAll(returnType.getImports());
        return classPaths;
    }

    @Override
    public List<String> format(int indent) {
        return List.of("((%s) => %s)".formatted(
                String.join(", ", params.stream().map(p -> "%s%s: %s".formatted(
                        p.name,
                        p.optional ? "?" : "",
                        p.type.first()
                )).toList()),
                returnType.first()
        ));
    }

    @Override
    public Collection<Code> getContainedTypes() {
        List<Code> containedTypes = new ArrayList<>(params);
        containedTypes.add(returnType);
        return containedTypes;
    }

    public static class Builder {
        private final List<ObjectType.ParamType> params = new ArrayList<>();
        private Type returnType = Types.VOID;

        public Builder param(String name, Type type, boolean optional) {
            params.add(new ObjectType.ParamType(name, optional, type));
            return this;
        }

        public Builder param(String name, Type type) {
            return param(name, type, false);
        }

        public Builder returns(Type returnType) {
            this.returnType = returnType;
            return this;
        }

        public LambdaType build() {
            return new LambdaType(params, returnType);
        }
    }
}
