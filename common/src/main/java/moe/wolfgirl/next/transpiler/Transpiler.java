package moe.wolfgirl.next.transpiler;

import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.plugin.ProbeJSPlugin;
import moe.wolfgirl.next.typescript.TypeScriptFile;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Converts a Clazz into a TypeScriptFile ready for dump.
 */
public class Transpiler {
    public final TypeConverter typeConverter = new TypeConverter();
    public final Set<ClassPath> rejectedClasses = new HashSet<>();

    public void reject(Class<?> clazz) {
        rejectedClasses.add(new ClassPath(clazz));
    }

    public Map<ClassPath, TypeScriptFile> dump() {
        ProbeJSPlugin.forEachPlugin(plugin -> {
            plugin.addPredefinedTypes(typeConverter);
            plugin.denyTypes(this);
        });

        ClassTranspiler transpiler = new ClassTranspiler(typeConverter);

        //TODO: add stuffs
        throw new NotImplementedException();
    }
}
