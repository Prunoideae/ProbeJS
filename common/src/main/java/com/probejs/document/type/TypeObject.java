package com.probejs.document.type;

import java.util.Map;
import java.util.stream.Collectors;

public class TypeObject implements IType {
    private final Map<String, IType> members;

    public TypeObject(Map<String, IType> members) {
        this.members = members;
    }

    @Override
    public String getTypeName() {
        return "{%s}".formatted(members
                .entrySet()
                .stream()
                .map(e -> "%s: %s".formatted(e.getKey(), e.getValue().getTypeName()))
                .collect(Collectors.joining(", ")));
    }

}
