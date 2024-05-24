package moe.wolfgirl.next.typescript.parts.impl.types;

import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.typescript.Reference;

import java.util.Map;

public class ArrayTS extends TypeTS {
    private final TypeTS component;

    public ArrayTS(TypeTS component) {
        this.component = component;
    }

    @Override
    public String getType() {
        return getMaybeWrapped(component) + "[]";
    }

    @Override
    public void setSymbols(Map<ClassPath, Reference> symbols) {
        super.setSymbols(symbols);
        component.setSymbols(symbols);
    }
}
