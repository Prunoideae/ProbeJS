package moe.wolfgirl.probejs.lang.typescript.code.member;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.typescript.Declaration;
import moe.wolfgirl.probejs.lang.typescript.code.Code;
import moe.wolfgirl.probejs.lang.typescript.code.ts.MethodDeclaration;
import moe.wolfgirl.probejs.lang.typescript.code.ts.VariableDeclaration;
import moe.wolfgirl.probejs.lang.typescript.code.ts.Wrapped;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.TSVariableType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class InterfaceDecl extends ClassDecl {
    private final Wrapped.Namespace namespace;

    public InterfaceDecl(String name, @Nullable BaseType superClass, List<BaseType> interfaces, List<TSVariableType> variableTypes) {
        super(name, superClass, interfaces, variableTypes);
        this.namespace = new Wrapped.Namespace(name);

    }

    @Override
    public List<String> formatRaw(Declaration declaration) {
        for (MethodDecl method : methods) {
            method.isInterface = true;
        }
        // Format head - export interface name<T> extends ... {
        String head = "export interface %s".formatted(name);
        if (variableTypes.size() != 0) {
            String variables = variableTypes.stream().map(type -> type.line(declaration, BaseType.FormatType.VARIABLE)).collect(Collectors.joining(", "));
            head = "%s<%s>".formatted(head, variables);
        }
        if (interfaces.size() != 0) {
            String formatted = interfaces.stream().map(type -> type.line(declaration)).collect(Collectors.joining(", "));
            head = "%s extends %s".formatted(head, formatted);
        }
        head = "%s {".formatted(head);

        // Format body - fields, constructors, methods
        List<String> body = new ArrayList<>();

        for (FieldDecl field : fields) {
            // if (!field.isStatic) throw new RuntimeException("Why an interface can have a non-static field?");
            // Because ProbeJS can add non-static fields to it... And it's legal in TypeScript.
            namespace.addCode(new VariableDeclaration(
                    field.name,
                    field.type
            ));
        }
        body.add("");
        for (MethodDecl method : methods) {
            if (method.isStatic) namespace.addCode(new MethodDeclaration(
                    method.name,
                    method.variableTypes,
                    method.params,
                    method.returnType
            ));
            else body.addAll(method.format(declaration));
        }

        // Adds a marker in it to prevent VSCode from not recognizing the namespace to import
        if (namespace.isEmpty()) {
            namespace.addCode(new Code() {
                @Override
                public Collection<ClassPath> getUsedClassPaths() {
                    return List.of();
                }

                @Override
                public List<String> format(Declaration declaration) {
                    return List.of("const probejs$$marker: never");
                }
            });
        }

        // Use hybrid to represent functional interfaces
        // (a: SomeClass<number>, b: SomeClass<string>): void;
        if (methods.stream().filter(method -> method.isAbstract).count() == 1) {
            body.add("");
            MethodDecl method = methods.get(0);
            String hybridBody = ParamDecl.formatParams(method.params, declaration);
            String returnType = method.returnType.line(declaration);

            body.add("%s: %s".formatted(hybridBody, returnType));
        }

        // tail - }


        // Concatenate them as a whole
        List<String> formatted = new ArrayList<>();
        formatted.add(head);
        formatted.addAll(body);
        formatted.add("}\n");

        // Static methods and fields, adds it even if it's empty, so auto import can still discover it
        formatted.addAll(namespace.format(declaration));
        return formatted;
    }
}
