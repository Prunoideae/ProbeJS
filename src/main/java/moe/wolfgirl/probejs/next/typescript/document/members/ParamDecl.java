package moe.wolfgirl.probejs.next.typescript.document.members;

import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.typescript.document.base.Code;
import moe.wolfgirl.probejs.next.typescript.document.base.Type;

import java.util.List;
import java.util.Set;

// A parameter declaration in method or constructor
// param1: Type1
// ...args: Type2[]
public class ParamDecl extends Code {
    private static final Set<String> RESERVED_NAMES = Set.of(
            "in", "with", "as", "async", "await", "break", "case", "catch", "class", "const", "continue", "debugger",
            "default", "delete", "do", "else", "enum", "export", "extends", "finally", "for", "function", "if", "import",
            "instanceof", "new", "return", "super", "switch", "this", "throw", "try", "typeof", "var", "void", "while"
    );

    public String name;
    public Type typeInfo;
    public boolean isRest;

    public ParamDecl(String name, Type typeInfo, boolean isRest) {
        this.name = name;
        this.typeInfo = typeInfo;
        this.isRest = isRest;
    }

    public ParamDecl(String name, Type typeInfo) {
        this(name, typeInfo, false);
    }

    @Override
    public Set<ClassPath> getImports() {
        return typeInfo.getImports();
    }

    @Override
    public List<String> format(int indent) {
        if (RESERVED_NAMES.contains(name)) {
            name = "_" + name;
        }
        return List.of("%s%s: %s".formatted(isRest ? "..." : "", name, typeInfo.first()));
    }
}
