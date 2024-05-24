package moe.wolfgirl.next.typescript.parts.impl.types.complex;

import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.typescript.Reference;
import moe.wolfgirl.next.typescript.parts.impl.types.TypeTS;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class JointTS extends TypeTS {

    private final List<TypeTS> components;
    private final String separator;

    private JointTS(List<TypeTS> components, String separator) {
        this.components = components;
        this.separator = separator;
    }

    @Override
    public String getType() {
        return components.stream().map(TypeTS::getType).collect(Collectors.joining(separator));
    }

    @Override
    public void setSymbols(Map<ClassPath, Reference> symbols) {
        super.setSymbols(symbols);
        for (TypeTS component : components) {
            component.setSymbols(symbols);
        }
    }

    public static class And extends JointTS {
        public And(List<TypeTS> components) {
            super(components, " & ");
        }
    }

    public static class Or extends JointTS {
        public Or(List<TypeTS> components) {
            super(components, " | ");
        }
    }
}
