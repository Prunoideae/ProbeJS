package moe.wolfgirl.probejs.docs;

import dev.latvian.mods.kubejs.plugin.builtin.wrapper.TextWrapper;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.typescript.TypeScriptFile;
import moe.wolfgirl.probejs.lang.typescript.code.member.ClassDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.MethodDecl;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import moe.wolfgirl.probejs.next.plugin.ProbeJSPlugin;

import java.util.Map;

public class TranslationDoc extends ProbeJSPlugin {
    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        TypeScriptFile typeScriptFile = globalClasses.get(new ClassPath(TextWrapper.class));
        ClassDecl classDecl = typeScriptFile.findCode(ClassDecl.class).orElse(null);
        if (classDecl == null) return;

        for (MethodDecl method : classDecl.methods) {
            if (method.name.equals("translate") || method.name.equals("translatable")) {
                method.params.getFirst().type = Types.primitive("Special.LangKey");
            }
        }
    }
}
