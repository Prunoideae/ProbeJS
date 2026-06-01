package moe.wolfgirl.probejs.typescript.transpiler.members;


import moe.wolfgirl.probejs.java.members.other.ParamInfo;
import moe.wolfgirl.probejs.typescript.document.Types;
import moe.wolfgirl.probejs.typescript.document.members.ParamDecl;
import moe.wolfgirl.probejs.typescript.transpiler.TypeConverter;

import javax.annotation.Nullable;

public class ParamConverter extends Converter<ParamInfo, ParamDecl> {
    public ParamConverter(TypeConverter converter) {
        super(converter);
    }

    @Override
    public ParamDecl convert(ParamInfo source) {
        var type = converter.convertType(source.typeInfo());
        return new ParamDecl(
                source.name(),
                source.hasAnnotation(Nullable.class) ? Types.union(type, Types.NULL) : type,
                source.varArgs()
        );
    }
}
