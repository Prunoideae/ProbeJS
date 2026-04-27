package moe.wolfgirl.probejs.next.typescript.transpiler;

import moe.wolfgirl.probejs.next.java.members.ClassInfo;
import moe.wolfgirl.probejs.next.typescript.document.ClassDecl;
import moe.wolfgirl.probejs.next.typescript.document.base.Code;
import moe.wolfgirl.probejs.next.typescript.document.base.KindAware;
import moe.wolfgirl.probejs.next.typescript.document.base.Type;
import moe.wolfgirl.probejs.next.typescript.document.types.VariableType;
import moe.wolfgirl.probejs.next.typescript.transpiler.members.ConstructorConverter;
import moe.wolfgirl.probejs.next.typescript.transpiler.members.Converter;
import moe.wolfgirl.probejs.next.typescript.transpiler.members.FieldConverter;
import moe.wolfgirl.probejs.next.typescript.transpiler.members.MethodConverter;

import java.util.ArrayList;
import java.util.List;

public class Transpiler extends Converter<ClassInfo, ClassDecl> {
    private final FieldConverter field;
    private final ConstructorConverter constructor;
    private final MethodConverter method;

    public Transpiler(TypeConverter converter) {
        super(converter);
        this.field = new FieldConverter(converter);
        this.constructor = new ConstructorConverter(converter);
        this.method = new MethodConverter(converter);
    }

    @Override
    public ClassDecl convert(ClassInfo source) {
        boolean isInterface = source.attributes().isInterface();
        Type superClass = converter.convertType(source.superClass());
        List<Type> interfaces = source.interfaces().stream().map(converter::convertType).toList();
        List<VariableType> variableTypes = source.asVariableTypeInfos()
                .stream()
                .map(converter::convertType)
                .map(c -> (VariableType) c)
                .toList();

        List<Code> members = new ArrayList<>();
        for (var field : source.fields()) {
            var fieldDecl = this.field.convert(field);
            fieldDecl.setKind(isInterface ? KindAware.Kind.INTERFACE : KindAware.Kind.CLASS);
            members.add(fieldDecl);
        }
        for (var constructor : source.constructors()) {
            members.add(this.constructor.convert(constructor));
        }
        for (var method : source.methods()) {
            var methodDecl = this.method.convert(method);
            methodDecl.setKind(isInterface ? KindAware.Kind.INTERFACE : KindAware.Kind.CLASS);
            members.add(methodDecl);
        }

        return new ClassDecl(
                true,
                source.attributes().isInterface() ? KindAware.Kind.INTERFACE : KindAware.Kind.CLASS,
                source.classPath().getClassName(),
                superClass,
                interfaces,
                variableTypes,
                members
        );
    }
}
