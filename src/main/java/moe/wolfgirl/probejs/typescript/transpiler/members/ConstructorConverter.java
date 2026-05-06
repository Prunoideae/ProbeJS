package moe.wolfgirl.probejs.typescript.transpiler.members;

import dev.latvian.mods.rhino.type.TypeInfo;
import moe.wolfgirl.probejs.java.members.ConstructorInfo;
import moe.wolfgirl.probejs.typescript.document.members.ConstructorDecl;
import moe.wolfgirl.probejs.typescript.document.types.VariableType;
import moe.wolfgirl.probejs.typescript.transpiler.TypeConverter;

import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

public class ConstructorConverter extends Converter<ConstructorInfo, ConstructorDecl> {
    private final ParamConverter paramConverter;

    public ConstructorConverter(TypeConverter converter) {
        super(converter);
        this.paramConverter = new ParamConverter(converter);
    }

    @Override
    public ConstructorDecl convert(ConstructorInfo source) {
        List<VariableType> variableTypes = new ArrayList<>();
        for (TypeVariable<?> typeVariable : source.typeVariables()) {
            variableTypes.add((VariableType) converter.convertType(TypeInfo.of(typeVariable)));
        }

        return new ConstructorDecl(
                variableTypes,
                new ArrayList<>(source.params().stream().map(paramConverter::convert).toList())
        );
    }
}
