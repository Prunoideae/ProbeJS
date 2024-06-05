package moe.wolfgirl.next.typescript.code.member;

import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.typescript.Declaration;
import moe.wolfgirl.next.typescript.code.type.TSVariableType;

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
            paths.addAll(param.type().getUsedClassPaths());
        }
        return paths;
    }

    @Override
    public List<String> formatRaw(Declaration declaration) {
        // Format head - constructor<T>
        String head = "constructor";
        if (variableTypes.size() != 0) {
            String variables = variableTypes.stream()
                    .map(type -> type.line(declaration))
                    .collect(Collectors.joining(", "));
            head = "%s<%s>".formatted(head, variables);
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

        // Format tail - {/** content */}
        String tail = "";
        if (content != null) {
            tail = "%s {/** %s */}".formatted(tail, content);
        }
        return List.of("%s%s%s".formatted(head, body, tail));
    }
}
