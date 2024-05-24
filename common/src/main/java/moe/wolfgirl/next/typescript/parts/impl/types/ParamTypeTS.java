package moe.wolfgirl.next.typescript.parts.impl.types;

import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.typescript.Reference;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ParamTypeTS extends TypeTS {
    private final TypeTS base;
    private final List<TypeTS> params;

    public ParamTypeTS(TypeTS base, List<TypeTS> params) {
        this.base = base;
        this.params = params;
    }

    @Override
    public void setSymbols(Map<ClassPath, Reference> symbols) {
        super.setSymbols(symbols);
        base.setSymbols(symbols);
        for (TypeTS param : params) {
            param.setSymbols(symbols);
        }
    }

    @Override
    public String getType() {
        return "%s<%s>".formatted(
                getMaybeWrapped(base),
                params.stream()
                        .map(TypeTS::getType)
                        .collect(Collectors.joining(", "))
        );
    }
}
