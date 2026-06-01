package moe.wolfgirl.probejs.plugin.builtins;

import moe.wolfgirl.probejs.typescript.ClassPath;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.typescript.Documents;
import moe.wolfgirl.probejs.typescript.document.base.Code;
import moe.wolfgirl.probejs.typescript.document.base.Type;
import moe.wolfgirl.probejs.typescript.document.members.MethodDecl;
import moe.wolfgirl.probejs.typescript.document.types.ClassType;
import moe.wolfgirl.probejs.typescript.document.types.ParamType;

import java.util.*;

// Iterables and Maps
public class InjectIndex extends ProbeJSPlugin {
    private static final ClassPath ITERATOR = new ClassPath(Iterator.class);

    @Override
    public void transformClass(Documents.ClassDocument document) {
        var classInfo = document.classInfo();
        var classDocument = document.document();

        if (Iterable.class.isAssignableFrom(classInfo.clazz())) {
            // iterator should be correctly typed, so we search for it and find the type as the [symbol.iterator] return type
            var iterator = classDocument.members.stream()
                    .filter(m -> m instanceof MethodDecl method && method.name.equals("iterator") && method.params.isEmpty())
                    .map(m -> (MethodDecl) m)
                    .findFirst()
                    .orElse(null);
            if (iterator == null) return; // superclass has implemented the method, we don't need to do anything
            var iteratorType = iterator.returnType;
            if (iteratorType instanceof ParamType paramType &&
                    paramType.baseType instanceof ClassType classType &&
                    classType.classPath.equals(ITERATOR)) {
                var elementType = paramType.typeArgs.getFirst();
                classDocument.members.add(new IterableIndex(elementType));
            }
        }
    }

    private static class IterableIndex extends Code {
        private final Type elementType;

        private IterableIndex(Type elementType) {
            this.elementType = elementType;
        }

        @Override
        public Set<ClassPath> getImports() {
            return elementType.getImports();
        }

        @Override
        public List<String> format(int indent) {
            return List.of("%s[Symbol.iterator](): Iterator<%s>".formatted(" ".repeat(indent), elementType.first()));
        }

        @Override
        public void setResolvedSymbols(Map<ClassPath, String> resolvedSymbols) {
            elementType.setResolvedSymbols(resolvedSymbols);
        }
    }

    private static class MapIndex extends Code {
        private final Type valueType;

        private MapIndex(Type valueType) {
            this.valueType = valueType;
        }

        @Override
        public Set<ClassPath> getImports() {
            return valueType.getImports();
        }

        @Override
        public List<String> format(int indent) {
            return List.of("%s[key: string]: %s | undefined | ((...args: any[]) => any)".formatted(
                    " ".repeat(indent),
                    valueType.first())
            );
        }

        @Override
        public void setResolvedSymbols(Map<ClassPath, String> resolvedSymbols) {
            valueType.setResolvedSymbols(resolvedSymbols);
        }
    }
}