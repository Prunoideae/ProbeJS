package com.probejs.document.type;

import java.util.Set;
import java.util.function.BiFunction;

public interface IType {
    String getTypeName();

    default String getTransformedName(BiFunction<IType, String, String> transformer) {
        return transformer.apply(this, getTypeName());
    }
}
