package com.probejs.info.type;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class InfoTypeResolver {
    public static ITypeInfo resolveType(Type type) {
        if (TypeInfoArray.test(type))
            return new TypeInfoArray(type);
        if (TypeInfoClass.test(type))
            return new TypeInfoClass(type);
        if (TypeInfoVariable.test(type))
            return new TypeInfoVariable(type);
        if (TypeInfoWildcard.test(type))
            return new TypeInfoWildcard(type);
        if (TypeInfoParameterized.test(type))
            return new TypeInfoParameterized(type);
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

    /**
     * Returns a new modified typeInfo basing on the Map<String, ITypeInfo>
     * If the typeInfo is immutable, a new TypeInfo will be returned.
     */
    public static ITypeInfo mutateTypeMap(ITypeInfo typeInfo, Map<String, ITypeInfo> toMutate) {
        if (typeInfo instanceof TypeInfoClass ||
                typeInfo instanceof TypeInfoVariable)
            return toMutate.getOrDefault(typeInfo.getTypeName(), typeInfo).copy();

        typeInfo = typeInfo.copy();

        if (typeInfo instanceof TypeInfoWildcard wild) {
            return mutateTypeMap(wild.getBaseType(), toMutate);
        }

        if (typeInfo instanceof TypeInfoArray array) {
            array.setType(mutateTypeMap(array.getBaseType(), toMutate));
        }

        if (typeInfo instanceof TypeInfoParameterized parType) {
            parType.setRawType(mutateTypeMap(parType.getBaseType(), toMutate));
            parType.setParamTypes(
                    parType.getParamTypes()
                            .stream()
                            .map(info -> mutateTypeMap(info, toMutate))
                            .collect(Collectors.toList()));
        }

        return typeInfo;
    }
}
