package moe.wolfgirl.probejs.utils;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.typescript.TypeScriptFile;
import moe.wolfgirl.probejs.lang.typescript.code.member.ClassDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.MethodDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.ParamDecl;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        for (ClassPath usedClassPath : toReplace.getUsedClassPaths()) {
            file.declaration.addClass(usedClassPath);
        }
    }

    public static void writeMergedConfig(Path path, String config) throws IOException {
        JsonObject updates = ProbeJS.GSON.fromJson(config, JsonObject.class);
        JsonObject read = Files.exists(path) ? ProbeJS.GSON.fromJson(Files.newBufferedReader(path), JsonObject.class) : new JsonObject();
        if (read == null) read = new JsonObject();
        JsonObject original = (JsonObject) JsonUtils.mergeJsonRecursively(read, updates);
        JsonWriter jsonWriter = ProbeJS.GSON_WRITER.newJsonWriter(Files.newBufferedWriter(path));
        jsonWriter.setIndent("    ");
        ProbeJS.GSON_WRITER.toJson(original, JsonObject.class, jsonWriter);
        jsonWriter.close();
    }
}
