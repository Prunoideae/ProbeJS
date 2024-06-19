package moe.wolfgirl.probejs.lang.transpiler.members;

import moe.wolfgirl.probejs.lang.java.clazz.members.ParamInfo;
import moe.wolfgirl.probejs.lang.transpiler.TypeConverter;
import moe.wolfgirl.probejs.lang.typescript.code.member.ParamDecl;

public class Param extends Converter<ParamInfo, ParamDecl> {
    public Param(TypeConverter converter) {
        super(converter);
    }

    @Override
    public ParamDecl transpile(ParamInfo input) {
        return new ParamDecl(input.name, converter.convertType(input.type), input.varArgs, false);
    }
}
