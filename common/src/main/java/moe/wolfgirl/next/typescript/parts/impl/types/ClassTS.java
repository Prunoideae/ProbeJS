package moe.wolfgirl.next.typescript.parts.impl.types;

import moe.wolfgirl.next.java.clazz.ClassPath;

public class ClassTS extends TypeTS {

    private final ClassPath path;
    private final boolean input;

    public ClassTS(ClassPath path, boolean input) {
        this.path = path;
        this.input = input;
    }

    @Override
    public String getType() {
        return input ? input(path) : output(path);
    }
}
