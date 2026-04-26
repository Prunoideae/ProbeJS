package moe.wolfgirl.probejs.next.typescript.transpiler.members;

import moe.wolfgirl.probejs.next.java.members.FieldInfo;
import moe.wolfgirl.probejs.next.typescript.document.members.FieldDecl;
import moe.wolfgirl.probejs.next.typescript.transpiler.TypeConverter;

public class FieldConverter extends Converter<FieldInfo, FieldDecl> {
    public FieldConverter(TypeConverter converter) {
        super(converter);
    }

    @Override
    public FieldDecl convert(FieldInfo source) {
        return new FieldDecl(
                source.name(),
                converter.convertType(source.type()),
                source.isStatic()
        );
    }
}
