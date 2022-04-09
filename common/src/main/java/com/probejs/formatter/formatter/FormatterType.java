package com.probejs.formatter.formatter;

import com.probejs.formatter.NameResolver;
import com.probejs.info.type.*;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FormatterType {
    private final ITypeInfo typeInfo;
    private final boolean useSpecial;
    private final BiFunction<ITypeInfo, String, String> transformer;

    public FormatterType(ITypeInfo typeInfo, boolean useSpecial, BiFunction<ITypeInfo, String, String> transformer) {
        this.typeInfo = typeInfo;
        this.useSpecial = useSpecial;
        this.transformer = transformer;
    }

    public FormatterType(ITypeInfo typeInfo, boolean useSpecial) {
        this(typeInfo, useSpecial, (t, s) -> s);
    }

    public FormatterType(ITypeInfo typeInfo) {
        this(typeInfo, true);
    }

    public String format(Integer indent, Integer stepIndent) {
        if (useSpecial) {
            Class<?> rawClass = typeInfo.getResolvedClass();
            if (NameResolver.specialTypeFormatters.containsKey(rawClass)) {
                return NameResolver.specialTypeFormatters.get(rawClass).apply(this.typeInfo);
            }
        }

        if (typeInfo instanceof TypeInfoClass)
            return transformer.apply(typeInfo, NameResolver.getResolvedName(typeInfo.getTypeName()).getFullName());
        if (typeInfo instanceof TypeInfoWildcard)
            return transformer.apply(typeInfo, new FormatterType(typeInfo.getBaseType(), useSpecial, transformer).format(indent, stepIndent));
        if (typeInfo instanceof TypeInfoVariable)
            return transformer.apply(typeInfo, typeInfo.getTypeName());
        if (typeInfo instanceof TypeInfoArray)
            return transformer.apply(typeInfo, new FormatterType(typeInfo.getBaseType(), useSpecial, transformer).format(indent, stepIndent) + "[]");
        if (typeInfo instanceof TypeInfoParameterized parType) {
            if (new FormatterType(parType.getBaseType(), useSpecial, transformer).format(0, 0).equals("any"))
                return transformer.apply(typeInfo, NameResolver.ResolvedName.UNRESOLVED.getFullName());
            return transformer.apply(typeInfo, new FormatterType(parType.getBaseType(), useSpecial, transformer).format(indent, stepIndent) +
                    "<%s>".formatted(parType
                            .getParamTypes()
                            .stream()
                            .map(p -> new FormatterType(p, useSpecial, transformer).format(indent, stepIndent))
                            .collect(Collectors.joining(", "))));
        }
        return "any";
    }
}
