package moe.wolfgirl.probejs.jdoc.java.type;

public interface ITypeInfo {

    ITypeInfo getBaseType();

    Class<?> getResolvedClass();

    String getTypeName();

    ITypeInfo copy();

    boolean assignableFrom(ITypeInfo info);

    boolean equalsTo(ITypeInfo info);

    default String getExplicitName(){
        return getTypeName();
    }
}
