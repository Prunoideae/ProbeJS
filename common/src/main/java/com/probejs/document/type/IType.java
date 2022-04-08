package com.probejs.document.type;

import java.util.Set;

public interface IType {
    String getTypeName();

    Set<String> getAssignableNames();
}
