package moe.wolfgirl.next.java.type;

import moe.wolfgirl.next.java.type.impl.*;
import moe.wolfgirl.next.java.type.impl.WildcardType;

import java.lang.reflect.*;

public class TypeAdapter {
    public static TypeDescriptor getTypeDescription(AnnotatedType type) {
        if (type == null) return null;

        if (type instanceof AnnotatedArrayType arrayType) {
            return new ArrayType(arrayType);
        }
        if (type instanceof AnnotatedParameterizedType paramType) {
            return new ParamType(paramType);
        }
        if (type instanceof AnnotatedTypeVariable typeVariable) {
            return new VariableType(typeVariable);
        }
        if (type instanceof AnnotatedWildcardType wildcardType) {
            return new WildcardType(wildcardType);
        }
        return new ClassType(type);
    }
}
