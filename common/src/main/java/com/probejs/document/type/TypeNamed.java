package com.probejs.document.type;

import com.probejs.document.Manager;
import com.probejs.formatter.NameResolver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TypeNamed implements IType {
    private final String typeName;

    public TypeNamed(String typeName) {
        this.typeName = typeName;
    }

    public String getRawTypeName() {
        return typeName;
    }

    @Override
    public String getTypeName() {
        NameResolver.ResolvedName resolved = NameResolver.resolvedNames.get(typeName);
        if (resolved == null)
            return typeName;
        return resolved.getFullName();
    }

    @Override
    public Set<String> getAssignableNames() {
        Set<String> assignableNames = new HashSet<>();
        assignableNames.add(getTypeName());
        Manager.typesAssignable.getOrDefault(getRawTypeName(), new ArrayList<>()).stream().map(IType::getTypeName).forEach(assignableNames::add);
        return assignableNames;
    }
}
