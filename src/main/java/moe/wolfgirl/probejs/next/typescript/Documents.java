package moe.wolfgirl.probejs.next.typescript;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.java.ClassRegistry;
import moe.wolfgirl.probejs.next.typescript.document.AliasDecl;
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

public class Documents {
    public static final Documents INSTANCE = new Documents();

    private final Map<ClassPath, Code> documents = new HashMap<>();
    private final Multimap<ClassPath, Type> inputAlias = ArrayListMultimap.create();

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
        var allAlias = inputAlias.get(classPath);
        if (allAlias.isEmpty()) return null;
        List<Type> aliasList = new ArrayList<>(allAlias);
        aliasList.add(Types.clazz(classPath));
        var aliasDecl = new AliasDecl(classPath, Types.union(aliasList));
        aliasDecl.addComments("Values that may be interpreted as {@link %s}.".formatted(classPath.getClassName()));
        return aliasDecl;
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
