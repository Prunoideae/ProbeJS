package moe.wolfgirl.probejs.next.typescript.code.member;

import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.next.java.clazz.ClassPath;
import moe.wolfgirl.probejs.next.typescript.Declaration;
import moe.wolfgirl.probejs.next.typescript.code.type.BaseType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class FieldDecl extends CommentableCode {
    public boolean isFinal = false;
    public boolean isStatic = false;
    public String name;
    public BaseType type;

    public FieldDecl(String name, BaseType type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public Collection<ClassPath> getUsedClassPaths() {
        return type.getUsedClassPaths();
    }

    @Override
    public List<String> formatRaw(Declaration declaration) {
        List<String> modifiers = new ArrayList<>();
        if (isFinal) modifiers.add("readonly");
        if (isStatic) modifiers.add("static");

        return List.of("%s %s: %s".formatted(
                String.join(" ", modifiers), ProbeJS.GSON.toJson(name), type.line(declaration, BaseType.FormatType.RETURN)
        ));
    }
}
