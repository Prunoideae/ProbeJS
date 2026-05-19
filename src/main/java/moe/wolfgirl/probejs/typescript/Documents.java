package moe.wolfgirl.probejs.typescript;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import moe.wolfgirl.probejs.GameStates;
import moe.wolfgirl.probejs.java.ClassRegistry;
import moe.wolfgirl.probejs.java.PackageTree;
import moe.wolfgirl.probejs.java.members.ClassInfo;
import moe.wolfgirl.probejs.java.members.ConstructorInfo;
import moe.wolfgirl.probejs.java.members.FieldInfo;
import moe.wolfgirl.probejs.java.members.MethodInfo;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.typescript.base.DocumentRegistrar;
import moe.wolfgirl.probejs.typescript.base.DocumentRegistry;
import moe.wolfgirl.probejs.typescript.document.TypeDecl;
import moe.wolfgirl.probejs.typescript.document.ClassDecl;
import moe.wolfgirl.probejs.typescript.document.Types;
import moe.wolfgirl.probejs.typescript.document.base.Code;
import moe.wolfgirl.probejs.typescript.document.base.Type;
import moe.wolfgirl.probejs.typescript.document.members.ConstructorDecl;
import moe.wolfgirl.probejs.typescript.document.members.FieldDecl;
import moe.wolfgirl.probejs.typescript.document.members.MethodDecl;
import moe.wolfgirl.probejs.typescript.document.types.VariableType;
import moe.wolfgirl.probejs.typescript.transpiler.Transpiler;
import moe.wolfgirl.probejs.typescript.transpiler.TypeConverter;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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

    private void reportTranspileStatus(int documentCount) {
        GameStates.DUMP_STATE.setStatus(Component.literal("Transpiled %d classes".formatted(documentCount)));
    }

    public void transpile() {
        TypeConverter typeConverter = new TypeConverter();
        Transpiler transpiler = new Transpiler(typeConverter);
        Map<ClassPath, ClassInfo> allClasses = ClassRegistry.INSTANCE.getAllClasses();
        for (var entry : allClasses.entrySet()) {
            var classPath = entry.getKey();
            var classInfo = entry.getValue();

            if (documents.containsKey(classPath)) continue;
            ClassDecl document = transpiler.convert(classInfo);
            addDocument(classPath, document);
            reportTranspileStatus(documents.size());
        }

        ProbeJSPlugin.forEachWithPriority("modifyClasses", plugin -> plugin.modifyClasses(new ClassAccessor(documents, allClasses, typeConverter)));

        // Free ClassInfo data now that transpiling is complete;
        // PackageDump only reads from Documents, not ClassRegistry.
        ClassRegistry.INSTANCE.clear();
        System.gc();
    }

    @Nullable
    public Code getDocument(ClassPath classPath) {
        return documents.get(classPath);
    }

    @Nullable
    public Code getInputAlias(ClassPath classPath) {
        var allAlias = inputAlias.get(classPath);
        if (allAlias.isEmpty()) return null;
        var document = documents.get(classPath);
        if (document == null) return null;
        List<Type> aliasList = new ArrayList<>(allAlias);
        // Blame TypeScript's structural typing, if I include the original class as an alias, members of the original
        // class will show up in the suggestion when I type {
        // aliasList.add(Types.clazz(classPath));
        List<VariableType> classVariables = document instanceof ClassDecl classDecl ? classDecl.typeParams : List.of();
        var aliasDecl = new TypeDecl(classPath.withSuffix("_"), classVariables, Types.union(aliasList), true);
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
        return inputAlias.containsKey(classPath) && !inputAlias.get(classPath).isEmpty();
    }

    public void clear() {
        documents.clear();
        inputAlias.clear();
    }

    public record ClassDocument(ClassInfo classInfo, ClassDecl document, TypeConverter converter,
                                List<Pair<FieldInfo, FieldDecl>> fieldDocs,
                                List<Pair<ConstructorInfo, ConstructorDecl>> constructorDocs,
                                List<Pair<MethodInfo, MethodDecl>> methodDocs
    ) {
    }


    public static class ClassAccessor {
        private final Map<ClassPath, Code> documents;
        private final Map<ClassPath, ClassInfo> classInfo;
        public final TypeConverter converter;

        public ClassAccessor(Map<ClassPath, Code> documents, Map<ClassPath, ClassInfo> classInfo, TypeConverter converter) {
            this.documents = documents;
            this.classInfo = classInfo;
            this.converter = converter;
        }

        @Nullable
        public Code getDocument(ClassPath classPath) {
            return documents.get(classPath);
        }

        @Nullable
        public Code getDocument(Class<?> clazz) {
            return getDocument(new ClassPath(clazz));
        }

        @Nullable
        public ClassInfo getClassInfo(ClassPath classPath) {
            return classInfo.get(classPath);
        }

        @Nullable
        public ClassInfo getClassInfo(Class<?> clazz) {
            return getClassInfo(new ClassPath(clazz));
        }

        public Collection<ClassPath> getAllClassPaths() {
            return documents.keySet();
        }

        public void addClassDocument(ClassPath classPath, Code code) {
            documents.put(classPath, code);
        }

        public void removeClassDocument(ClassPath classPath, Code code) {
            documents.remove(classPath);
        }
    }
}
