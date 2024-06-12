package moe.wolfgirl.next.typescript.code.type.js;

import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.typescript.Declaration;
import moe.wolfgirl.next.typescript.code.type.BaseType;

import java.util.Collection;
import java.util.List;

public class JSPrimitiveType extends BaseType {

    public final String content;

    public JSPrimitiveType(String content) {
        this.content = content;
    }


    @Override
    public Collection<ClassPath> getUsedClassPaths() {
        return List.of();
    }

    @Override
    public List<String> format(Declaration declaration, FormatType input) {
        return List.of(content);
    }
}
