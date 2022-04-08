package com.probejs.formatter.formatter;

import com.probejs.formatter.NameResolver;
import com.probejs.info.type.*;

import java.util.stream.Collectors;

public class FormatterType {
    private final ITypeInfo typeInfo;
    private final boolean useSpecial;

    public FormatterType(ITypeInfo typeInfo, boolean useSpecial) {
        this.typeInfo = typeInfo;
        this.useSpecial = useSpecial;
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
            return NameResolver.getResolvedName(typeInfo.getTypeName()).getFullName();
        if (typeInfo instanceof TypeInfoWildcard)
            return new FormatterType(typeInfo.getBaseType(), useSpecial).format(indent, stepIndent);
        if (typeInfo instanceof TypeInfoVariable)
            return typeInfo.getTypeName();
        if (typeInfo instanceof TypeInfoArray)
            return new FormatterType(typeInfo.getBaseType(), useSpecial).format(indent, stepIndent) + "[]";
        if (typeInfo instanceof TypeInfoParameterized parType) {
            if (new FormatterType(parType.getBaseType(), useSpecial).format(0, 0).equals("any"))
                return NameResolver.ResolvedName.UNRESOLVED.getFullName();
            return new FormatterType(parType.getBaseType(), useSpecial).format(indent, stepIndent) +
                    "<%s>".formatted(parType
                            .getParamTypes()
                            .stream()
                            .map(p -> new FormatterType(p, useSpecial).format(indent, stepIndent))
                            .collect(Collectors.joining(", ")));
        }
        return "any";
    }
}
