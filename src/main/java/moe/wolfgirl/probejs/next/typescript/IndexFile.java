package moe.wolfgirl.probejs.next.typescript;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.java.PackageTree;
import moe.wolfgirl.probejs.next.typescript.document.base.Code;
import moe.wolfgirl.probejs.next.typescript.transpiler.Documents;
import moe.wolfgirl.probejs.utils.ProbeFileUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Represents an index.d.ts under a package
// import { A } from "@package/xxx";
// export * as subpackage from "@package/xxx/subpackage";
// declare "@package/xxx" {
// export class Foo {
//     bar(a: A): void;
// }
public class IndexFile {
    private final ClassPath classPath;
    private final List<ClassPath> imports = new ArrayList<>();
    private final List<ClassPath> subpackages = new ArrayList<>();
    private final List<ClassPath> classes = new ArrayList<>();
    private final List<Code> moduleDumps = new ArrayList<>();

    public IndexFile(PackageTree.Node packageNode) {
        this.classPath = packageNode.getClassPath();
        subpackages.addAll(packageNode.getSubPackages());
        classes.addAll(packageNode.getClasses());
    }

    public void addCode(Code code) {
        imports.addAll(code.getImports());
        moduleDumps.add(code);
    }

    private void loadClasses() {
        for (var clazz : classes) {
            Code classDecl = Documents.INSTANCE.getDocument(clazz);
            if (classDecl == null) continue;
            addCode(classDecl);
            Code classAlias = Documents.INSTANCE.getInputAlias(clazz);
            if (classAlias == null) continue;
            addCode(classAlias);
        }

        var resolvedSymbols = getResolvedSymbols();
        for (var code : moduleDumps) {
            code.setResolvedSymbols(resolvedSymbols);
        }
    }

    public void dumpTo(Path baseDir) {
        var dirPath = classPath == null ? baseDir : classPath.asDirPath(baseDir);
        ProbeFileUtils.createDirectories(dirPath);

        var indexPath = dirPath.resolve("index.d.ts");
        try (var indexWriter = java.nio.file.Files.newBufferedWriter(indexPath)) {
            loadClasses();

            // import { A, B } from "@package/xxx";
            Multimap<String, String> packageToClasses = ArrayListMultimap.create();
            for (var importPath : imports) {
                // indexWriter.write("import { %s } from \"%s\";\n".formatted(importPath.getClassName(), importPath.asTypePath()));
                packageToClasses.put(importPath.asTypePath(), importPath.getClassName());
            }
            for (var entry : packageToClasses.asMap().entrySet()) {
                String packagePath = entry.getKey();
                String classList = String.join(", ", entry.getValue());
                indexWriter.write("import { %s } from \"%s\";\n".formatted(classList, packagePath));
            }

            // export * as subpackage from "@package/xxx/subpackage";
            for (var subPackage : subpackages) {
                indexWriter.write("export * as %s from \"%s\";\n".formatted(subPackage.getClassName(), subPackage.asTypePath()));
            }

            if (classes.isEmpty() || classPath == null) return;
            indexWriter.write("\n");
            // declare module ${packageNode.asTypePath} {\n");
            indexWriter.write("declare module \"%s\" {\n".formatted(classPath.asTypePath()));
            for (var code : moduleDumps) {
                for (String line : code.format(4)) {
                    indexWriter.write(line);
                    indexWriter.write("\n");
                }
            }
            indexWriter.write("}\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<ClassPath, String> getResolvedSymbols() {
        // java.lang.String -> String
        // java.lang.String + foo.bar.String -> String, String$1
        Multimap<String, ClassPath> nameToClassPaths = ArrayListMultimap.create();
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
            List<ClassPath> classPaths = new ArrayList<>(entry.getValue());
            for (int i = 0; i < classPaths.size(); i++) {
                ClassPath classPath = classPaths.get(i);
                if (i == 0) {
                    resolvedSymbols.put(classPath, name);
                } else {
                    resolvedSymbols.put(classPath, name + "$" + i);
                }
            }
        }

        return resolvedSymbols;
    }

}
