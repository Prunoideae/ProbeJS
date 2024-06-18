package moe.wolfgirl.probejs.events;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.typescript.TypeScriptFile;

import java.util.Map;
import java.util.function.Consumer;

public class TypingModificationEventJS extends ScriptEventJS {

    private final Map<ClassPath, TypeScriptFile> files;

    public TypingModificationEventJS(ScriptDump dump, Map<ClassPath, TypeScriptFile> files) {
        super(dump);
        this.files = files;
    }

    public void modify(Class<?> clazz, Consumer<TypeScriptFile> file) {
        TypeScriptFile ts = files.get(new ClassPath(clazz));
        if (ts != null) file.accept(ts);
    }
}
