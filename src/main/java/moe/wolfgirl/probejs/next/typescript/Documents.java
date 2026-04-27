package moe.wolfgirl.probejs.next.typescript;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.java.ClassRegistry;
import moe.wolfgirl.probejs.next.java.PackageTree;
import moe.wolfgirl.probejs.next.java.members.ClassInfo;
import moe.wolfgirl.probejs.next.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.next.typescript.base.DocumentRegistrar;
import moe.wolfgirl.probejs.next.typescript.base.DocumentRegistry;
import moe.wolfgirl.probejs.next.typescript.document.AliasDecl;
import moe.wolfgirl.probejs.next.typescript.document.ClassDecl;
import moe.wolfgirl.probejs.next.typescript.document.Types;
import moe.wolfgirl.probejs.next.typescript.document.base.Code;
import moe.wolfgirl.probejs.next.typescript.document.base.Type;
import moe.wolfgirl.probejs.next.typescript.transpiler.Transpiler;
import moe.wolfgirl.probejs.next.typescript.transpiler.TypeConverter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Documents implements DocumentRegistry, DocumentRegistrar {
    public static final Documents INSTANCE = new Documents();

    private final Map<ClassPath, Code> documents = new HashMap<>();
    private final Multimap<ClassPath, Type> inputAlias = ArrayListMultimap.create();

    @Override
    public void addGlobal(ClassPath classPath, Code code) {
        throw new UnsupportedOperationException("Global declarations are not supported in Documents. Use SpecialDocuments instead.");
    }

    public void addDocument(ClassPath classPath, Code code) {
        documents.put(classPath, code);
    }

    public void addInputAlias(ClassPath classPath, Type alias) {
        inputAlias.put(classPath, alias);
    }

    public void addInputAlias(Class<?> clazz, Type alias) {
        addInputAlias(new ClassPath(clazz), alias);
    }

    public void transpile() {
        Transpiler transpiler = new Transpiler(new TypeConverter());
        for (var entry : ClassRegistry.INSTANCE.getAllClasses().entrySet()) {
            var classPath = entry.getKey();
            var classInfo = entry.getValue();

            if (documents.containsKey(classPath)) continue;
            ClassDecl document = transpiler.convert(classInfo);
            ProbeJSPlugin.forEachPlugin(plugin -> plugin.transformClass(new ClassDocument(classInfo, document)));
            addDocument(classPath, document);
        }
    }

    @Nullable
    public Code getDocument(ClassPath classPath) {
        return documents.get(classPath);
    }

    @Nullable
    public Code getInputAlias(ClassPath classPath) {
        var allAlias = inputAlias.get(classPath);
        if (allAlias.isEmpty()) return null;
        List<Type> aliasList = new ArrayList<>(allAlias);
        aliasList.add(Types.clazz(classPath));
        var aliasDecl = new AliasDecl(classPath, Types.union(aliasList));
        aliasDecl.addComments("Values that may be interpreted as {@link %s}.".formatted(classPath.getClassName()));
        return aliasDecl;
    }

    @Override
    public Code getGlobal(ClassPath classPath) {
        return null;
    }

    @Override
    public PackageTree resolveTree() {
        PackageTree tree = new PackageTree();
        for (var entry : documents.entrySet()) {
            tree.addClassPath(entry.getKey());
        }
        return tree;
    }

    public boolean hasAlias(ClassPath classPath) {
        return inputAlias.containsKey(classPath);
    }

    public void clear() {
        documents.clear();
        inputAlias.clear();
    }

    public record ClassDocument(ClassInfo classInfo, ClassDecl document) {

    }

    public static class ClassAccessor {
        private final Map<ClassPath, Code> documents;
        private final Map<ClassPath, ClassInfo> classInfo;

        public ClassAccessor(Map<ClassPath, Code> documents, Map<ClassPath, ClassInfo> classInfo) {
            this.documents = documents;
            this.classInfo = classInfo;
        }

        @Nullable
        public ClassDocument getDocument(ClassPath classPath) {
            var document = documents.get(classPath);
            var info = classInfo.get(classPath);
            if (document instanceof ClassDecl classDecl && info != null) {
                return new ClassDocument(info, classDecl);
            }
            return null;
        }

        @Nullable
        public ClassDocument getDocument(Class<?> clazz) {
            return getDocument(new ClassPath(clazz));
        }

        public void addClassDocument(ClassPath classPath, Code code) {
            documents.put(classPath, code);
        }

        public void removeClassDocument(ClassPath classPath, Code code) {
            documents.remove(classPath);
        }
    }
}
