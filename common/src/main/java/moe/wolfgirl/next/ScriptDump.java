package moe.wolfgirl.next;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.KubeJSPaths;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.server.ServerScriptManager;
import dev.latvian.mods.kubejs.util.UtilsJS;
import moe.wolfgirl.ProbeJS;
import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.java.clazz.Clazz;
import moe.wolfgirl.next.plugin.ProbeJSPlugin;
import moe.wolfgirl.next.transpiler.Transpiler;
import moe.wolfgirl.next.typescript.Declaration;
import moe.wolfgirl.next.typescript.TypeScriptFile;
import moe.wolfgirl.next.typescript.code.Code;
import moe.wolfgirl.next.typescript.code.member.TypeDecl;
import moe.wolfgirl.next.typescript.code.ts.Wrapped;
import moe.wolfgirl.next.typescript.code.type.BaseType;
import moe.wolfgirl.next.typescript.code.type.js.JSJoinedType;
import moe.wolfgirl.next.typescript.code.type.Types;

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
    public static final Supplier<ScriptDump> SERVER_DUMP = () -> new ScriptDump(
            ServerScriptManager.getScriptManager(),
            KubeJSPaths.SERVER_SCRIPTS,
            (clazz -> true)
    );
    public static final Supplier<ScriptDump> CLIENT_DUMP = () -> new ScriptDump(
            KubeJS.getClientScriptManager(),
            KubeJSPaths.CLIENT_SCRIPTS,
            (clazz -> true)
    );
    public static final Supplier<ScriptDump> STARTUP_DUMP = () -> new ScriptDump(
            KubeJS.getStartupScriptManager(),
            KubeJSPaths.STARTUP_SCRIPTS,
            (clazz -> true)
    );

    public final ScriptType scriptType;
    public final ScriptManager manager;
    public final Path basePath;
    public final Map<String, Pair<Collection<String>, Wrapped.Global>> globals;
    public final Transpiler transpiler;
    public final Set<Clazz> recordedClasses = new HashSet<>();
    private final Predicate<Clazz> accept;
    private final Multimap<ClassPath, BaseType> convertibles = ArrayListMultimap.create();

    public ScriptDump(ScriptManager manager, Path basePath, Predicate<Clazz> scriptPredicate) {
        this.scriptType = manager.scriptType;
        this.manager = manager;
        this.basePath = basePath;
        this.transpiler = new Transpiler(manager);
        this.globals = new HashMap<>();
        this.accept = scriptPredicate;

        ProbeJSPlugin.forEachPlugin(plugin -> plugin.assignType(this));
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
        convertibles.put(classPath, type);
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
        Path full = basePath.resolve(path);
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
        return ensurePath("src");
    }

    public void dumpClasses() throws IOException, ClassNotFoundException {
        Path packageFolder = getPackageFolder();

        Map<ClassPath, TypeScriptFile> globalClasses = transpiler.dump(recordedClasses);
        ProbeJSPlugin.forEachPlugin(plugin -> plugin.modifyClasses(this, globalClasses));
        for (Map.Entry<ClassPath, TypeScriptFile> entry : globalClasses.entrySet()) {
            try {
                ClassPath classPath = entry.getKey();
                TypeScriptFile output = entry.getValue();

                // Add all assignable types
                // type ExportedType = ConvertibleTypes
                // declare global {
                //     type Type_ = ExportedType
                // }
                String symbol = classPath.getName() + "_";
                String exportedSymbol = Declaration.INPUT_TEMPLATE.formatted(classPath.getName());
                BaseType exportedType = Types.type(classPath);
                BaseType thisType = Types.type(classPath);
                List<String> generics = classPath.getGenerics();

                if (generics.size() != 0) {
                    String suffix = "<%s>".formatted(String.join(", ", generics));
                    symbol = symbol + suffix;
                    exportedSymbol = exportedSymbol + suffix;
                    thisType = Types.parameterized(thisType, generics.stream().map(Types::generic).toArray(BaseType[]::new));
                    exportedType = Types.parameterized(exportedType, generics.stream().map(Types::generic).toArray(BaseType[]::new));
                }
                exportedType = Types.ignoreContext(exportedType, BaseType.FormatType.INPUT);
                thisType = Types.ignoreContext(thisType, BaseType.FormatType.RETURN);

                List<BaseType> allTypes = new ArrayList<>(convertibles.get(classPath));
                allTypes.add(thisType);
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
                output.addCode(convertibleType);
                output.addCode(typeExport);


                Path dir = classPath.makePath(packageFolder);
                output.write(dir.resolve(
                        "%s.d.ts".formatted(classPath.getName())
                ));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public void dumpGlobal() throws IOException {
        ProbeJSPlugin.forEachPlugin(plugin -> plugin.addGlobals(this));

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
        }
    }

    public void dumpJSConfig() throws IOException {
        writeMergedConfig(basePath.resolve("jsconfig.json"), """
                {
                    "compilerOptions": {
                        "module": "commonjs",
                        "target": "ES2015",
                        "lib": [
                            "ES5",
                            "ES2015"
                        ],
                        "rootDirs": [
                            "./src",
                            "./probe-types"
                        ],
                        "baseUrl": "./probe-types",
                        "skipLibCheck": true
                    }
                }
                """);
    }

    public void dump() throws IOException, ClassNotFoundException {
        Path srcFolder = getSource();

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

        dumpClasses();
        dumpGlobal();
        dumpJSConfig();

        if (Files.notExists(srcFolder.resolve("globals.d.ts"))) {
            write(srcFolder.resolve("globals.d.ts"), """
                    export {} // Do not remove this line.
                                        
                    // Add your own declarations of methods, variables and types here.
                    // Using require will let VSCode think every script file is an isolated module,
                    // so they will not be visible unless you declare them in the global scope.
                                    
                    // You can also create additional declarations as you like.
                    declare global {
                                    
                    }""".strip());
        }
    }

    private static void write(Path writeTo, String content) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(writeTo)) {
            writer.write(content);
        }
    }

    private static void writeMergedConfig(Path path, String config) throws IOException {
        JsonObject updates = ProbeJS.GSON.fromJson(config, JsonObject.class);
        JsonObject read = Files.exists(path) ? ProbeJS.GSON.fromJson(Files.newBufferedReader(path), JsonObject.class) : new JsonObject();
        if (read == null) read = new JsonObject();
        JsonObject original = (JsonObject) mergeJsonRecursively(read, updates);
        JsonWriter jsonWriter = ProbeJS.GSON_WRITER.newJsonWriter(Files.newBufferedWriter(path));
        jsonWriter.setIndent("    ");
        ProbeJS.GSON_WRITER.toJson(original, JsonObject.class, jsonWriter);
        jsonWriter.close();
    }

    private static JsonElement mergeJsonRecursively(JsonElement first, JsonElement second) {
        if (first instanceof JsonObject firstObject && second instanceof JsonObject secondObject) {
            var result = firstObject.deepCopy();
            for (Map.Entry<String, JsonElement> entry : secondObject.entrySet()) {
                String key = entry.getKey();
                JsonElement value = entry.getValue();
                if (result.has(key)) {
                    result.add(key, mergeJsonRecursively(result.get(key), value));
                } else {
                    result.add(key, value);
                }
            }
            return result;
        }

        if (first instanceof JsonArray firstArray && second instanceof JsonArray secondArray) {
            List<JsonElement> elements = new ArrayList<>();
            for (JsonElement element : firstArray) {
                elements.add(element.deepCopy());
            }
            for (JsonElement element : secondArray) {
                int index;
                if ((index = elements.indexOf(element)) != -1) {
                    elements.set(index, mergeJsonRecursively(elements.get(index), element));
                } else {
                    elements.add(element);
                }
            }
            JsonArray result = new JsonArray();
            for (JsonElement element : elements) {
                result.add(element);
            }
            return result;
        }

        return second;
    }
}
