package moe.wolfgirl.probejs.lang.java;

import dev.latvian.mods.rhino.type.ArrayTypeInfo;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.latvian.mods.rhino.type.VariableTypeInfo;

public class TypeAdapter {
    public static TypeInfo consolidateType(TypeInfo in, String symbol, TypeInfo replacement) {
        return switch (in) {
            case VariableTypeInfo variableTypeInfo -> variableTypeInfo.getName().equals(symbol) ? replacement : in;
            case ArrayTypeInfo arrayTypeInfo ->
                    consolidateType(arrayTypeInfo.componentType(), symbol, replacement).asArray();
            case null, default -> in;
        };
    }
}
