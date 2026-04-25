package moe.wolfgirl.probejs.next.typescript.transpiler;

import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.typescript.document.AliasDecl;
import moe.wolfgirl.probejs.next.typescript.document.base.Code;
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

    @Nullable
    public Code getDocument(ClassPath classPath) {
        return documents.get(classPath);
    }

    @Nullable
    public Code getInputAlias(ClassPath classPath) {
        return inputAlias.get(classPath);
    }
}
