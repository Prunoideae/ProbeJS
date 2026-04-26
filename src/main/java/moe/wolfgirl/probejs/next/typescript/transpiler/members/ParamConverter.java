package moe.wolfgirl.probejs.next.typescript.transpiler.members;


import moe.wolfgirl.probejs.next.java.members.other.ParamInfo;
import moe.wolfgirl.probejs.next.typescript.document.members.ParamDecl;
import moe.wolfgirl.probejs.next.typescript.transpiler.TypeConverter;

public class ParamConverter extends Converter<ParamInfo, ParamDecl> {
    public ParamConverter(TypeConverter converter) {
        super(converter);
    }

    @Override
    public ParamDecl convert(ParamInfo source) {
        return new ParamDecl(
                source.name(),
                converter.convertType(source.typeInfo()),
                source.varArgs()
        );
    }
}
