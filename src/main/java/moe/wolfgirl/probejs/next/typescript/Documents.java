package moe.wolfgirl.probejs.next.typescript;

import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.java.ClassRegistry;
import moe.wolfgirl.probejs.next.typescript.document.AliasDecl;
import moe.wolfgirl.probejs.next.typescript.document.base.Code;
import moe.wolfgirl.probejs.next.typescript.document.base.Type;
import moe.wolfgirl.probejs.next.typescript.transpiler.Transpiler;
import moe.wolfgirl.probejs.next.typescript.transpiler.TypeConverter;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class Documents {
    public static final Documents INSTANCE = new Documents();

    private final Map<ClassPath, Code> documents = new HashMap<>();
    private final Map<ClassPath, AliasDecl> inputAlias = new HashMap<>();

    public void addDocument(ClassPath classPath, Code code) {
        documents.put(classPath, code);
    }

    public void addInputAlias(ClassPath classPath, AliasDecl code) {
        inputAlias.put(classPath, code);
    }

    public void transpile() {
        Transpiler transpiler = new Transpiler(new TypeConverter());
        for (var entry : ClassRegistry.INSTANCE.getAllClasses().entrySet()) {
            var classPath = entry.getKey();
            var classInfo = entry.getValue();

            if (documents.containsKey(classPath)) continue;
            Code document = transpiler.convert(classInfo);
            addDocument(classPath, document);
        }
    }

    @Nullable
    public Code getDocument(ClassPath classPath) {
        return documents.get(classPath);
    }

    @Nullable
    public Code getInputAlias(ClassPath classPath) {
        return inputAlias.get(classPath);
    }

    public boolean hasAlias(ClassPath classPath) {
        return inputAlias.containsKey(classPath);
    }

    public void clear() {
        documents.clear();
        inputAlias.clear();
    }

    @FunctionalInterface
    public interface AliasRegistrar {
        void addAlias(Class<?> clazz, Type type);
    }
}
