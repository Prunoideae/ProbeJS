package moe.wolfgirl.next.transpiler;

import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.rhino.util.HideFromJS;
import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.java.clazz.Clazz;
import moe.wolfgirl.next.plugin.ProbeJSPlugin;
import moe.wolfgirl.next.typescript.TypeScriptFile;
import moe.wolfgirl.next.typescript.code.member.ClassDecl;

import java.util.*;

/**
 * Converts a Clazz into a TypeScriptFile ready for dump.
 */
public class Transpiler {
    public final TypeConverter typeConverter = new TypeConverter();
    public final Set<ClassPath> rejectedClasses = new HashSet<>();
    private final ScriptManager scriptManager;

    public Transpiler(ScriptManager manager) {
        this.scriptManager = manager;
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
            if (rejectedClasses.contains(clazz.classPath)
                    || !scriptManager.isClassAllowed(clazz.classPath.getClassPath())
                    || clazz.hasAnnotation(HideFromJS.class)) {
                continue;
            }
            ClassDecl classDecl = transpiler.transpile(clazz);

            TypeScriptFile scriptFile = new TypeScriptFile(clazz.classPath);
            scriptFile.addCode(classDecl);

            // TODO: type-conversion stuffs

            // TODO: Globally resolved underscored type

            /*
             * TODO:
             *  ├── client_script
             *  │   ├── src/
             *  │   ├── jsconfig.json
             *  │   └── probe-types/
             *  ├── server_script
             *  │   ├── src/
             *  │   ├── jsconfig.json
             *  │   └── probe-types/
             *  └── startup_script
             *      ├── src/
             *      ├── jsconfig.json
             *      └── probe-types/
             */

            result.put(clazz.classPath, scriptFile);
        }

        return result;
    }
}
