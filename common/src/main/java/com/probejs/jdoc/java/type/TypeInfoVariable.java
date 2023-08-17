package com.probejs.jdoc.java.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TypeInfoVariable implements ITypeInfo {
    public static boolean test(Type type) {
        return type instanceof TypeVariable;
    }

    private final TypeVariable<?> type;
    private final List<ITypeInfo> bounds = new ArrayList<>();
    private boolean underscored = false;

    public TypeInfoVariable(Type type, Function<Type, Type> typeTransformer) {
        this(type, typeTransformer, true);
    }

    public TypeInfoVariable(Type type, Function<Type, Type> typeTransformer, boolean checkBounds) {
        TypeVariable<?> inner = (TypeVariable<?>) type;
        this.type = inner;
        if (checkBounds)
            for (Type bound : inner.getBounds()) {
                if (bound instanceof ParameterizedType parameterized) {
                    bounds.add(new TypeInfoParameterized(
                            InfoTypeResolver.resolveType(parameterized.getRawType()),
                            Arrays.stream(parameterized.getActualTypeArguments())
                                    .map(t -> {
                                        if (t instanceof TypeVariable<?> variable)
                                            return new TypeInfoVariable(variable, typeTransformer, false);
                                        if (t instanceof WildcardType wildcardType) {
                                            Type[] upper = wildcardType.getUpperBounds();
                                            Type[] lower = wildcardType.getLowerBounds();
                                            Type wild;
                                            if (upper[0] != Object.class) {
                                                wild = upper[0];
                                            } else if (lower.length != 0) {
                                                wild = lower[0];
                                            } else {
                                                wild = Object.class;
                                            }
                                            if (wild instanceof TypeVariable<?>)
                                                wild = Object.class;
                                            return InfoTypeResolver.resolveType(wild);
                                        }
                                        return InfoTypeResolver.resolveType(t);
                                    })
                                    .collect(Collectors.toList())
                    ));
                } else {
                    bounds.add(InfoTypeResolver.resolveType(bound, typeTransformer));
                }
            }
        bounds.removeIf(typeInfo -> typeInfo instanceof TypeInfoClass clazz && clazz.getResolvedClass().equals(Object.class));
    }

    private TypeInfoVariable(TypeVariable<?> inner) {
        this.type = inner;
    }

    public void setUnderscored(boolean underscored) {
        this.underscored = underscored;
    }

    @Override
    public ITypeInfo getBaseType() {
        return this;
    }

    @Override
    public String getTypeName() {
        return type.getName();
    }

    @Override
    public ITypeInfo copy() {
        TypeInfoVariable copied = new TypeInfoVariable(type);
        copied.bounds.addAll(bounds);
        copied.setUnderscored(underscored);
        return copied;
    }

    @Override
    public boolean assignableFrom(ITypeInfo info) {
        return info instanceof TypeInfoVariable;
    }

    @Override
    public boolean equalsTo(ITypeInfo info) {
        return info instanceof TypeInfoVariable;
    }

    @Override
    public Class<?> getResolvedClass() {
        if (bounds.size() == 1)
            return bounds.get(0).getResolvedClass();
        return Object.class;
    }

    public List<ITypeInfo> getBounds() {
        return bounds;
    }
}
