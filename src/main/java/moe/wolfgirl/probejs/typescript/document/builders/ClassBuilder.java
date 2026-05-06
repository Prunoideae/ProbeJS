package moe.wolfgirl.probejs.typescript.document.builders;

import moe.wolfgirl.probejs.typescript.document.ClassDecl;
import moe.wolfgirl.probejs.typescript.document.Members;
import moe.wolfgirl.probejs.typescript.document.Types;
import moe.wolfgirl.probejs.typescript.document.base.Code;
import moe.wolfgirl.probejs.typescript.document.base.KindAware;
import moe.wolfgirl.probejs.typescript.document.base.Type;
import moe.wolfgirl.probejs.typescript.document.members.FieldDecl;
import moe.wolfgirl.probejs.typescript.document.types.VariableType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ClassBuilder {
    private boolean export = true;
    private KindAware.Kind kind = KindAware.Kind.CLASS;
    private final String identifier;
    private Type extendsType = Types.NEVER;
    private final List<Type> implementsTypes = new ArrayList<>();
    private final List<VariableType> typeParams = new ArrayList<>();
    private final List<Code> members = new ArrayList<>();

    public ClassBuilder(String identifier) {
        this.identifier = identifier;
    }

    public ClassBuilder noExport() {
        this.export = false;
        return this;
    }

    public ClassBuilder kind(KindAware.Kind kind) {
        this.kind = kind;
        return this;
    }

    public ClassBuilder extendsType(Type extendsType) {
        this.extendsType = extendsType;
        return this;
    }

    public ClassBuilder extendsType(Class<?> extendsType) {
        return extendsType(Types.clazz(extendsType));
    }

    public ClassBuilder implementsType(Type implementsType) {
        this.implementsTypes.add(implementsType);
        return this;
    }

    public ClassBuilder implementsType(Class<?> implementsType) {
        return implementsType(Types.clazz(implementsType));
    }

    public ClassBuilder typeParam(String name, @Nullable Type bound) {
        this.typeParams.add(Types.variable(name, bound));
        return this;
    }

    public ClassBuilder typeParam(String name) {
        return typeParam(name, null);
    }

    public ClassBuilder member(Code member) {
        this.members.add(member);
        return this;
    }

    public ClassBuilder method(String name, Consumer<MethodBuilder> builder) {
        var methodBuilder = new MethodBuilder(name);
        builder.accept(methodBuilder);
        return member(methodBuilder.build(kind));
    }

    public ClassBuilder field(String name, Type type, boolean isStatic) {
        FieldDecl fieldDecl = new FieldDecl(name, type, isStatic);
        fieldDecl.setKind(kind);
        return member(fieldDecl);
    }

    public ClassBuilder field(String name, Class<?> type, boolean isStatic) {
        return field(name, Types.clazz(type), isStatic);
    }

    public ClassBuilder field(String name, Type type) {
        return field(name, type, false);
    }

    public ClassBuilder field(String name, Class<?> type) {
        return field(name, Types.clazz(type), false);
    }

    public ClassBuilder constructor(Consumer<ConstructorBuilder> builder) {
        var constructorBuilder = new ConstructorBuilder();
        builder.accept(constructorBuilder);
        return member(constructorBuilder.build());
    }

    public ClassDecl build() {
        for (Code member : members) {
            if (member instanceof KindAware kindAware) {
                kindAware.setKind(kind);
            }
        }
        return new ClassDecl(export, kind, identifier, extendsType, implementsTypes, typeParams, members);
    }
}
