package moe.wolfgirl.probejs.typescript;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.java.PackageTree;
import moe.wolfgirl.probejs.typescript.base.DocumentRegistry;
import moe.wolfgirl.probejs.typescript.document.base.Code;
import moe.wolfgirl.probejs.typescript.document.base.CommentableCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

// Represents an index.d.ts under a package
// import { A } from "@package/xxx";
// export * as subpackage from "@package/xxx/subpackage";
// declare "@package/xxx" {
// export class Foo {
//     bar(a: A): void;
// }
public class IndexFile {
    private final ClassPath classPath;
    private final Set<ClassPath> imports = new HashSet<>();
    private final List<ClassPath> subpackages = new ArrayList<>();
    private final List<ClassPath> classes = new ArrayList<>();
    private final List<Code> moduleDumps = new ArrayList<>();
    private final List<Code> globals = new ArrayList<>();
    private final DocumentRegistry registry;

    public IndexFile(PackageTree.Node packageNode, DocumentRegistry registry) {
        this.classPath = packageNode.getClassPath();
        subpackages.addAll(packageNode.getSubPackages());
        classes.addAll(packageNode.getClasses());
        this.registry = registry;
    }

    public void addCode(Code code) {
        imports.addAll(code.getImports());
        moduleDumps.add(code);
    }

    public void addGlobal(Code code) {
        imports.addAll(code.getImports());
        globals.add(code);
    }

    private Map<ClassPath, String> loadClasses() {
        for (var clazz : classes) {
            if (clazz.getClassName().equals("package-info")) continue; // Skip package-info.java
            Code classDecl = registry.getDocument(clazz);
            if (classDecl != null) addCode(classDecl);
            Code classAlias = registry.getInputAlias(clazz);
            if (classAlias != null) addCode(classAlias);
            Code global = registry.getGlobal(clazz);
            if (global != null) addGlobal(global);
        }

        var resolvedSymbols = getResolvedSymbols();
        for (var code : moduleDumps) {
            code.setResolvedSymbols(resolvedSymbols);
        }
        for (Code global : globals) {
            global.setResolvedSymbols(resolvedSymbols);
        }

        return resolvedSymbols;
    }

    public void dumpTo(Path baseDir) {
        var dirPath = classPath == null ? baseDir : classPath.asDirPath(baseDir);
        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var indexPath = dirPath.resolve("index.d.ts");
        try (var indexWriter = Files.newBufferedWriter(indexPath)) {
            var resolvedSymbols = loadClasses();

            // import { A, B } from "@package/xxx";
            Multimap<ClassPath, String> packageToClasses = ArrayListMultimap.create();
            for (var importPath : imports) {
                if (importPath.getPackage() != null && importPath.getPackage().equals(classPath))
                    continue; // Don't import from the same package
                // indexWriter.write("import { %s } from \"%s\";\n".formatted(importPath.getClassName(), importPath.asTypePath()));
                var symbolName = resolvedSymbols.getOrDefault(importPath, importPath.getClassName());
                if (symbolName.equals(importPath.getClassName())) {
                    packageToClasses.put(importPath.getPackage(), symbolName);
                } else {
                    packageToClasses.put(importPath.getPackage(), "%s as %s".formatted(importPath.getClassName(), resolvedSymbols.get(importPath)));
                }
            }
            for (var entry : packageToClasses.asMap().entrySet()) {
                ClassPath packagePath = entry.getKey();
                String classList = String.join(", ", entry.getValue());
                indexWriter.write("import { %s } from \"%s\";\n".formatted(classList, packagePath.asTypePath()));
            }

            // export * as subpackage from "@package/xxx/subpackage";
            for (var subPackage : subpackages) {
                indexWriter.write("export * as %s from \"%s\";\n".formatted(subPackage.getClassName(), subPackage.asTypePath()));
            }

            // declare module ${packageNode.asTypePath} {
            if (!classes.isEmpty() && classPath != null) {
                indexWriter.write("\n");
                indexWriter.write("declare module \"%s\" {\n".formatted(classPath.asTypePath()));
                for (var code : moduleDumps) {
                    for (String line : CommentableCode.format(code, 4)) {
                        indexWriter.write(line);
                        indexWriter.write("\n");
                    }
                }
                indexWriter.write("}\n");
            }

            // declare global {
            if (!globals.isEmpty()) {
                // Augmentations for the global scope can only be directly nested in external modules or ambient module declarations
                indexWriter.write("\nexport {};\n\n");
                indexWriter.write("declare global {\n");
                for (var code : globals) {
                    for (String line : CommentableCode.format(code, 4)) {
                        indexWriter.write(line);
                        indexWriter.write("\n");
                    }
                }
                indexWriter.write("}\n");
            }
        } catch (IOException e) {
            ProbeJS.LOGGER.error("Failed to write index.d.ts for package %s: %s".formatted(classPath, e.getMessage()));
        }
    }

    private Map<ClassPath, String> getResolvedSymbols() {
        // java.lang.String -> String
        // java.lang.String + foo.bar.String -> String, String$1
        Multimap<String, ClassPath> nameToClassPaths = ArrayListMultimap.create();
        for (var classPath : classes) {
            nameToClassPaths.put(classPath.getClassName(), classPath);
        }
        for (var importPath : imports) {
            nameToClassPaths.put(importPath.getClassName(), importPath);
        }
        for (var subPackage : subpackages) {
            nameToClassPaths.put(subPackage.getClassName(), subPackage); // In case if people go mad
        }

        // We only keep the first one, and rename the rest with $1, $2, ...
        Map<ClassPath, String> resolvedSymbols = new HashMap<>();
        for (var entry : nameToClassPaths.asMap().entrySet()) {
            String name = entry.getKey();
            // deduplicate class paths to avoid unnecessary renaming
            List<ClassPath> classPaths = new ArrayList<>(new HashSet<>(entry.getValue()));

            var localSymbol = classPaths.stream()
                    .filter(cp -> cp.getPackage() != null && cp.getPackage().equals(classPath))
                    .findFirst()
                    .orElse(null);

            int marker = 0;
            if (localSymbol != null) {
                resolvedSymbols.put(localSymbol, name);
                classPaths.remove(localSymbol);
                marker = 1;
            }

            for (int i = 0; i < classPaths.size(); i++) {
                ClassPath classPath = classPaths.get(i);
                if (i + marker == 0) {
                    resolvedSymbols.put(classPath, name);
                } else {
                    resolvedSymbols.put(classPath, name + "$" + (i + marker));
                }
            }
        }

        return resolvedSymbols;
    }

}
