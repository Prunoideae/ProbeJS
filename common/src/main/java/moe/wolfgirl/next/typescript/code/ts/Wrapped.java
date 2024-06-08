package moe.wolfgirl.next.typescript.code.ts;

import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.typescript.Declaration;
import moe.wolfgirl.next.typescript.code.Code;

import java.util.*;

public abstract class Wrapped extends Code {
    protected final List<Code> inner = new ArrayList<>();

    public void addCode(Code inner) {
        this.inner.add(inner);
    }

    @Override
    public Collection<ClassPath> getUsedClassPaths() {
        Set<ClassPath> innerPaths = new HashSet<>();
        for (Code code : inner) {
            innerPaths.addAll(code.getUsedClassPaths());
        }
        return innerPaths;
    }

    @Override
    public List<String> format(Declaration declaration) {
        List<String> lines = new ArrayList<>();
        for (Code code : inner) {
            lines.addAll(code.format(declaration));
        }
        return lines;
    }

    public boolean isEmpty() {
        return inner.isEmpty();
    }

    public void merge(Wrapped other) {
        this.inner.addAll(other.inner);
    }


    public static class Global extends Wrapped {
        @Override
        public List<String> format(Declaration declaration) {
            List<String> lines = new ArrayList<>();
            lines.add("declare global {");
            lines.addAll(super.format(declaration));
            lines.add("}");
            return lines;
        }
    }

    public static class Namespace extends Wrapped {
        public final String nameSpace;

        public Namespace(String nameSpace) {
            this.nameSpace = nameSpace;
        }

        @Override
        public List<String> format(Declaration declaration) {
            List<String> lines = new ArrayList<>();
            lines.add("export namespace %s {".formatted(nameSpace));
            lines.addAll(super.format(declaration));
            lines.add("}");
            return lines;
        }
    }
}
