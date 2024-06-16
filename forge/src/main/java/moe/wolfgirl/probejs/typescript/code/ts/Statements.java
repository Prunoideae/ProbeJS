package moe.wolfgirl.probejs.typescript.code.ts;

import moe.wolfgirl.probejs.typescript.code.member.ClassDecl;

public interface Statements {
    static MethodDeclaration.Builder method(String name) {
        return new MethodDeclaration.Builder(name);
    }

    static ClassDecl.Builder clazz(String name) {
        return new ClassDecl.Builder(name);
    }
}
