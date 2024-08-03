package moe.wolfgirl.probejs.lang.typescript;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.KubeJSPaths;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.server.ServerScriptManager;
import dev.latvian.mods.kubejs.util.UtilsJS;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.ProbePaths;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.java.clazz.Clazz;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.lang.transpiler.Transpiler;
import moe.wolfgirl.probejs.lang.typescript.code.Code;
import moe.wolfgirl.probejs.lang.typescript.code.member.ClassDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.TypeDecl;
import moe.wolfgirl.probejs.lang.typescript.code.ts.Wrapped;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import moe.wolfgirl.probejs.lang.typescript.code.type.js.JSJoinedType;
import moe.wolfgirl.probejs.utils.GameUtils;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Controls a dump. A dump is made of a script type, and is responsible for
 * maintaining the file structures
 */
public class ScriptDump {
    public static final Supplier<ScriptDump> SERVER_DUMP = () -> {
        ServerScriptManager scriptManager = GameUtils.getServerScriptManager();
        if (scriptManager == null) return null;

        return new ScriptDump(
                scriptManager,
                ProbePaths.PROBE.resolve("server"),
                KubeJSPaths.SERVER_SCRIPTS,
                (clazz -> {
                    for (OnlyIn annotation : clazz.getAnnotations(OnlyIn.class)) {
                        if (annotation.value().isClient()) return false;
                    }
                    return true;
                })
        );
    };
    public static final Supplier<ScriptDump> CLIENT_DUMP = () -> new ScriptDump(
            KubeJS.getClientScriptManager(),
            ProbePaths.PROBE.resolve("client"),
            KubeJSPaths.CLIENT_SCRIPTS,
            (clazz -> {
                for (OnlyIn annotation : clazz.getAnnotations(OnlyIn.class)) {
                    if (annotation.value().isDedicatedServer()) return false;
                }
                return true;
            })
    );
    public static final Supplier<ScriptDump> STARTUP_DUMP = () -> new ScriptDump(
            KubeJS.getStartupScriptManager(),
            ProbePaths.PROBE.resolve("startup"),
            KubeJSPaths.STARTUP_SCRIPTS,
            (clazz -> true)
    );

    public final ScriptType scriptType;
    public final ScriptManager manager;
    public final Path basePath;
    public final Path scriptPath;
    public final Map<String, Pair<Collection<String>, Wrapped.Global>> globals;
    public final Transpiler transpiler;
    public final Set<Clazz> recordedClasses = new HashSet<>();
    private final Predicate<Clazz> accept;
    private final Multimap<ClassPath, TypeDecl> convertibles = ArrayListMultimap.create();
    public int dumped = 0;
    public int total = 0;

    public ScriptDump(ScriptManager manager, Path basePath, Path scriptPath, Predicate<Clazz> scriptPredicate) {
        this.scriptType = manager.scriptType;
        this.manager = manager;
        this.basePath = basePath;
        this.scriptPath = scriptPath;
        this.transpiler = new Transpiler(manager);
        this.globals = new HashMap<>();
        this.accept = scriptPredicate;
    }

    public void acceptClasses(Collection<Clazz> classes) {
        for (Clazz clazz : classes) {
            if (accept.test(clazz)) recordedClasses.add(clazz);
        }
    }

    public Set<Class<?>> retrieveClasses() {
        Set<Class<?>> classes = new HashSet<>();
        ProbeJSPlugin.forEachPlugin(plugin -> classes.addAll(plugin.provideJavaClass(this)));
        return classes;
    }

    public void assignType(Class<?> classPath, BaseType type) {
        assignType(new ClassPath(classPath), type);
    }

    public void assignType(ClassPath classPath, BaseType type) {
        convertibles.put(classPath, new TypeDecl(null, type));
    }

    public void assignType(Class<?> classPath, String name, BaseType type) {
        assignType(new ClassPath(classPath), name, type);
    }

