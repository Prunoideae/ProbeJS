package com.probejs.info.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TypeInfoParameterized implements ITypeInfo {
    public static boolean test(Type type) {
        return type instanceof ParameterizedType;
    }

    private ITypeInfo rawType;
    private List<ITypeInfo> paramTypes;

    public TypeInfoParameterized(Type type, Function<Type, Type> typeTransformer) {
        if (type instanceof ParameterizedType parType) {
            rawType = InfoTypeResolver.resolveType(parType.getRawType(), typeTransformer);
            paramTypes = Arrays.stream(parType.getActualTypeArguments()).map(param -> InfoTypeResolver.resolveType(param, typeTransformer)).collect(Collectors.toList());
        }
    }

    public TypeInfoParameterized(ITypeInfo rawType, List<ITypeInfo> paramTypes) {
        this.rawType = rawType;
        this.paramTypes = paramTypes;
    }

    @Override
    public ITypeInfo getBaseType() {
        return rawType;
    }

    @Override
    public Class<?> getResolvedClass() {
        return rawType.getResolvedClass();
    }

    public List<ITypeInfo> getParamTypes() {
        return paramTypes;
    }

    @Override
    public String getTypeName() {
        return rawType.getTypeName() + "<%s>".formatted(paramTypes.stream().map(ITypeInfo::getTypeName).collect(Collectors.joining(", ")));
    }

    @Override
    public ITypeInfo copy() {
        return new TypeInfoParameterized(rawType.copy(), paramTypes.stream().map(ITypeInfo::copy).collect(Collectors.toList()));
    }

    @Override
    public boolean assignableFrom(ITypeInfo info) {
        if (info instanceof TypeInfoParameterized parType)
            if (parType.rawType.assignableFrom(rawType) && parType.paramTypes.size() == paramTypes.size()) {
                for (int i = 0; i < paramTypes.size(); i++)
                    if (!parType.paramTypes.get(i).assignableFrom(paramTypes.get(i)))
                        return false;
                return true;
            }
        return false;
    }

    public void setParamTypes(List<ITypeInfo> paramTypes) {
        this.paramTypes = paramTypes;
    }

    public void setRawType(ITypeInfo rawType) {
        this.rawType = rawType;
    }
}
