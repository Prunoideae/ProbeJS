package com.probejs.document.type;

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

}
