package moe.wolfgirl.probejs.next.transpiler.members;


import moe.wolfgirl.probejs.next.java.clazz.members.FieldInfo;
import moe.wolfgirl.probejs.next.transpiler.TypeConverter;
import moe.wolfgirl.probejs.next.typescript.code.member.FieldDecl;

public class Field extends Converter<FieldInfo, FieldDecl> {
    public Field(TypeConverter converter) {
        super(converter);
    }

    @Override
    public FieldDecl transpile(FieldInfo input) {
        FieldDecl decl = new FieldDecl(input.name, converter.convertType(input.type));
        decl.isFinal = input.attributes.isFinal;
        decl.isStatic = input.attributes.isStatic;

        return decl;
    }
}
