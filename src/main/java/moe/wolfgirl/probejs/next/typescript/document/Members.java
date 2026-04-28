package moe.wolfgirl.probejs.next.typescript.document;

import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.typescript.document.base.Type;
import moe.wolfgirl.probejs.next.typescript.document.builders.ClassBuilder;
import moe.wolfgirl.probejs.next.typescript.document.builders.ConstructorBuilder;
import moe.wolfgirl.probejs.next.typescript.document.builders.MethodBuilder;
import moe.wolfgirl.probejs.next.typescript.document.members.FieldDecl;

public interface Members {

    static ClassBuilder clazz(ClassPath classPath) {
        return new ClassBuilder(classPath.getClassName());
    }

    static MethodBuilder method(String identifier) {
        return new MethodBuilder(identifier);
    }

    static ConstructorBuilder constructor() {
        return new ConstructorBuilder();
    }

    static FieldDecl field(String name, Type type, boolean isStatic) {
        return new FieldDecl(name, type, isStatic);
    }

    static FieldDecl field(String name, Type type) {
        return field(name, type, false);
    }

    static FieldDecl field(String name, Class<?> type, boolean isStatic) {
        return field(name, Types.clazz(type), isStatic);
    }

    static FieldDecl field(String name, Class<?> type) {
        return field(name, Types.clazz(type), false);
    }
}
