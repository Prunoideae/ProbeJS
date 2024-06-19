package moe.wolfgirl.probejs.lang.typescript.code.member;

import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.typescript.Declaration;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.TSVariableType;

import java.util.*;
import java.util.stream.Collectors;

public class MethodDecl extends CommentableCode {
    public String name;
    public boolean isAbstract = false;
    public boolean isStatic = false;
    public boolean isInterface = false;
    public List<TSVariableType> variableTypes;
    public List<ParamDecl> params;
    public BaseType returnType;
    public String content = null;


    public MethodDecl(String name, List<TSVariableType> variableTypes, List<ParamDecl> params, BaseType returnType) {
        this.name = name;
        this.variableTypes = new ArrayList<>(variableTypes);
        this.params = new ArrayList<>(params);
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
        // Format head - public static "name"<T, U extends A>
        List<String> modifiers = new ArrayList<>();
        if (!isInterface) modifiers.add("public");
        if (isStatic) modifiers.add("static");

        String head = String.join(" ", modifiers);
        head = "%s %s".formatted(head, ProbeJS.GSON.toJson(name));
        if (!variableTypes.isEmpty()) {
            String variables = variableTypes.stream()
                    .map(type -> type.line(declaration, BaseType.FormatType.VARIABLE))
                    .collect(Collectors.joining(", "));
            head = "%s<%s>".formatted(head, variables);
        }

        // Format body - (a: type, ...)
        String body = ParamDecl.formatParams(params, declaration);

        // Format tail - : returnType {/** content */}
        String tail = ": %s".formatted(returnType.line(declaration, BaseType.FormatType.RETURN));
        if (content != null) {
            tail = "%s {/** %s */}".formatted(tail, content);
        }

        return List.of("%s%s%s".formatted(head, body, tail));
    }
}
