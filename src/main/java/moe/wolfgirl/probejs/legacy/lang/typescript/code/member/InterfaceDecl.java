package moe.wolfgirl.probejs.legacy.lang.typescript.code.member;

import moe.wolfgirl.probejs.legacy.lang.typescript.Declaration;
import moe.wolfgirl.probejs.legacy.lang.typescript.code.Code;
import moe.wolfgirl.probejs.legacy.lang.typescript.code.ImportInfo;
import moe.wolfgirl.probejs.legacy.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.legacy.lang.typescript.code.type.TSVariableType;
import moe.wolfgirl.probejs.legacy.lang.typescript.code.type.Types;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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
    public Collection<ImportInfo> getUsedImports() {
        boolean seen = false;
        for (MethodDecl method : methods) {
            if (!method.isStatic && method.isAbstract) {
                if (seen) return super.getUsedImports();
                seen = true;
            }
        }
        if (!seen) return super.getUsedImports();


        Set<ImportInfo> paths = new HashSet<>();
        for (FieldDecl field : fields) {
            paths.addAll(field.getUsedImports());
        }
        for (ConstructorDecl constructor : constructors) {
            paths.addAll(constructor.getUsedImports());
        }
        for (MethodDecl method : methods) {
            if (method.isAbstract && !method.isStatic) {
                for (ImportInfo usedImport : method.getUsedImports()) {
                    paths.add(usedImport.asType(ImportInfo.Type.ORIGINAL));
                }
            } else {
                paths.addAll(method.getUsedImports());
            }
        }
        for (BaseType anInterface : interfaces) {
            paths.addAll(anInterface.getUsedImports());
        }
        for (TSVariableType variableType : variableTypes) {
            paths.addAll(variableType.getUsedImports());
        }
        for (Code code : bodyCode) {
            paths.addAll(code.getUsedImports());
        }
        if (superClass != null) paths.addAll(superClass.getUsedImports());

        return paths;
    }

    /**
     * Format the document for interface.
     * <br>
     * Note that the normal body is formatted as Interface type, that is only used when implemented by others
     * <br>
     * The original name was exported as a class that has both static method and interface methods.
     */
    @Override
    public List<String> formatRaw(Declaration declaration) {
        for (MethodDecl method : methods) {
            method.isInterface = true;
        }
        // Format head - export interface name<T> extends ... {
        String head = "export interface %s$$Interface".formatted(name);
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
        formatted.addAll(createStaticClass(name, methods, fields).format(declaration));
        return formatted;
    }

    public ClassDecl createStaticClass(String name, List<MethodDecl> methodDecls, List<FieldDecl> fieldDecls) {
        ClassDecl classDecl = new ClassDecl(name, null, List.of(Types.primitive(ImportInfo.INTERFACE_TEMPLATE.formatted(name))), this.variableTypes);
        classDecl.methods.addAll(methodDecls);
        classDecl.fields.addAll(fieldDecls);
        return classDecl;
    }
}
