package moe.wolfgirl.next.java.type;

import moe.wolfgirl.next.java.type.impl.*;
import moe.wolfgirl.next.java.type.impl.WildcardType;

import java.lang.reflect.*;

public class TypeAdapter {
    public static TypeDescriptor getTypeDescription(AnnotatedType type) {
        return getTypeDescription(type, true);
    }

    public static TypeDescriptor getTypeDescription(AnnotatedType type, boolean recursive) {
        if (type == null) return null;

        if (type instanceof AnnotatedArrayType arrayType) {
            return new ArrayType(arrayType);
        }
        if (type instanceof AnnotatedParameterizedType paramType) {
            return new ParamType(paramType);
        }
        if (type instanceof AnnotatedTypeVariable typeVariable) {
            return new VariableType(typeVariable, recursive);
        }
        if (type instanceof AnnotatedWildcardType wildcardType) {
            return new WildcardType(wildcardType, recursive);
        }
        return new ClassType(type);
    }

    public static TypeDescriptor getTypeDescription(Type type) {
        return getTypeDescription(type, true);
    }

    public static TypeDescriptor getTypeDescription(Type type, boolean recursive) {
        if (type == null) return null;

        if (type instanceof GenericArrayType arrayType) {
            return new ArrayType(arrayType);
        }
        if (type instanceof ParameterizedType parameterizedType) {
            return new ParamType(parameterizedType);
        }
        if (type instanceof TypeVariable<?> typeVariable) {
            return new VariableType(typeVariable, recursive);
        }
        if (type instanceof java.lang.reflect.WildcardType wildcardType) {
            return new WildcardType(wildcardType, recursive);
        }

        return new ClassType(type);
    }
}
