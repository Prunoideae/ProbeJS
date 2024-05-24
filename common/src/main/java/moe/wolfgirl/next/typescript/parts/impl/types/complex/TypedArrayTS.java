package moe.wolfgirl.next.typescript.parts.impl.types.complex;

import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.typescript.Reference;
import moe.wolfgirl.next.typescript.parts.impl.types.TypeTS;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TypedArrayTS extends TypeTS {

    private final List<TypeTS> components;

    public TypedArrayTS(List<TypeTS> components) {
        this.components = components;
    }

    @Override
    public void setSymbols(Map<ClassPath, Reference> symbols) {
        super.setSymbols(symbols);
        for (TypeTS component : components) {
            component.setSymbols(symbols);
        }
    }

    @Override
    public String getType() {
        return "[%s]".formatted(
                components.stream()
                        .map(TypeTS::getType)
                        .collect(Collectors.joining(", "))
        );
    }
}
