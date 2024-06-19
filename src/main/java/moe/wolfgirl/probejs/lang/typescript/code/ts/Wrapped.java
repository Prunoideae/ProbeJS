package moe.wolfgirl.probejs.lang.typescript.code.ts;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.typescript.Declaration;
import moe.wolfgirl.probejs.lang.typescript.code.Code;
import moe.wolfgirl.probejs.lang.typescript.code.member.CommentableCode;

import java.util.*;

public abstract class Wrapped extends CommentableCode {
    public final List<Code> codes = new ArrayList<>();

    public void addCode(Code inner) {
        this.codes.add(inner);
    }

    @Override
    public Collection<ClassPath> getUsedClassPaths() {
        Set<ClassPath> innerPaths = new HashSet<>();
        for (Code code : codes) {
            innerPaths.addAll(code.getUsedClassPaths());
        }
        return innerPaths;
    }

    @Override
    public List<String> formatRaw(Declaration declaration) {
        List<String> lines = new ArrayList<>();
        for (Code code : codes) {
            lines.addAll(code.format(declaration));
        }
        return lines;
    }

    public boolean isEmpty() {
        return codes.isEmpty();
    }

    public void merge(Wrapped other) {
        this.codes.addAll(other.codes);
    }


    public static class Global extends Wrapped {
        @Override
        public List<String> formatRaw(Declaration declaration) {
            List<String> lines = new ArrayList<>();
            lines.add("declare global {");
            lines.addAll(super.formatRaw(declaration));
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
        public List<String> formatRaw(Declaration declaration) {
            List<String> lines = new ArrayList<>();
            lines.add("export namespace %s {".formatted(nameSpace));
            lines.addAll(super.formatRaw(declaration));
            lines.add("}");
            return lines;
        }
    }
}
