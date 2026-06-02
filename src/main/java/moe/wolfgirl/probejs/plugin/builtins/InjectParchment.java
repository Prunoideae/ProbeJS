package moe.wolfgirl.probejs.plugin.builtins;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import moe.wolfgirl.probejs.GameStates;
import moe.wolfgirl.probejs.ProbeConfig;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.ProbePaths;
import moe.wolfgirl.probejs.java.members.ConstructorInfo;
import moe.wolfgirl.probejs.java.members.FieldInfo;
import moe.wolfgirl.probejs.java.members.MethodInfo;
import moe.wolfgirl.probejs.misc.javadoc.JavadocSanitizer;
import moe.wolfgirl.probejs.misc.javadoc.ParchmentClass;
import moe.wolfgirl.probejs.misc.javadoc.SourceJarDownloader;
import moe.wolfgirl.probejs.misc.javadoc.SourceJarParser;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.typescript.ClassPath;
import moe.wolfgirl.probejs.typescript.Documents;
import moe.wolfgirl.probejs.typescript.document.members.ConstructorDecl;
import moe.wolfgirl.probejs.typescript.document.members.FieldDecl;
import moe.wolfgirl.probejs.typescript.document.members.MethodDecl;
import moe.wolfgirl.probejs.utils.GameUtils;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InjectParchment extends ProbeJSPlugin {
    private final Map<ClassPath, ParchmentClass> parchmentClasses = new HashMap<>();

    @Override
    public void initialize() {
        try {
            SourceJarDownloader downloader = new SourceJarDownloader(ProbePaths.SOURCE_JARS);
            downloader.load(ProbePaths.SOURCE_JARS.resolve("records.json"));
            downloader.downloadSourceJars(ProbeConfig.INSTANCE.modSources.get(), false);
            downloader.save(ProbePaths.SOURCE_JARS.resolve("records.json"));
            GameStates.DUMP_STATE.setStatus(Component.literal("Loading Parchment data..."));
            String parchmentJson = String.join("\n", GameUtils.readData("assets/probejs/dumps/parchment.json", Map.of()));
            var obj = ProbeJS.GSON.fromJson(parchmentJson, JsonObject.class);
            for (ParchmentClass parchmentClass : ParchmentClass.load(obj)) {
                parchmentClasses.put(parchmentClass.getClassPath(), parchmentClass);
            }
            GameStates.DUMP_STATE.setStatus(Component.literal("Loading source jars..."));
            for (ParchmentClass sourceClass : SourceJarParser.fromSourceJars(ProbePaths.SOURCE_JARS)) {
                parchmentClasses.put(sourceClass.getClassPath(), sourceClass);
            }
            GameStates.DUMP_STATE.setStatus(Component.literal("Merging Parchment data..."));
            ParchmentClass.mergeSuperMembers(parchmentClasses);
        } catch (IOException ignore) {
        }
    }

    @Override
    public void transformClass(Documents.ClassDocument document) {
        var classInfo = document.classInfo();
        if (!parchmentClasses.containsKey(classInfo.classPath())) return;
        var classDocument = document.document();

        var parchment = parchmentClasses.get(classInfo.classPath());
        if (!parchment.javaDoc().isEmpty()) classDocument.addComments(JavadocSanitizer.sanitize(parchment.javaDoc()));


        for (Pair<ConstructorInfo, ConstructorDecl> constructorDoc : document.constructorDocs()) {
            var parchmentConstructor = parchment.getConstructor(constructorDoc.getFirst().descriptor());
            if (parchmentConstructor == null) continue;
            var constructorDecl = constructorDoc.getSecond();
            if (!parchmentConstructor.javaDoc().isEmpty())
                constructorDecl.addComments(JavadocSanitizer.sanitize(parchmentConstructor.javaDoc()));
            int minParamIndex = parchmentConstructor.params().stream().map(ParchmentClass.Param::index).min(Integer::compareTo).orElse(0);
            for (ParchmentClass.Param param : parchmentConstructor.params()) {
                try {
                    var paramDecl = constructorDecl.params.get(param.index() - minParamIndex);
                    paramDecl.name = param.name();
                } catch (IndexOutOfBoundsException ignore) {
                    // Maybe the constructor is changed
                }
            }
        }

        for (Pair<MethodInfo, MethodDecl> methodDoc : document.methodDocs()) {
            var parchmentMethod = parchment.getMethod(methodDoc.getFirst().descriptor());
            if (parchmentMethod == null) continue;
            var methodDecl = methodDoc.getSecond();
            if (!parchmentMethod.javaDoc().isEmpty())
                methodDecl.addComments(JavadocSanitizer.sanitize(parchmentMethod.javaDoc()));
            int minParamIndex = parchmentMethod.params().stream().map(ParchmentClass.Param::index).min(Integer::compareTo).orElse(0);
            for (ParchmentClass.Param param : parchmentMethod.params()) {
                try {
                    var paramDecl = methodDecl.params.get(param.index() - minParamIndex);
                    paramDecl.name = param.name();
                } catch (IndexOutOfBoundsException ignore) {
                    // Maybe the method is changed
                }
            }
        }

        for (Pair<FieldInfo, FieldDecl> fieldDoc : document.fieldDocs()) {
            var parchmentField = parchment.getField(fieldDoc.getFirst().name());
            if (parchmentField == null) continue;
            var fieldDecl = fieldDoc.getSecond();
            if (!parchmentField.javaDoc().isEmpty())
                fieldDecl.addComments(JavadocSanitizer.sanitize(parchmentField.javaDoc()));
        }
    }
}
