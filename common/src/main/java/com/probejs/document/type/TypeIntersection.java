package com.probejs.document.type;

import com.google.common.collect.Sets;

import java.util.Set;
import java.util.stream.Collectors;

public class TypeIntersection implements IType {
    private final IType leftType;
    private final IType rightType;

    public TypeIntersection(IType leftType, IType rightType) {
        this.leftType = leftType;
        this.rightType = rightType;
    }

    @Override
    public String getTypeName() {
        return leftType.getTypeName() + " & " + rightType.getTypeName();
    }

    @Override
    public Set<String> getAssignableNames() {
        return Sets.cartesianProduct(this.leftType.getAssignableNames(), this.rightType.getAssignableNames()).stream().map(l -> l.get(0) + " & " + l.get(1)).collect(Collectors.toSet());
    }


}