    public void assignType(ClassPath classPath, String name, BaseType type) {
        convertibles.put(classPath, new TypeDecl(name, type));
    }

    public void addGlobal(String identifier, Code... content) {
        addGlobal(identifier, List.of(), content);
    }

    public void addGlobal(String identifier, Collection<String> excludedNames, Code... content) {
        Wrapped.Global global = new Wrapped.Global();
        for (Code code : content) {
            global.addCode(code);
        }
        globals.put(identifier, new Pair<>(excludedNames, global));
    }


    public Path ensurePath(String path) {
        return ensurePath(path, false);
    }

    public Path ensurePath(String path, boolean script) {
        Path full = (script ? scriptPath : basePath).resolve(path);
        if (Files.notExists(full)) {
            UtilsJS.tryIO(() -> Files.createDirectories(full));
        }
        return full;
    }

    public Path getTypeFolder() {
        return ensurePath("probe-types");
    }

    public Path getPackageFolder() {
        return ensurePath("probe-types/packages");
    }

    public Path getGlobalFolder() {
        return ensurePath("probe-types/global");
    }

    public Path getSource() {
        return ensurePath("src", true);
    }

    public Path getTest() {
        return ensurePath("test", true);
    }

    public void dumpClasses() throws IOException {
        dumped = 0;
        total = 0;
        transpiler.init();
        ProbeJSPlugin.forEachPlugin(plugin -> {
            try {
                plugin.assignType(this);
            } catch (Throwable t) {
                GameUtils.logException(t);
            }
        });
        Map<String, BufferedWriter> files = new HashMap<>();
        Map<ClassPath, TypeScriptFile> globalClasses = transpiler.dump(recordedClasses);
        ProbeJSPlugin.forEachPlugin(plugin -> plugin.modifyClasses(this, globalClasses));
        total = globalClasses.size();
        for (Map.Entry<ClassPath, TypeScriptFile> entry : globalClasses.entrySet()) {
            try {
                ClassPath classPath = entry.getKey();
                TypeScriptFile output = entry.getValue();
                ClassDecl classDecl = output.findCode(ClassDecl.class).orElse(null);
                if (classDecl == null) continue;

                // Add all assignable types
                // type ExportedType = ConvertibleTypes
                // declare global {
                //     type Type_ = ExportedType
                // }
                String symbol = classPath.getName() + "_";
                String exportedSymbol = Declaration.INPUT_TEMPLATE.formatted(classPath.getName());
                BaseType exportedType = Types.type(classPath);
                BaseType thisType = Types.type(classPath);
                List<String> generics = classDecl.variableTypes.stream().map(v -> v.symbol).toList();

                if (!generics.isEmpty()) {
                    String suffix = "<%s>".formatted(String.join(", ", generics));
                    symbol = symbol + suffix;
                    exportedSymbol = exportedSymbol + suffix;
                    thisType = Types.parameterized(thisType, generics.stream().map(Types::generic).toArray(BaseType[]::new));
                    exportedType = Types.parameterized(exportedType, generics.stream().map(Types::generic).toArray(BaseType[]::new));
                }
                exportedType = Types.ignoreContext(exportedType, BaseType.FormatType.INPUT);
                thisType = Types.ignoreContext(thisType, BaseType.FormatType.RETURN);

                List<BaseType> allTypes = new ArrayList<>();
                List<TypeDecl> delegatedTypes = new ArrayList<>();
                for (TypeDecl typeDecl : convertibles.get(classPath)) {
                    if (typeDecl.symbol == null) allTypes.add(typeDecl.type);
                    else {
                        delegatedTypes.add(typeDecl);
                        allTypes.add(Types.primitive(typeDecl.symbol));
                    }
                }

                if (allTypes.isEmpty()) {
                    allTypes.add(thisType); // Don't add if there are wrapping, for better compatibility with duck typing
                }

                TypeDecl convertibleType = new TypeDecl(
                        exportedSymbol,
                        new JSJoinedType.Union(allTypes)
                );
                TypeDecl globalType = new TypeDecl(
                        symbol,
                        exportedType
                );
                Wrapped.Global typeExport = new Wrapped.Global();
                typeExport.addCode(globalType);
                convertibleType.addComment("""
                        Class-specific type exported by ProbeJS, use global Type_
                        types for convenience unless there's a naming conflict.
                        """);
                typeExport.addComment("""
                        Global type exported for convenience, use class-specific
                        types if there's a naming conflict.
                        """);
                for (TypeDecl delegatedType : delegatedTypes) {
                    output.addCode(delegatedType);
                }
                output.addCode(convertibleType);
                output.addCode(typeExport);

                var fileKey = "%s.%s".formatted(classPath.parts().get(0), classPath.parts().get(1));
                BufferedWriter writer = files.computeIfAbsent(fileKey, key -> {
                    try {
                        return Files.newBufferedWriter(getPackageFolder().resolve(key + ".d.ts"));
                    } catch (IOException e) {
                        ProbeJS.LOGGER.error("Failed to write %s.d.ts".formatted(key));
                        return null;
                    }
                });
                if (writer != null) output.writeAsModule(writer);
                dumped++;
            } catch (Throwable t) {
                GameUtils.logException(t);
            }
        }

        try (var writer = Files.newBufferedWriter(getPackageFolder().resolve("index.d.ts"))) {
            for (Map.Entry<String, BufferedWriter> entry : files.entrySet()) {
                String key = entry.getKey();
                BufferedWriter value = entry.getValue();
                writer.write("/// <reference path=%s />\n".formatted(ProbeJS.GSON.toJson(key + ".d.ts")));
                value.close();
            }
        }
    }

