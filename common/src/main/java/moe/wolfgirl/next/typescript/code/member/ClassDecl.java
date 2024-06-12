package moe.wolfgirl.next.typescript.code.member;

import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.typescript.Declaration;
import moe.wolfgirl.next.typescript.code.Code;
import moe.wolfgirl.next.typescript.code.type.BaseType;
import moe.wolfgirl.next.typescript.code.type.TSVariableType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The Class name in the ClassDecl must be the name of its corresponding classpath.
 */
public class ClassDecl extends CommentableCode {
    public final String name;
    @Nullable
    public BaseType superClass;
    public final List<BaseType> interfaces;
    public final List<TSVariableType> variableTypes;

    public boolean isAbstract = false;

    public final List<FieldDecl> fields = new ArrayList<>();
    public final List<ConstructorDecl> constructors = new ArrayList<>();
    public final List<MethodDecl> methods = new ArrayList<>();

    /**
     * Reserved field to inject custom code body
     */
    public final List<Code> bodyCode = new ArrayList<>();

    public ClassDecl(String name, @Nullable BaseType superClass, List<BaseType> interfaces, List<TSVariableType> variableTypes) {
        this.name = name;
        this.superClass = superClass;
        this.interfaces = interfaces;
        this.variableTypes = variableTypes;
    }

    @Override
    public Collection<ClassPath> getUsedClassPaths() {
        Set<ClassPath> paths = new HashSet<>();
        for (FieldDecl field : fields) {
            paths.addAll(field.getUsedClassPaths());
        }
        for (ConstructorDecl constructor : constructors) {
            paths.addAll(constructor.getUsedClassPaths());
        }
        for (MethodDecl method : methods) {
            paths.addAll(method.getUsedClassPaths());
        }
        for (BaseType anInterface : interfaces) {
            paths.addAll(anInterface.getUsedClassPaths());
        }
        for (TSVariableType variableType : variableTypes) {
            paths.addAll(variableType.getUsedClassPaths());
        }
        for (Code code : bodyCode) {
            paths.addAll(code.getUsedClassPaths());
        }
        if (superClass != null) paths.addAll(superClass.getUsedClassPaths());

        return paths;
    }

    @Override
    public List<String> formatRaw(Declaration declaration) {
        // Format head - export abstract (class / interface) name<T> extends ... implements ... {
        List<String> modifiers = new ArrayList<>();
        modifiers.add("export");
        if (isAbstract) modifiers.add("abstract");
        modifiers.add("class");

        String head = "%s %s".formatted(String.join(" ", modifiers), name);
        if (variableTypes.size() != 0) {
            String variables = variableTypes.stream()
                    .map(type -> type.line(declaration, BaseType.FormatType.VARIABLE))
                    .collect(Collectors.joining(", "));
            head = "%s<%s>".formatted(head, variables);
        }

        if (superClass != null) head = "%s extends %s".formatted(head, superClass.line(declaration));
        if (interfaces.size() != 0) {
            String formatted = interfaces.stream()
                    .map(type -> type.line(declaration))
                    .collect(Collectors.joining(", "));
            head = "%s implements %s".formatted(head, formatted);
        }
        head = "%s {".formatted(head);

        // Format body - fields, constructors, methods
        List<String> body = new ArrayList<>();

        for (FieldDecl field : fields) {
            body.addAll(field.format(declaration));
        }
        body.add("");
        for (ConstructorDecl constructor : constructors) {
            body.addAll(constructor.format(declaration));
        }
        body.add("");
        for (MethodDecl method : methods) {
            body.addAll(method.format(declaration));
        }

        // tail - custom code, }
        List<String> tail = new ArrayList<>();
        for (Code code : bodyCode) {
            tail.addAll(code.format(declaration));
        }
        tail.add("}");

        // Concatenate them as a whole
        List<String> formatted = new ArrayList<>();
        formatted.add(head);
        formatted.addAll(body);
        formatted.addAll(tail);
        return formatted;
    }
}
