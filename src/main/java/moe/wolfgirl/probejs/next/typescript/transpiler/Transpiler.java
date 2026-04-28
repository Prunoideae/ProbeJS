package moe.wolfgirl.probejs.next.typescript.transpiler;

import com.mojang.datafixers.util.Pair;
import moe.wolfgirl.probejs.next.java.members.ClassInfo;
import moe.wolfgirl.probejs.next.java.members.ConstructorInfo;
import moe.wolfgirl.probejs.next.java.members.FieldInfo;
import moe.wolfgirl.probejs.next.java.members.MethodInfo;
import moe.wolfgirl.probejs.next.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.next.typescript.Documents;
import moe.wolfgirl.probejs.next.typescript.document.ClassDecl;
import moe.wolfgirl.probejs.next.typescript.document.base.Code;
import moe.wolfgirl.probejs.next.typescript.document.base.KindAware;
import moe.wolfgirl.probejs.next.typescript.document.base.Type;
import moe.wolfgirl.probejs.next.typescript.document.members.ConstructorDecl;
import moe.wolfgirl.probejs.next.typescript.document.members.FieldDecl;
import moe.wolfgirl.probejs.next.typescript.document.members.MethodDecl;
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

        List<Pair<FieldInfo, FieldDecl>> fieldPairs = new ArrayList<>();
        List<Pair<MethodInfo, MethodDecl>> methodPairs = new ArrayList<>();
        List<Pair<ConstructorInfo, ConstructorDecl>> constructorPairs = new ArrayList<>();

        List<Code> members = new ArrayList<>();
        for (var field : source.fields()) {
            var fieldDecl = this.field.convert(field);
            fieldDecl.setKind(isInterface ? KindAware.Kind.INTERFACE : KindAware.Kind.CLASS);
            members.add(fieldDecl);
            fieldPairs.add(Pair.of(field, fieldDecl));
        }
        for (var constructor : source.constructors()) {
            var constructorDecl = this.constructor.convert(constructor);
            members.add(constructorDecl);
            constructorPairs.add(Pair.of(constructor, constructorDecl));
        }
        for (var method : source.methods()) {
            var methodDecl = this.method.convert(method);
            methodDecl.setKind(isInterface ? KindAware.Kind.INTERFACE : KindAware.Kind.CLASS);
            members.add(methodDecl);
            methodPairs.add(Pair.of(method, methodDecl));
        }

        var classDecl = new ClassDecl(
                true,
                source.attributes().isInterface() ? KindAware.Kind.INTERFACE : KindAware.Kind.CLASS,
                source.classPath().getClassName(),
                superClass,
                interfaces,
                variableTypes,
                members
        );

        ProbeJSPlugin.forEachPlugin(plugin -> plugin.transformClass(new Documents.ClassDocument(
                source, classDecl, converter,
                fieldPairs, constructorPairs, methodPairs
        )));

        return classDecl;
    }
}
