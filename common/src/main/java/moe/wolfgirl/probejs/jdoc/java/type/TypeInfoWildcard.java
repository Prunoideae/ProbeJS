package moe.wolfgirl.probejs.jdoc.java.type;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.function.Function;

public class TypeInfoWildcard implements ITypeInfo {


    public static boolean test(Type type) {
        return type instanceof WildcardType;
    }

    private final ITypeInfo type;

    public TypeInfoWildcard(Type type, Function<Type, Type> typeTransformer) {
        if (type instanceof WildcardType wild) {
            Type[] upper = wild.getUpperBounds();
            Type[] lower = wild.getLowerBounds();
            if (upper[0] != Object.class) {
                this.type = InfoTypeResolver.resolveType(upper[0], typeTransformer);
                return;
            }
            if (lower.length != 0) {
                this.type = InfoTypeResolver.resolveType(lower[0], typeTransformer);
                return;
            }
        }
        this.type = new TypeInfoClass(Object.class, typeTransformer);
    }

    private TypeInfoWildcard(ITypeInfo inner) {
        this.type = inner;
    }

    @Override
    public ITypeInfo getBaseType() {
        return type;
    }

    @Override
    public Class<?> getResolvedClass() {
        return type.getResolvedClass();
    }

    @Override
    public String getTypeName() {
        return type.getTypeName();
    }

    @Override
    public ITypeInfo copy() {
        return new TypeInfoWildcard(type.copy());
    }

    @Override
    public boolean assignableFrom(ITypeInfo info) {
        return info.getBaseType().assignableFrom(type);
    }

    @Override
    public boolean equalsTo(ITypeInfo info) {
        return info instanceof TypeInfoWildcard wildcard && wildcard.type.equalsTo(type);
    }
}
