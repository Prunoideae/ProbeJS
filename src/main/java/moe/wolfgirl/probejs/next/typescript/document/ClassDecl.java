package moe.wolfgirl.probejs.next.typescript.document;

import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.typescript.document.base.Code;
import moe.wolfgirl.probejs.next.typescript.document.base.CommentableCode;
import moe.wolfgirl.probejs.next.typescript.document.base.KindAware;
import moe.wolfgirl.probejs.next.typescript.document.base.Type;
import moe.wolfgirl.probejs.next.typescript.document.members.FieldDecl;
import moe.wolfgirl.probejs.next.typescript.document.members.MethodDecl;
import moe.wolfgirl.probejs.next.typescript.document.types.VariableType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Represents a class/ declaration in TypeScript.
// (export) (class/interface/namespace) Identifier(<T1, T2>) (extends Base) (implements Interface1, Interface2) {
//   // members...
// }
public class ClassDecl extends CommentableCode {
    public boolean export;
    public KindAware.Kind kind;
    public String identifier;
    public Type extendsType;
    public List<Type> implementsTypes;
    public List<VariableType> typeParams;
    public List<Code> members;

    public ClassDecl(boolean export, KindAware.Kind kind, String identifier, Type extendsType, List<Type> implementsTypes, List<VariableType> typeParams, List<Code> members) {
        this.export = export;
        this.kind = kind;
        this.identifier = identifier;
        this.extendsType = extendsType;
        this.implementsTypes = implementsTypes;
        this.typeParams = typeParams;
        this.members = members;
    }

    @Override
    public Set<ClassPath> getImports() {
        Set<ClassPath> imports = new HashSet<>(extendsType.getImports());
        for (Code typeParam : typeParams) {
            imports.addAll(typeParam.getImports());
        }
        for (Code implementsType : implementsTypes) {
            imports.addAll(implementsType.getImports());
        }
        for (Code member : members) {
            imports.addAll(member.getImports());
        }
        return Set.copyOf(imports);
    }

    @Override
    public List<String> format(int indent) {
        sanitize();
        if (kind == KindAware.Kind.CLASS) return formatAsClass(indent);
        else if (kind == KindAware.Kind.INTERFACE) return formatAsInterface(indent);
        else if (kind == KindAware.Kind.NAMESPACE) return formatAsNamespace(indent);
        else throw new IllegalStateException("Unknown kind: %s".formatted(kind));
    }

    private List<String> formatAsClass(int indent) {
        List<String> formatted = new ArrayList<>();
        // (export) (class/interface/namespace) Identifier(<T1, T2>) (extends Base) (implements Interface1, Interface2) {
        String indentStr = " ".repeat(indent);
        String exportStr = export ? "export " : "";
        String typeParamsStr = typeParams.isEmpty() ? "" : "<%s>".formatted(String.join(", ", typeParams.stream().map(VariableType::formatWithBound).toList()));
        String extendsStr = extendsType == Types.NEVER ? "" : " extends %s".formatted(extendsType.first());
        String implementsStr = implementsTypes.isEmpty() ? "" : " implements %s".formatted(String.join(", ", implementsTypes.stream().map(Code::first).toList()));
        formatted.add("%s%sclass %s%s%s%s {".formatted(indentStr, exportStr, identifier, typeParamsStr, extendsStr, implementsStr));
        // members
        for (Code member : members) {
            formatted.addAll(CommentableCode.format(member, indent + 4));
        }
        formatted.add("%s}".formatted(" ".repeat(indent)));
        return formatted;
    }

    // Java interface is a real thing, but TS one disappears at runtime
    // We need to export both a class that implements the interface and
    // an interface to make it work with instanceof checks.
    // static members go to the class, others go to the interface
    private List<String> formatAsInterface(int indent) {

        List<String> formatted = new ArrayList<>();

        // (export) class Identifier {
        String indentStr = " ".repeat(indent);
        String exportStr = export ? "export " : "";
        String typeParamsStr = typeParams.isEmpty() ? "" : "<%s>".formatted(String.join(", ", typeParams.stream().map(VariableType::formatWithBound).toList()));
        formatted.add("%s%sclass %s%s {".formatted(indentStr, exportStr, identifier, typeParamsStr));
        for (Code member : members) {
            if (member instanceof FieldDecl fieldDecl && !fieldDecl.isStatic) continue;
            if (member instanceof MethodDecl methodDecl && !methodDecl.isStatic) continue;
            formatted.addAll(CommentableCode.format(member, indent + 4));
        }
        formatted.add("%s}".formatted(" ".repeat(indent)));

        // (export) interface Identifier(<T1, T2>) (extends Interface1, Interface2) {
        String extendsInterfaceStr = implementsTypes.isEmpty() ? "" : " extends %s".formatted(String.join(", ", implementsTypes.stream().map(Code::first).toList()));

        formatted.add("%s%sinterface %s%s%s {".formatted(indentStr, exportStr, identifier, typeParamsStr, extendsInterfaceStr));
        for (Code member : members) {
            if (member instanceof FieldDecl fieldDecl && fieldDecl.isStatic) continue;
            if (member instanceof MethodDecl methodDecl && methodDecl.isStatic) continue;
            formatted.addAll(CommentableCode.format(member, indent + 4));
        }
        formatted.add("%s}".formatted(" ".repeat(indent)));

        return formatted;
    }

    // Why do I need this?
    private List<String> formatAsNamespace(int indent) {
        List<String> formatted = new ArrayList<>();
        // (export) (class/interface/namespace) Identifier(<T1, T2>) (extends Base) (implements Interface1, Interface2) {
        String indentStr = " ".repeat(indent);
        String exportStr = export ? "export " : "";
        String typeParamsStr = typeParams.isEmpty() ? "" : "<%s>".formatted(String.join(", ", typeParams.stream().map(VariableType::formatWithBound).toList()));
        String extendsStr = extendsType == Types.NEVER ? "" : " extends %s".formatted(extendsType.first());
        String implementsStr = implementsTypes.isEmpty() ? "" : " implements %s".formatted(String.join(", ", implementsTypes.stream().map(Code::first).toList()));
        formatted.add("%s%snamespace %s%s%s%s {".formatted(indentStr, exportStr, identifier, typeParamsStr, extendsStr, implementsStr));
        // members
        for (Code member : members) {
            formatted.addAll(CommentableCode.format(member, indent + 4));
        }
        formatted.add("%s}".formatted(" ".repeat(indent)));
        return formatted;
    }

    private void sanitize() {
        if (kind == KindAware.Kind.NAMESPACE) {
            if (extendsType != Types.NEVER) throw new IllegalStateException("Namespace cannot have extends type");
            if (!implementsTypes.isEmpty()) throw new IllegalStateException("Namespace cannot have implements types");
        }

        if (kind == KindAware.Kind.INTERFACE) {
            if (extendsType != Types.NEVER) throw new IllegalStateException("Interface cannot have extends type");
        }
    }

}
