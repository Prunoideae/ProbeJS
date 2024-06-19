package moe.wolfgirl.probejs.lang.typescript.code.ts;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.typescript.Declaration;
import moe.wolfgirl.probejs.lang.typescript.code.member.CommentableCode;
import moe.wolfgirl.probejs.lang.typescript.code.member.ParamDecl;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.TSVariableType;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;

import java.util.*;
import java.util.stream.Collectors;

public class MethodDeclaration extends CommentableCode {
    public String name;
    public final List<TSVariableType> variableTypes;
    public final List<ParamDecl> params;
    public BaseType returnType;

    public MethodDeclaration(String name, List<TSVariableType> variableTypes, List<ParamDecl> params, BaseType returnType) {
        this.name = name;
        this.variableTypes = variableTypes;
        this.params = params;
        this.returnType = returnType;
    }

    @Override
    public Collection<ClassPath> getUsedClassPaths() {
        Set<ClassPath> paths = new HashSet<>(returnType.getUsedClassPaths());
        for (TSVariableType variableType : variableTypes) {
            paths.addAll(variableType.getUsedClassPaths());
        }
        for (ParamDecl param : params) {
            paths.addAll(param.type.getUsedClassPaths());
        }
        return paths;
    }

    @Override
    public List<String> formatRaw(Declaration declaration) {
        // Format head - function name<T, U extends A>
        String head = "function %s".formatted(name);
        if (!variableTypes.isEmpty()) {
            String variables = variableTypes.stream()
                    .map(type -> type.line(declaration))
                    .collect(Collectors.joining(", "));
            head = "%s<%s>".formatted(head, variables);
        }

        // Format body - (a: type, ...)
        String body = ParamDecl.formatParams(params, declaration);

        // Format tail - : returnType
        String tail = ": %s".formatted(returnType.line(declaration, BaseType.FormatType.RETURN));

        return List.of("%s%s%s".formatted(head, body, tail));
    }

    public static class Builder {
        public final String name;
        public final List<TSVariableType> variableTypes = new ArrayList<>();
        public final List<ParamDecl> params = new ArrayList<>();
        public BaseType returnType = Types.VOID;

        public Builder(String name) {
            this.name = name;
        }

        public Builder variable(String... symbols) {
            for (String symbol : symbols) {
                variable(Types.generic(symbol));
            }
            return this;
        }

        public Builder variable(TSVariableType... variableType) {
            variableTypes.addAll(Arrays.asList(variableType));
            return this;
        }

        public Builder returnType(BaseType type) {
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
            params.add(new ParamDecl(symbol, type, isVarArg, isOptional));
            return this;
        }


        public MethodDeclaration build() {
            return new MethodDeclaration(
                    name,
                    variableTypes,
                    params,
                    returnType
            );
        }
    }
}
