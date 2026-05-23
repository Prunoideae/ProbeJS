package moe.wolfgirl.probejs.plugin.builtins.extras;

import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.typescript.ClassPath;
import moe.wolfgirl.probejs.typescript.Documents;
import moe.wolfgirl.probejs.typescript.base.DocumentRegistrar;
import moe.wolfgirl.probejs.typescript.document.Members;
import moe.wolfgirl.probejs.typescript.document.TypeDecl;
import moe.wolfgirl.probejs.typescript.document.base.Code;
import moe.wolfgirl.probejs.typescript.document.base.KindAware;
import moe.wolfgirl.probejs.typescript.document.base.Type;
import moe.wolfgirl.probejs.typescript.document.builders.ClassBuilder;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.*;

/*
 * declare global{
 *     export namespace Internal{
 *         type Object = import(...).$Object
 *         type Object_ = import(...).$Object_
 *     }
 * }
 */
public class Internals extends ProbeJSPlugin {
    // Classes with these names will more likely to get Name rather than Name$Index
    public static final List<String> PRIORITY_PACKAGES = List.of(
            "net.minecraft",
            "net.neoforged",
            "dev.latvian",
            "java.util"
    );
    public static final ClassPath INTERNAL = ClassPath.special("docs.internal.Internal");

    @Override
    public void addSpecialDocuments(DocumentRegistrar registrar) {
        Map<ClassPath, Code> allTypes = Documents.INSTANCE.getAllDocuments();
        Map<ClassPath, List<Type>> allAlias = Documents.INSTANCE.getAllAlias();
        Map<String, MutableInt> nameCounter = new HashMap<>();

        ClassBuilder classBuilder = Members.clazz(INTERNAL).kind(KindAware.Kind.NAMESPACE);
        List<ClassPath> classPaths = allTypes.keySet()
                .stream()
                .sorted(Comparator.comparing(Internals::getOrdinal))
                .toList();

        for (ClassPath classPath : classPaths) {
            int count = nameCounter.computeIfAbsent(classPath.getClassName(), s -> new MutableInt()).getAndIncrement();
            var namePath = classPath;
            if (count > 0) namePath = classPath.withSuffix("$" + count);
            classBuilder.member(new TypeDecl(namePath, new ImportType(classPath)));
            if (allAlias.containsKey(classPath)) {
                classBuilder.member(new TypeDecl(namePath.withSuffix("_"), new ImportType(classPath.withSuffix("_"))));
            }
        }

        registrar.addGlobal(INTERNAL, classBuilder.build());
    }

    private static int getOrdinal(ClassPath classPath) {
        var firstTwo = classPath.subpath(2).asJavaPath();
        for (int i = 0; i < PRIORITY_PACKAGES.size(); i++) {
            if (PRIORITY_PACKAGES.get(i).equals(firstTwo)) return i;
        }
        return Integer.MAX_VALUE;
    }

    // This is used for self-contained imports, so no external imports are declared
    // in this way we avoid naming conflict hell
    // though we still need to resolve other names...
    public static class ImportType extends Type {
        private final ClassPath importFrom;

        public ImportType(ClassPath importFrom) {
            this.importFrom = importFrom;
        }

        @Override
        public Set<ClassPath> getImports() {
            return Set.of();
        }

        @Override
        public List<String> format(int indent) {
            var base = importFrom.getPackage().asTypePath();
            var name = importFrom.getClassName();
            return List.of("import(%s).%s".formatted(ProbeJS.GSON.toJson(base), name));
        }

        @Override
        public Collection<Code> getContainedTypes() {
            return List.of();
        }
    }
}
