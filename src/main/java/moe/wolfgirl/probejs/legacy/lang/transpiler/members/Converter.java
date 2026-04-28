package moe.wolfgirl.probejs.legacy.lang.transpiler.members;

import moe.wolfgirl.probejs.legacy.lang.transpiler.TypeConverter;

public abstract class Converter<T, C> {
    protected final TypeConverter converter;

    public Converter(TypeConverter converter) {
        this.converter = converter;
    }

    public abstract C transpile(T input);
}
