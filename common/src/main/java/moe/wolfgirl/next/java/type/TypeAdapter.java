package moe.wolfgirl.next.java.type;

import moe.wolfgirl.next.java.type.impl.*;
import moe.wolfgirl.next.java.type.impl.WildcardType;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
        if (type.getType() instanceof Class<?> clazz) {
            TypeVariable<?>[] interfaces = clazz.getTypeParameters();
            if (recursive && interfaces.length != 0)
                return new ParamType(
                        type.getAnnotations(),
                        new ClassType(clazz),
                        Collections.nCopies(interfaces.length, new ClassType(Object.class))
                );
            return new ClassType(type);
        }
        throw new RuntimeException("Unknown type to be resolved");
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
        if (type instanceof Class<?> clazz) {
            TypeVariable<?>[] interfaces = clazz.getTypeParameters();
            if (recursive && interfaces.length != 0)
                return new ParamType(
                        new Annotation[]{},
                        new ClassType(clazz),
                        Collections.nCopies(interfaces.length, new ClassType(Object.class))
                );
            return new ClassType(clazz);
        }
        throw new RuntimeException("Unknown type to be resolved");
    }

    public static TypeDescriptor consolidateType(TypeDescriptor in, String symbol, TypeDescriptor replacement) {
        if (in instanceof VariableType variableType) {
            if (variableType.symbol.equals(symbol)) return replacement;
        }
        if (in instanceof ArrayType arrayType) {
            return new ArrayType(consolidateType(arrayType.component, symbol, replacement));
        }
        if (in instanceof ParamType paramType) {
            return new ParamType(
                    new Annotation[]{},
                    consolidateType(paramType.base, symbol, replacement),
                    paramType.params.stream().map(t -> consolidateType(t, symbol, replacement)).toList()
            );
        }
        return in;
    }
}
