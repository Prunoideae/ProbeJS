package moe.wolfgirl.probejs.lang.transpiler;

import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.rhino.util.HideFromJS;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.java.clazz.Clazz;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.lang.transpiler.transformation.ClassTransformer;
import moe.wolfgirl.probejs.lang.typescript.TypeScriptFile;
import moe.wolfgirl.probejs.lang.typescript.code.member.ClassDecl;

import java.util.*;

/**
 * Converts a Clazz into a TypeScriptFile ready for dump.
 */
public class Transpiler {
    public final TypeConverter typeConverter;
    public final Set<ClassPath> rejectedClasses = new HashSet<>();
    private final ScriptManager scriptManager;

    public Transpiler(ScriptManager manager) {
        this.scriptManager = manager;
        this.typeConverter = new TypeConverter(manager);
    }

    public void reject(Class<?> clazz) {
        rejectedClasses.add(new ClassPath(clazz));
    }

    public Map<ClassPath, TypeScriptFile> dump(Collection<Clazz> clazzes) {
        ProbeJSPlugin.forEachPlugin(plugin -> {
            plugin.addPredefinedTypes(typeConverter);
            plugin.denyTypes(this);
        });

        ClassTranspiler transpiler = new ClassTranspiler(typeConverter);
        Map<ClassPath, TypeScriptFile> result = new HashMap<>();

        for (Clazz clazz : clazzes) {
            if (rejectedClasses.contains(clazz.classPath) || clazz.hasAnnotation(HideFromJS.class)) {
                continue;
            }
            ClassDecl classDecl = transpiler.transpile(clazz);
            ClassTransformer.transformClass(clazz, classDecl);

            if (!scriptManager.isClassAllowed(clazz.original.getName())) {
                classDecl.addComment(
                        "This class is not allowed By KubeJS!",
                        "You should not load the class, or KubeJS will throw an error.",
                        "Loading the class using require() will not throw an error, but the class will be undefined."
                );
            }

            TypeScriptFile scriptFile = new TypeScriptFile(clazz.classPath);
            scriptFile.addCode(classDecl);

            result.put(clazz.classPath, scriptFile);
        }

        return result;
    }
}
