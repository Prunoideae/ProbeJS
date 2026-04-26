package moe.wolfgirl.probejs.next.typescript.document.members;

import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.typescript.document.base.CommentableCode;
import moe.wolfgirl.probejs.next.typescript.document.base.Type;

import java.util.List;
import java.util.Set;

// Represents a field declaration in a TypeScript class.
// fieldName: type;
public class FieldDecl extends CommentableCode {
    public String name;
    public Type typeInfo;
    public boolean isStatic;
    public boolean isInterface;

    public FieldDecl(String name, Type typeInfo, boolean isStatic) {
        this.name = name;
        this.typeInfo = typeInfo;
        this.isStatic = isStatic;
    }

    @Override
    public Set<ClassPath> getImports() {
        return typeInfo.getImports();
    }

    @Override
    public List<String> format(int indent) {
        return List.of("%s%s%s: %s;".formatted(" ".repeat(indent), isStatic ? isInterface ? "let " : "static " : "", name, typeInfo.first()));
    }
}
