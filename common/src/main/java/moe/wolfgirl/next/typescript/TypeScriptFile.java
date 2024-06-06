package moe.wolfgirl.next.typescript;

import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.typescript.code.Code;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TypeScriptFile {
    public final Declaration declaration;
    public final List<Code> codeList;
    public final ClassPath classPath;

    public TypeScriptFile(ClassPath self) {
        this.declaration = new Declaration();
        this.codeList = new ArrayList<>();
        this.classPath = self;
        declaration.addClass(self);
    }

    public void addCode(Code code) {
        codeList.add(code);
        for (ClassPath usedClassPath : code.getUsedClassPaths()) {
            declaration.addClass(usedClassPath);
        }
    }

    public String format() {
        List<String> formatted = new ArrayList<>();

        for (Code code : codeList) {
            formatted.addAll(code.format(declaration));
        }

        return String.join("\n", formatted);
    }

    public void write(Path writeTo) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(writeTo)) {
            for (Reference value : declaration.references.values()) {
                if (value.classPath().equals(classPath)) continue;
                writer.write(value.getImport() + "\n");
            }

            writer.write("\n");
            writer.write(format());
        }
    }
}
