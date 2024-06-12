package moe.wolfgirl.probejs.next.transpiler.members;

import moe.wolfgirl.probejs.next.transpiler.TypeConverter;

public abstract class Converter<T, C> {
    protected final TypeConverter converter;

    public Converter(TypeConverter converter) {
        this.converter = converter;
    }

    public abstract C transpile(T input);
}
