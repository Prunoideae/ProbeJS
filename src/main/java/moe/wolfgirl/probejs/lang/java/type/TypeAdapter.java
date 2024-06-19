package moe.wolfgirl.probejs.lang.java.type;

import moe.wolfgirl.probejs.lang.java.type.impl.ArrayType;
import moe.wolfgirl.probejs.lang.java.type.impl.ClassType;
import moe.wolfgirl.probejs.lang.java.type.impl.ParamType;
import moe.wolfgirl.probejs.lang.java.type.impl.VariableType;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Collections;

public class TypeAdapter {
    public static TypeDescriptor getTypeDescription(AnnotatedType type) {
        return getTypeDescription(type, true);
    }

    public static TypeDescriptor getTypeDescription(AnnotatedType type, boolean recursive) {
        switch (type) {
            case null -> {
                return null;
            }
            case AnnotatedArrayType arrayType -> {
                return new ArrayType(arrayType);
            }
            case AnnotatedParameterizedType paramType -> {
                return new ParamType(paramType);
            }
            case AnnotatedTypeVariable typeVariable -> {
                return new VariableType(typeVariable, recursive);
            }
            case AnnotatedWildcardType wildcardType -> {
                return new moe.wolfgirl.probejs.lang.java.type.impl.WildcardType(wildcardType, recursive);
            }
            default -> {
            }
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
        switch (type) {
            case null -> {
                return null;
            }
            case GenericArrayType arrayType -> {
                return new ArrayType(arrayType);
            }
            case ParameterizedType parameterizedType -> {
                return new ParamType(parameterizedType);
            }
            case TypeVariable<?> typeVariable -> {
                return new VariableType(typeVariable, recursive);
            }
            case WildcardType wildcardType -> {
                return new moe.wolfgirl.probejs.lang.java.type.impl.WildcardType(wildcardType, recursive);
            }
            case Class<?> clazz -> {
                TypeVariable<?>[] interfaces = clazz.getTypeParameters();
                if (recursive && interfaces.length != 0)
                    return new ParamType(
                            new Annotation[]{},
                            new ClassType(clazz),
                            Collections.nCopies(interfaces.length, new ClassType(Object.class))
                    );
                return new ClassType(clazz);
            }
            default -> throw new RuntimeException("Unknown type to be resolved");
        }

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
