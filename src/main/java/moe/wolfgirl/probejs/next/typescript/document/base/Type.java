package moe.wolfgirl.probejs.next.typescript.document.base;

import moe.wolfgirl.probejs.next.ClassPath;

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
