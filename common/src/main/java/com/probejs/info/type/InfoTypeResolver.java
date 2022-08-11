package com.probejs.info.type;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InfoTypeResolver {
    public static ITypeInfo resolveType(Type type) {
        return resolveType(type, t -> t);
    }

    public static ITypeInfo resolveType(Type type, Function<Type, Type> typeTransformer) {
        type = typeTransformer.apply(type);
        if (TypeInfoArray.test(type))
            return new TypeInfoArray(type, typeTransformer);
        if (TypeInfoClass.test(type))
            return new TypeInfoClass(type, typeTransformer);
        if (TypeInfoVariable.test(type))
            return new TypeInfoVariable(type, typeTransformer);
        if (TypeInfoWildcard.test(type))
            return new TypeInfoWildcard(type, typeTransformer);
        if (TypeInfoParameterized.test(type))
            return new TypeInfoParameterized(type, typeTransformer);
        return null;
    }

    public static ITypeInfo getContainedTypeOrSelf(ITypeInfo typeInfo) {
        if (typeInfo instanceof TypeInfoParameterized paramType) {
            ITypeInfo baseType = paramType.getBaseType();
            if (baseType.assignableFrom(resolveType(Collection.class)) && paramType.getParamTypes().size() > 0) {
                return paramType.getParamTypes().get(0);
            }
            if (baseType.assignableFrom(resolveType(Map.class)) && paramType.getParamTypes().size() > 1) {
                return paramType.getParamTypes().get(1);
            }
        }
        return typeInfo;
    }
}
