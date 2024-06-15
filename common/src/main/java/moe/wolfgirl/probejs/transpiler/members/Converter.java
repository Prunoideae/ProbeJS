package moe.wolfgirl.probejs.transpiler.members;

import moe.wolfgirl.probejs.transpiler.TypeConverter;

public abstract class Converter<T, C> {
    protected final TypeConverter converter;

    public Converter(TypeConverter converter) {
        this.converter = converter;
    }

    public abstract C transpile(T input);
}
