package moe.wolfgirl.next.typescript.parts.impl;

import moe.wolfgirl.next.typescript.parts.Code;
import moe.wolfgirl.next.typescript.parts.impl.members.FieldDef;
import moe.wolfgirl.next.typescript.parts.impl.members.MethodDef;
import moe.wolfgirl.next.typescript.parts.impl.types.TypeTS;

import java.util.ArrayList;
import java.util.List;

public class ClassDef extends Code {
    public final String name;
    public final List<String> comments = new ArrayList<>();

    public TypeTS parent;
    public final List<TypeTS> interfaces = new ArrayList<>();
    public final List<TypeTS> generics = new ArrayList<>();

    public final List<ClassDef> classes = new ArrayList<>();
    public final List<FieldDef> fields = new ArrayList<>();
    public final List<MethodDef> methods = new ArrayList<>();

    public final ClassAttributes attributes = new ClassAttributes();


    public ClassDef(String name) {
        this.name = name;
    }

    @Override
    public List<String> getContent() {
        return null;
    }


    public static class ClassAttributes {
        public boolean isAbstract = false;
        public boolean isInterface = false;
    }
}
