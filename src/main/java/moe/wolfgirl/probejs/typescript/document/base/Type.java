package moe.wolfgirl.probejs.typescript.document.base;

import moe.wolfgirl.probejs.typescript.ClassPath;

import java.util.Map;

public abstract class Type extends Code implements TypeDocument {

    @Override
    public void setResolvedSymbols(Map<ClassPath, String> resolvedSymbols) {
        super.setResolvedSymbols(resolvedSymbols);
        for (Code containedType : getContainedTypes()) {
            containedType.setResolvedSymbols(resolvedSymbols);
        }
    }
}
