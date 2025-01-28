package moe.wolfgirl.probejs.lang.java;

import dev.latvian.mods.rhino.type.ArrayTypeInfo;
import dev.latvian.mods.rhino.type.ParameterizedTypeInfo;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.latvian.mods.rhino.type.VariableTypeInfo;

import java.util.Arrays;

public class TypeAdapter {
    public static TypeInfo consolidateType(TypeInfo in, String symbol, TypeInfo replacement) {
        return switch (in) {
            case VariableTypeInfo variableTypeInfo -> variableTypeInfo.getName().equals(symbol) ? replacement : in;
            case ArrayTypeInfo arrayTypeInfo ->
                    consolidateType(arrayTypeInfo.componentType(), symbol, replacement).asArray();
            case ParameterizedTypeInfo parameterizedTypeInfo -> parameterizedTypeInfo.rawType().withParams(
                    Arrays.stream(parameterizedTypeInfo.params())
                            .map(p -> consolidateType(p, symbol, replacement))
                            .toArray(TypeInfo[]::new)
            );
            case null, default -> in;
        };
    }
}
