package moe.wolfgirl.probejs.next.typescript.transpiler.members;

import moe.wolfgirl.probejs.next.typescript.document.base.Code;
import moe.wolfgirl.probejs.next.typescript.transpiler.TypeConverter;

public abstract class Converter<T, C extends Code> {
    protected final TypeConverter converter;

    public Converter(TypeConverter converter) {
        this.converter = converter;
    }

    public abstract C convert(T source);
}
