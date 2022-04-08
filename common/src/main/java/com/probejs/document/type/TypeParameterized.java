package com.probejs.document.type;

import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TypeParameterized implements IType {
    private final IType rawType;
    private final List<IType> paramTypes;

    public TypeParameterized(IType rawType, List<IType> paramTypes) {
        this.rawType = rawType;
        this.paramTypes = paramTypes;
    }

    public IType getRawType() {
        return rawType;
    }

    public List<IType> getParamTypes() {
        return paramTypes;
    }

    @Override
    public String getTypeName() {
        return "%s<%s>".formatted(rawType.getTypeName(), paramTypes.stream().map(IType::getTypeName).collect(Collectors.joining(", ")));
    }

    @Override
    public Set<String> getAssignableNames() {
        Set<String> baseType = rawType.getAssignableNames();
        List<Set<String>> paramTypes = getParamTypes().stream().map(IType::getAssignableNames).collect(Collectors.toList());
        Set<String> paramProducts = Sets.cartesianProduct(paramTypes).stream().map(l -> String.join(", ", l)).collect(Collectors.toSet());
        return Sets.cartesianProduct(baseType, paramProducts).stream().map(l -> "%s<%s>".formatted(l.get(0), l.get(1))).collect(Collectors.toSet());
    }


}
