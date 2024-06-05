package moe.wolfgirl.next.transpiler.members;

import moe.wolfgirl.next.java.clazz.members.ConstructorInfo;
import moe.wolfgirl.next.java.type.impl.VariableType;
import moe.wolfgirl.next.transpiler.TypeConverter;
import moe.wolfgirl.next.typescript.code.member.ConstructorDecl;
import moe.wolfgirl.next.typescript.code.type.TSVariableType;

import java.util.ArrayList;
import java.util.List;

public class Constructor extends Converter<ConstructorInfo, ConstructorDecl> {
    private final Param param;

    public Constructor(TypeConverter converter) {
        super(converter);
        this.param = new Param(converter);
    }

    @Override
    public ConstructorDecl transpile(ConstructorInfo input) {
        List<TSVariableType> variableTypes = new ArrayList<>();
        for (VariableType variableType : input.variableTypes) {
            variableTypes.add((TSVariableType) converter.convertType(variableType));
        }
        return new ConstructorDecl(
                variableTypes,
                input.params.stream().map(param::transpile).toList()
        );
    }
}
