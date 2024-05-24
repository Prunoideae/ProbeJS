package moe.wolfgirl.next.typescript.parts.impl.members;

import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.typescript.Reference;
import moe.wolfgirl.next.typescript.parts.Code;
import moe.wolfgirl.next.typescript.parts.impl.types.TypeTS;

import java.util.List;
import java.util.Map;

/**
 * type Something = ...
 */
public class TypeDef extends Code {

    private final String name;
    private final TypeTS type;

    public TypeDef(String name, TypeTS type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public void setSymbols(Map<ClassPath, Reference> symbols) {
        super.setSymbols(symbols);
        type.setSymbols(symbols);
    }

    @Override
    public List<String> getContent() {
        return List.of("type %s = %s;".formatted(name, type.getType()));
    }
}
