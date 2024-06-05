package moe.wolfgirl.next.typescript;

import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.typescript.code.Code;

import java.util.ArrayList;
import java.util.List;

public class TypeScriptFile {
    public final Declaration declaration;
    public final List<Code> codeList;

    public TypeScriptFile() {
        this.declaration = new Declaration();
        this.codeList = new ArrayList<>();
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
}
