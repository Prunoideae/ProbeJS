package moe.wolfgirl.next.transpiler.members;

import moe.wolfgirl.next.java.clazz.members.ParamInfo;
import moe.wolfgirl.next.transpiler.TypeConverter;
import moe.wolfgirl.next.typescript.code.member.ParamDecl;

public class Param extends Converter<ParamInfo, ParamDecl> {
    public Param(TypeConverter converter) {
        super(converter);
    }

    @Override
    public ParamDecl transpile(ParamInfo input) {
        return new ParamDecl(input.name, converter.convertType(input.type), input.varArgs);
    }
}
