package moe.wolfgirl.next.typescript.code.member;

import moe.wolfgirl.ProbeJS;
import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.typescript.Declaration;
import moe.wolfgirl.next.typescript.code.type.BaseType;
import moe.wolfgirl.next.typescript.code.type.TSVariableType;

import java.util.*;
import java.util.stream.Collectors;

public class MethodDecl extends CommentableCode {
    public String name;
    public boolean isStatic = false;
    public final List<TSVariableType> variableTypes;
    public final List<ParamDecl> params;
    public BaseType returnType;
    public String content = null;


    public MethodDecl(String name, List<TSVariableType> variableTypes, List<ParamDecl> params, BaseType returnType) {
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
            paths.addAll(param.type().getUsedClassPaths());
        }
        return paths;
    }

    @Override
    public List<String> formatRaw(Declaration declaration) {
        // Format head - public static "name"<T, U extends A>
        List<String> modifiers = new ArrayList<>();
        modifiers.add("public");
        if (isStatic) modifiers.add("static");

        String head = String.join(" ", modifiers);
        head = "%s %s".formatted(head, ProbeJS.GSON.toJson(name));
        if (variableTypes.size() != 0) {
            String variables = variableTypes.stream()
                    .map(type -> type.line(declaration))
                    .collect(Collectors.joining(", "));
            head = "%s<%s>".formatted(name, variables);
        }

        // Format body - (a: type, ...)
        List<String> formattedParams = new ArrayList<>();
        ListIterator<ParamDecl> it = params.listIterator();
        while (it.hasNext()) {
            int index = it.nextIndex();
            ParamDecl param = it.next();
            formattedParams.add(param.format(index, declaration));
        }
        String body = "(%s)".formatted(String.join(", ", formattedParams));

        // Format tail - : returnType {/** content */}
        String tail = ": %s".formatted(returnType.line(declaration));
        if (content != null) {
            tail = "%s {/** %s */}".formatted(tail, content);
        }

        return List.of("%s%s%s".formatted(head, body, tail));
    }
}
