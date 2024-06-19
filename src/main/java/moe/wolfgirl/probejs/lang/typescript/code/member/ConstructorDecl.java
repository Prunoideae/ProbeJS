package moe.wolfgirl.probejs.lang.typescript.code.member;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.typescript.Declaration;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.TSVariableType;

import java.util.*;
import java.util.stream.Collectors;

public class ConstructorDecl extends CommentableCode {
    public final List<TSVariableType> variableTypes;
    public final List<ParamDecl> params;
    public String content = null;

    public ConstructorDecl(List<TSVariableType> variableTypes, List<ParamDecl> params) {
        this.variableTypes = variableTypes;
        this.params = params;
    }

    @Override
    public Collection<ClassPath> getUsedClassPaths() {
        Set<ClassPath> paths = new HashSet<>();
        for (TSVariableType variable : variableTypes) {
            paths.addAll(variable.getUsedClassPaths());
        }
        for (ParamDecl param : params) {
            paths.addAll(param.type.getUsedClassPaths());
        }
        return paths;
    }

    @Override
    public List<String> formatRaw(Declaration declaration) {
        // Format head - constructor<T>
        String head = "constructor";
        if (!variableTypes.isEmpty()) {
            String variables = variableTypes.stream()
                    .map(type -> type.line(declaration, BaseType.FormatType.VARIABLE))
                    .collect(Collectors.joining(", "));
            head = "%s<%s>".formatted(head, variables);
        }

        // Format body - (a: type, ...)
        String body = ParamDecl.formatParams(params, declaration);

        // Format tail - {/** content */}
        String tail = "";
        if (content != null) {
            tail = "%s {/** %s */}".formatted(tail, content);
        }
        return List.of("%s%s%s".formatted(head, body, tail));
    }
}
