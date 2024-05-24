package moe.wolfgirl.next.typescript.parts.impl.types;

import moe.wolfgirl.next.typescript.parts.Code;
import moe.wolfgirl.next.typescript.parts.impl.types.complex.FunctionTS;
import moe.wolfgirl.next.typescript.parts.impl.types.complex.JointTS;

import java.util.List;

public abstract class TypeTS extends Code {
    private static final List<Class<? extends TypeTS>> SHOULD_WRAP = List.of(
            ParamTypeTS.class,
            FunctionTS.class,
            JointTS.class
    );

    public static boolean shouldWrap(TypeTS type) {
        for (Class<? extends TypeTS> clazz : SHOULD_WRAP) {
            if (clazz.isInstance(type)) return true;
        }
        return false;
    }

    public static String getMaybeWrapped(TypeTS type) {
        return shouldWrap(type) ? "(%s)".formatted(type.getType()) : type.getType();
    }

    public abstract String getType();

    @Override
    public final List<String> getContent() {
        return List.of(getType());
    }
}
