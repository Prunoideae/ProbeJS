package com.probejs.next.typescript.parts.impl.types.complex;

import com.mojang.datafixers.util.Pair;
import com.probejs.next.java.clazz.ClassPath;
import com.probejs.next.typescript.Reference;
import com.probejs.next.typescript.parts.impl.types.TypeTS;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a function type in TypeScript.
 * <br>
 * e.g. (a: number, b: string) => boolean
 */
public class FunctionTS extends TypeTS {
    private final List<Param> params;
    private final TypeTS returnType;

    private FunctionTS(List<Param> params, TypeTS returnType) {
        this.params = params;
        this.returnType = returnType;
    }

    @Override
    public String getType() {
        List<String> params = new ArrayList<>();
        for (Param param : this.params) {
            String paramStr = switch (param.paramType) {
                case REQUIRED -> "%s: %s".formatted(param.name, param.type.getType());
                case OPTIONAL -> "%s?: %s".formatted(param.name, param.type.getType());
                // We wrap the type in parentheses to avoid ambiguity
                // e.g. union, intersection, etc.
                case REST -> "...%s: %s[]".formatted(param.name, getMaybeWrapped(param.type));
            };
            params.add(paramStr);
        }
        return "(%s) => %s".formatted(String.join(", ", params), returnType.getType());
    }

    @Override
    public void setSymbols(Map<ClassPath, Reference> symbols) {
        super.setSymbols(symbols);
        returnType.setSymbols(symbols);
        for (Param param : params) {
            param.type.setSymbols(symbols);
        }
    }

    public enum ParamType {
        REQUIRED, OPTIONAL, REST
    }

    private static class Param {
        final String name;
        final TypeTS type;
        final ParamType paramType;

        public Param(String name, TypeTS type, ParamType paramType) {
            this.name = name;
            this.type = type;
            this.paramType = paramType;
        }
    }

    public static class Builder {
        private final List<Param> params = new ArrayList<>();
        private TypeTS returnType;

        public Builder param(String name, TypeTS type) {
            return param(name, type, ParamType.REQUIRED);
        }

        public Builder param(String name, TypeTS type, ParamType paramType) {
            params.add(new Param(name, type, paramType));
            return this;
        }

        public Builder returnType(TypeTS returnType) {
            this.returnType = returnType;
            return this;
        }

        public FunctionTS build() {
            return new FunctionTS(params, returnType);
        }
    }

    public static Builder create() {
        return new Builder();
    }
}
