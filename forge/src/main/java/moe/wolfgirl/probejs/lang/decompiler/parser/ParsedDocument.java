package moe.wolfgirl.probejs.lang.decompiler.parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import moe.wolfgirl.probejs.utils.NameUtils;

import java.util.HashMap;
import java.util.Map;

public class ParsedDocument {

    private static final JavaParser PARSER = new JavaParser();
    private static final String PARAM_SRG = "^p_[a-zA-Z0-9]+_$";

    private final CompilationUnit parsed;
    private final Map<String, String> paramRenames = new HashMap<>();

    public ParsedDocument(String content) {
        this.parsed = PARSER.parse(content).getResult().orElseThrow();
    }

    public void getParamTransformations() {
        for (ClassOrInterfaceDeclaration classDecl : parsed.findAll(ClassOrInterfaceDeclaration.class)) {
            for (BodyDeclaration<?> member : classDecl.getMembers()) {
                if (member instanceof CallableDeclaration<?> callable) {
                    int order = 0;
                    for (Parameter parameter : callable.getParameters()) {
                        if (!parameter.getNameAsString().matches(PARAM_SRG)) continue;
                        String[] types = NameUtils.extractAlphabets(parameter.getTypeAsString());
                        paramRenames.put(parameter.getNameAsString(), "%s%s".formatted(NameUtils.asCamelCase(types), order));
                        order++;
                    }
                }
            }
        }
    }

    public String getCode() {
        String content = this.parsed.toString();
        for (Map.Entry<String, String> entry : paramRenames.entrySet()) {
            String original = entry.getKey();
            String renamed = entry.getValue();
            content = content.replace(original, renamed);
        }
        return content;
    }

    public boolean isMixinClass() {
        for (ClassOrInterfaceDeclaration classDecl : parsed.findAll(ClassOrInterfaceDeclaration.class)) {
            for (AnnotationExpr annotation : classDecl.getAnnotations()) {
                if (annotation.getNameAsString().equals("Mixin")) return true;
            }
        }
        return false;
    }
}
