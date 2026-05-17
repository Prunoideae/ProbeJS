package moe.wolfgirl.probejs.typescript.document.types.special;

import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.typescript.ClassPath;
import moe.wolfgirl.probejs.typescript.document.base.Code;
import moe.wolfgirl.probejs.typescript.document.base.Type;
import moe.wolfgirl.probejs.utils.NameUtils;

import java.util.*;
import java.util.regex.Pattern;

public class ObjectType extends Type {
    // { paramName: type, ... }
    protected final List<ParamType> params;

    public ObjectType(List<ParamType> params) {
        this.params = params;
    }

    @Override
    public Set<ClassPath> getImports() {
        Set<ClassPath> imports = new HashSet<>();
        for (var param : params) {
            imports.addAll(param.getImports());
        }
        return imports;
    }

    @Override
    public List<String> format(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        for (ParamType param : params) {
            if (param.name == null) {
                throw new IllegalStateException("ObjectType param name cannot be null, use FixedArrayType instead");
            }
            sb.append(param.first()).append(", ");
        }
        sb.append(" }");
        return List.of(sb.toString());
    }

    @Override
    public Collection<Code> getContainedTypes() {
        return params.stream().map(p -> (Code) p).toList();
    }

    public static class ParamType extends Type {
        // paramName: type, not valid name use GSON to dump first
        private static final Pattern VALID_PARAM_NAME = Pattern.compile("^[a-zA-Z_$][a-zA-Z0-9_$]*$");

        final String name;
        final boolean optional;
        final Type type;

        ParamType(String name, boolean optional, Type type) {
            this.name = name;
            this.optional = optional;
            this.type = type;
        }

        @Override
        public Set<ClassPath> getImports() {
            return type.getImports();
        }

        public boolean isNameValid() {
            return name == null || NameUtils.JS_IDENTIFIER_MATCH.matcher(name).matches();
        }

        @Override
        public List<String> format(int indent) {
            if (name == null) {
                return List.of(type.first() + (optional ? "?" : ""));
            } else {
                return List.of((isNameValid() ? name : ProbeJS.GSON.toJson(name)) + (optional ? "?" : "") + ": " + type.first());
            }
        }

        @Override
        public Collection<Code> getContainedTypes() {
            return Set.of(type);
        }
    }

    public FixedArrayType asFixedArray() {
        return new FixedArrayType(params);
    }

    public static class Builder {
        private final List<ParamType> params = new ArrayList<>();

        public Builder param(String name, boolean optional, Type type) {
            params.add(new ParamType(name, optional, type));
            return this;
        }

        public Builder param(String name, Type type) {
            return param(name, false, type);
        }

        public Builder param(Type type) {
            return param(null, false, type);
        }

        public ObjectType build() {
            return new ObjectType(params);
        }
    }
}
