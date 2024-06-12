package moe.wolfgirl.next.transpiler;

import moe.wolfgirl.next.java.clazz.Clazz;
import moe.wolfgirl.next.java.type.impl.VariableType;
import moe.wolfgirl.next.transpiler.members.Constructor;
import moe.wolfgirl.next.transpiler.members.Converter;
import moe.wolfgirl.next.transpiler.members.Field;
import moe.wolfgirl.next.transpiler.members.Method;
import moe.wolfgirl.next.typescript.code.member.ClassDecl;
import moe.wolfgirl.next.typescript.code.member.InterfaceDecl;
import moe.wolfgirl.next.typescript.code.type.BaseType;
import moe.wolfgirl.next.typescript.code.type.TSVariableType;
import moe.wolfgirl.next.typescript.code.type.Types;

import java.util.ArrayList;
import java.util.List;

public class ClassTranspiler extends Converter<Clazz, ClassDecl> {

    private final Method method;
    private final Field field;
    private final Constructor constructor;

    public ClassTranspiler(TypeConverter converter) {
        super(converter);
        this.method = new Method(converter);
        this.field = new Field(converter);
        this.constructor = new Constructor(converter);
    }

    @Override
    public ClassDecl transpile(Clazz input) {
        List<TSVariableType> variableTypes = new ArrayList<>();
        for (VariableType variableType : input.variableTypes) {
            variableTypes.add((TSVariableType) converter.convertType(variableType));
        }
        BaseType superClass = input.superClass == null ? null : converter.convertType(input.superClass);
        ClassDecl decl =
                input.attribute.isInterface ?
                        new InterfaceDecl(input.classPath.getName(),
                                superClass == Types.ANY ? null : superClass,
                                input.interfaces.stream()
                                        .map(converter::convertType)
                                        .filter(t -> t != Types.ANY)
                                        .toList(),
                                variableTypes) :
                        new ClassDecl(input.classPath.getName(),
                                superClass == Types.ANY ? null : superClass,
                                input.interfaces.stream()
                                        .map(converter::convertType)
                                        .filter(t -> t != Types.ANY)
                                        .toList(),
                                variableTypes
                        );

        decl.fields.addAll(input.fields.stream().map(field::transpile).toList());
        decl.methods.addAll(input.methods.stream().map(method::transpile).toList());
        decl.constructors.addAll(input.constructors.stream().map(constructor::transpile).toList());
        decl.isAbstract = input.attribute.isAbstract;

        return decl;
    }
}
