package moe.wolfgirl.next.transpiler.members;

import moe.wolfgirl.next.java.clazz.members.MethodInfo;
import moe.wolfgirl.next.java.type.impl.VariableType;
import moe.wolfgirl.next.transpiler.TypeConverter;
import moe.wolfgirl.next.typescript.code.member.MethodDecl;
import moe.wolfgirl.next.typescript.code.type.TSVariableType;

import java.util.ArrayList;
import java.util.List;

public class Method extends Converter<MethodInfo, MethodDecl> {
    private final Param param;

    public Method(TypeConverter converter) {
        super(converter);
        this.param = new Param(converter);
    }

    @Override
    public MethodDecl transpile(MethodInfo input) {
        List<TSVariableType> variableTypes = new ArrayList<>();
        for (VariableType variableType : input.variableTypes) {
            variableTypes.add((TSVariableType) converter.convertType(variableType));
        }
        MethodDecl decl = new MethodDecl(
                input.name,
                variableTypes,
                input.params.stream().map(this.param::transpile).toList(),
                converter.convertType(input.returnType)
        );
        decl.isAbstract = input.attributes.isAbstract;
        decl.isStatic = input.attributes.isStatic;

        return decl;
    }
}
