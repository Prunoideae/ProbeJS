package moe.wolfgirl.probejs.next.typescript.transpiler.members;

import dev.latvian.mods.rhino.type.TypeInfo;
import moe.wolfgirl.probejs.next.java.members.MethodInfo;
import moe.wolfgirl.probejs.next.typescript.document.members.MethodDecl;
import moe.wolfgirl.probejs.next.typescript.document.types.VariableType;
import moe.wolfgirl.probejs.next.typescript.transpiler.TypeConverter;

import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

public class MethodConverter extends Converter<MethodInfo, MethodDecl> {
    private final ParamConverter paramConverter;

    public MethodConverter(TypeConverter converter) {
        super(converter);
        this.paramConverter = new ParamConverter(converter);
    }

    @Override
    public MethodDecl convert(MethodInfo source) {
        List<VariableType> variableTypes = new ArrayList<>();
        for (TypeVariable<?> typeVariable : source.typeVariables()) {
            variableTypes.add((VariableType) converter.convertType(TypeInfo.of(typeVariable)));
        }

        return new MethodDecl(
                source.name(),
                variableTypes,
                new ArrayList<>(source.params().stream().map(paramConverter::convert).toList()),
                converter.convertType(source.returnType()),
                source.isStatic()
        );
    }
}
