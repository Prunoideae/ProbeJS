package moe.wolfgirl.probejs.utils;

import com.mojang.datafixers.util.Pair;
import moe.wolfgirl.probejs.lang.typescript.TypeScriptFile;
import moe.wolfgirl.probejs.lang.typescript.code.ImportInfo;
import moe.wolfgirl.probejs.lang.typescript.code.member.ClassDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.MethodDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.ParamDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.TypeDecl;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import moe.wolfgirl.probejs.lang.typescript.code.type.js.JSObjectType;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class DocUtils {
    public static void applyParam(TypeScriptFile file, Predicate<MethodDecl> test, int index, Consumer<ParamDecl> effect) {
        if (file == null) return;
        file.findCode(ClassDecl.class).ifPresent(classDecl -> {
            for (MethodDecl method : classDecl.methods) {
                if (test.test(method)) {
                    effect.accept(method.params.get(index));
                }
            }
        });
    }

    public static void replaceParamType(TypeScriptFile file, Predicate<MethodDecl> test, int index, BaseType toReplace) {
        applyParam(file, test, index, decl -> decl.type = toReplace);
        for (ImportInfo usedClassPath : toReplace.getUsedImports()) {
            file.declaration.addClass(usedClassPath);
        }
    }

    public static void generateMappedType(String mapName, String flagName, Iterable<Pair<String, BaseType>> kvPairs, TypeScriptFile typeScriptFile) {
        JSObjectType.Builder typeDict = Types.object();

        for (Pair<String, BaseType> kvPair : kvPairs) {
            typeDict.member(kvPair.getFirst(), kvPair.getSecond());
        }

        typeScriptFile.addCode(new TypeDecl(mapName, typeDict.buildIndexed()));
        typeScriptFile.addCode(new TypeDecl(flagName, Types.primitive("keyof %s".formatted(mapName))));
    }
}
