package moe.wolfgirl.probejs.lang.typescript.code.member;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.typescript.Declaration;
import moe.wolfgirl.probejs.lang.typescript.code.Code;
import moe.wolfgirl.probejs.lang.typescript.code.ImportInfo;
import moe.wolfgirl.probejs.lang.typescript.code.ts.MethodDeclaration;
import moe.wolfgirl.probejs.lang.typescript.code.ts.Statements;
import moe.wolfgirl.probejs.lang.typescript.code.ts.VariableDeclaration;
import moe.wolfgirl.probejs.lang.typescript.code.ts.Wrapped;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.TSVariableType;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class InterfaceDecl extends ClassDecl {

    public InterfaceDecl(String name, @Nullable BaseType superClass, List<BaseType> interfaces, List<TSVariableType> variableTypes) {
        super(name, superClass, interfaces, variableTypes);
    }

    @Override
    public boolean isInterface() {
        return true;
    }

    @Override
    public List<String> formatRaw(Declaration declaration) {
        for (MethodDecl method : methods) {
            method.isInterface = true;
        }
        // Format head - export interface name<T> extends ... {
        String head = "export interface %s".formatted(name);
        if (!variableTypes.isEmpty()) {
            String variables = variableTypes.stream().map(type -> type.line(declaration, BaseType.FormatType.VARIABLE)).collect(Collectors.joining(", "));
            head = "%s<%s>".formatted(head, variables);
        }
        if (!interfaces.isEmpty()) {
            String formatted = interfaces.stream().map(type -> type.line(declaration)).collect(Collectors.joining(", "));
            head = "%s extends %s".formatted(head, formatted);
        }
        head = "%s {".formatted(head);

        // Format body - fields, constructors, methods
        List<String> body = new ArrayList<>();
        Wrapped.Namespace namespace = new Wrapped.Namespace(name);

        for (FieldDecl field : fields) {
            namespace.addCode(new VariableDeclaration(field.name, field.type));
        }

        body.add("");
        for (MethodDecl method : methods) {
            if (!method.isStatic) body.addAll(method.format(declaration));
            else namespace.addCode(method.asDeclaration());
        }

        // Adds a marker in it to prevent VSCode from not recognizing the namespace to import
        namespace.addCode(new VariableDeclaration("probejs$$marker", Types.NEVER));

        // Use hybrid to represent functional interfaces
        // (a: SomeClass<number>, b: SomeClass<string>): void;
        MutableInt count = new MutableInt(0);
        MethodDecl hybrid = methods.stream()
                .filter(method -> !method.isStatic)
                .filter(method -> method.isAbstract)
                .peek(c -> count.add(1))
                .reduce((a, b) -> b)
                .orElse(null);

        if (count.getValue() == 1 && hybrid != null) {
            body.add("");
            String hybridBody = ParamDecl.formatParams(hybrid.params, declaration, BaseType.FormatType.RETURN);
            String returnType = hybrid.returnType.line(declaration, BaseType.FormatType.INPUT);
            body.add("%s: %s".formatted(hybridBody, returnType));
        }

        // tail - }
        List<String> tail = new ArrayList<>();
        for (Code code : bodyCode) {
            tail.addAll(code.format(declaration));
        }
        tail.add("}\n");

        // Concatenate them as a whole
        List<String> formatted = new ArrayList<>();
        formatted.add(head);
        formatted.addAll(body);
        formatted.addAll(tail);

        // Static methods and fields, adds it even if it's empty, so auto import can still discover it
        formatted.addAll(namespace.format(declaration));
        formatted.addAll(createStaticClass(name, methods, fields).format(declaration));
        return formatted;
    }

    public ClassDecl createStaticClass(String name, List<MethodDecl> methodDecls, List<FieldDecl> fieldDecls) {
        ClassDecl classDecl = new ClassDecl(
                ImportInfo.STATIC_TEMPLATE.formatted(name),
                null, List.of(Types.primitive(name)),
                this.variableTypes
        );
        classDecl.methods.addAll(methodDecls);
        classDecl.fields.addAll(fieldDecls);

        return classDecl;
    }
}