    public void dumpGlobal() throws IOException {
        ProbeJSPlugin.forEachPlugin(plugin -> plugin.addGlobals(this));

        try (var writer = Files.newBufferedWriter(getGlobalFolder().resolve("index.d.ts"))) {
            for (Map.Entry<String, Pair<Collection<String>, Wrapped.Global>> entry : globals.entrySet()) {
                String identifier = entry.getKey();
                Pair<Collection<String>, Wrapped.Global> pair = entry.getValue();
                var global = pair.getSecond();
                var excluded = pair.getFirst();

                TypeScriptFile globalFile = new TypeScriptFile(null);
                for (String s : excluded) {
                    globalFile.excludeSymbol(s);
                }
                globalFile.addCode(global);
                globalFile.write(getGlobalFolder().resolve(identifier + ".d.ts"));
                writer.write("export * from %s\n".formatted(ProbeJS.GSON.toJson("./" + identifier)));
            }
        }

    }

    public void dumpJSConfig() throws IOException {
        moe.wolfgirl.probejs.utils.FileUtils.writeMergedConfig(scriptPath.resolve("jsconfig.json"), """
                {
                    "compilerOptions": {
                        "module": "commonjs",
                        "target": "ES2015",
                        "lib": [
                            "ES5",
                            "ES2015"
                        ],
                        "rootDir": ".",
                        "typeRoots": [
                            "../../.probe/%s/probe-types"
                        ],
                        "baseUrl": "../../.probe/%s/probe-types",
                        "skipLibCheck": true
                    },
                    "include": [
                        "./**/*.js",
                        "./**/*.ts",
                    ]
                }
                """.formatted(basePath.getFileName(), basePath.getFileName())
        );
    }

    public void removeClasses() throws IOException {
        FileUtils.deleteDirectory(getTypeFolder().toFile());
    }

    public void dump() throws IOException, ClassNotFoundException {
        // getSource();
        // if (ProbeConfig.INSTANCE.interactive.get()){
        //     getTest();
        // }

        dumpClasses();
        dumpGlobal();
        dumpJSConfig();
    }

    private static void write(Path writeTo, String content) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(writeTo)) {
            writer.write(content);
        }
    }
}
