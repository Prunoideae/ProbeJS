package moe.wolfgirl.probejs.misc;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.javadoc.JavadocBlockTag;
import moe.wolfgirl.probejs.utils.GameUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class SourceJarParser {
    private static final JavaParser JAVA_PARSER = new JavaParser();

    public static List<ParchmentClass> fromSourceJars(Path sourceJarDir) {
        List<ParchmentClass> allResult = new ArrayList<>();
        try (var stream = Files.walk(sourceJarDir)) {
            for (Path path : stream.filter(p -> p.toString().endsWith(".jar")).toList()) {
                try (var jarFile = new JarFile(path.toFile())) {
                    for (JarEntry jarEntry : jarFile.stream().filter(p -> p.getName().endsWith(".java") && !p.isDirectory()).toList()) {
                        try (var inputStream = jarFile.getInputStream(jarEntry)) {
                            CompilationUnit cu = JAVA_PARSER.parse(inputStream).getResult().orElseThrow();
                            allResult.addAll(parseCU(cu));
                        }
                    }
                }
            }
        } catch (Exception e) {
            GameUtils.logException(e);
        }
        return allResult;
    }

    private static String getJVMClassName(ClassOrInterfaceDeclaration classDecl) {
        List<String> parentParts = new ArrayList<>();
        TypeDeclaration<?> node = classDecl;
        while (!(node.getParentNode().orElse(null) instanceof CompilationUnit)) {
            parentParts.addFirst(node.getNameAsString());
            node = (TypeDeclaration<?>) node.getParentNode().orElseThrow();
        }
        String topClassName = node.getFullyQualifiedName().orElse(null);
        if (topClassName == null) return null;
        topClassName = topClassName.replace('.', '/');
        return topClassName + (parentParts.isEmpty() ? "" : "$" + String.join("$", parentParts));
    }

    public static List<ParchmentClass> parseCU(CompilationUnit compilationUnit) {
        List<ParchmentClass> result = new ArrayList<>();
        for (ClassOrInterfaceDeclaration classDecl : compilationUnit.findAll(ClassOrInterfaceDeclaration.class)) {
            if (!classDecl.isPublic()) continue;

            var className = getJVMClassName(classDecl);
            if (className == null) continue;

            List<String> javaDoc = classDecl.getJavadoc()
                    .map(javadoc -> javadoc.getDescription().toText().lines().toList())
                    .orElse(List.of());
            List<ParchmentClass.Method> methods = classDecl.getMethods().stream()
                    .filter(m -> m.isPublic() || classDecl.isInterface()) // interface methods are implicitly public
                    .map(m -> parseMethod(m, compilationUnit))
                    .toList();
            List<ParchmentClass.Field> fields = classDecl.getFields().stream()
                    .filter(FieldDeclaration::isPublic)
                    .flatMap(f -> parseFields(f, compilationUnit).stream())
                    .toList();
            // methods and fields must be mutable since we will perform merging later
            result.add(new ParchmentClass(className, javaDoc, new ArrayList<>(methods), new ArrayList<>(fields)));
        }
        return result;
    }

    // --- Type to JVM descriptor conversion ---

    /**
     * Converts a JavaParser {@link Type} to a JVM descriptor string.
     * <ul>
     *   <li>Primitives: {@code int -> "I"}, {@code boolean -> "Z"}, {@code void -> "V"}, etc.</li>
     *   <li>Arrays: {@code int[] -> "[I"}, {@code String[][] -> "[[Ljava/lang/String;"}</li>
     *   <li>Objects: {@code String -> "Ljava/lang/String;"}</li>
     *   <li>Type variables / wildcards fall back to {@code "Ljava/lang/Object;"}</li>
     * </ul>
     */
    private static String toJvmDescriptor(Type type, CompilationUnit cu) {
        if (type instanceof PrimitiveType pt) {
            return switch (pt.getType()) {
                case BOOLEAN -> "Z";
                case BYTE -> "B";
                case SHORT -> "S";
                case INT -> "I";
                case LONG -> "J";
                case FLOAT -> "F";
                case DOUBLE -> "D";
                case CHAR -> "C";
            };
        }
        if (type instanceof VoidType) {
            return "V";
        }
        if (type instanceof ArrayType at) {
            return "[" + toJvmDescriptor(at.getComponentType(), cu);
        }
        if (type instanceof ClassOrInterfaceType cit) {
            String internalName = resolveClassOrInterfaceType(cit, cu);
            return "L" + internalName + ";";
        }
        // TypeParameterType, WildcardType, VarType, UnknownType, etc.
        return "Ljava/lang/Object;";
    }

    /**
     * Resolves a {@link ClassOrInterfaceType} to a JVM internal name
     * (e.g. {@code java/util/Map$Entry}) using the compilation unit's imports
     * and the Java naming convention (lowercase = package, uppercase = class).
     */
    private static String resolveClassOrInterfaceType(ClassOrInterfaceType type, CompilationUnit cu) {
        if (type.getScope().isPresent()) {
            ClassOrInterfaceType scope = type.getScope().get();
            String scopeResolved = resolveClassOrInterfaceType(scope, cu);
            char sep = Character.isUpperCase(scope.getNameAsString().charAt(0)) ? '$' : '/';
            return scopeResolved + sep + type.getNameAsString();
        }
        // Simple name – resolve via imports
        String simpleName = type.getNameAsString();
        String resolved = resolveViaImports(cu, simpleName);
        return resolved != null ? resolved : simpleName;
    }

    /**
     * Attempts to resolve a simple type name to a fully qualified JVM internal
     * name using the compilation unit's import declarations and {@code java.lang}.
     */
    private static String resolveViaImports(CompilationUnit cu, String simpleName) {
        // 1. Explicit single-type imports
        for (var importDecl : cu.getImports()) {
            if (importDecl.isAsterisk()) continue;
            String imported = importDecl.getNameAsString();
            if (imported.endsWith("." + simpleName)) {
                return imported.replace('.', '/');
            }
        }
        // 2. java.lang implicit import
        try {
            Class.forName("java.lang." + simpleName);
            return "java/lang/" + simpleName;
        } catch (ClassNotFoundException ignored) {
        }
        // 3. Fallback: same package as the compilation unit
        return cu.getPackageDeclaration()
                .map(pkg -> pkg.getNameAsString().replace('.', '/') + "/" + simpleName)
                .orElse(simpleName);
    }

    // --- Public parsing methods ---

    public static ParchmentClass.Method parseMethod(MethodDeclaration method, CompilationUnit cu) {
        String name = method.getNameAsString();

        // Javadoc (description only, before @param / @return tags)
        List<String> javaDoc = method.getJavadoc()
                .map(javadoc -> javadoc.getDescription().toText().lines().toList())
                .orElse(List.of());

        // JVM method descriptor:  (paramTypes)returnType
        String returnDescriptor = toJvmDescriptor(method.getType(), cu);
        String paramsDescriptor = method.getParameters().stream()
                .map(p -> toJvmDescriptor(p.getType(), cu))
                .collect(Collectors.joining());
        String descriptor = "(" + paramsDescriptor + ")" + returnDescriptor;

        // Extract per-parameter javadoc from @param block tags
        Map<String, List<String>> paramDocs = extractParamDocs(method);

        // Build parameter list with 1-based indexing (parchment convention)
        List<ParchmentClass.Param> params = new ArrayList<>();
        int index = 1;
        for (var param : method.getParameters()) {
            String paramName = param.getNameAsString();
            List<String> paramJavaDoc = paramDocs.getOrDefault(paramName, List.of());
            params.add(new ParchmentClass.Param(paramName, index, paramJavaDoc));
            index++;
        }

        return new ParchmentClass.Method(name, javaDoc, descriptor, params);
    }

    /**
     * Parses a field declaration, producing one {@link ParchmentClass.Field}
     * for each variable declarator (e.g. {@code int a, b;} yields two records).
     */
    public static List<ParchmentClass.Field> parseFields(FieldDeclaration field, CompilationUnit cu) {
        List<String> javaDoc = field.getJavadoc()
                .map(javadoc -> javadoc.getDescription().toText().lines().toList())
                .orElse(List.of());

        List<ParchmentClass.Field> result = new ArrayList<>();
        for (VariableDeclarator variable : field.getVariables()) {
            String name = variable.getNameAsString();
            String descriptor = toJvmDescriptor(variable.getType(), cu);
            result.add(new ParchmentClass.Field(name, javaDoc, descriptor));
        }
        return result;
    }

    // --- Javadoc helpers ---

    /**
     * Reads {@code @param} block tags from a method's javadoc and returns a map
     * from parameter name to its documented description lines.
     */
    private static Map<String, List<String>> extractParamDocs(MethodDeclaration method) {
        Map<String, List<String>> result = new HashMap<>();
        method.getJavadoc().ifPresent(javadoc -> {
            for (var blockTag : javadoc.getBlockTags()) {
                if (blockTag.getType() == JavadocBlockTag.Type.PARAM) {
                    String paramName = blockTag.getName().orElse("");
                    List<String> lines = blockTag.getContent().toText().lines()
                            .filter(s -> !s.isBlank())
                            .toList();
                    if (!lines.isEmpty()) {
                        result.put(paramName, lines);
                    }
                }
            }
        });
        return result;
    }
}
