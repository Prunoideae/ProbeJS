package moe.wolfgirl.next.typescript.code.type;

import moe.wolfgirl.ProbeJS;
import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.typescript.Declaration;
import moe.wolfgirl.next.typescript.code.type.js.JSArrayType;
import moe.wolfgirl.next.typescript.code.type.js.JSJoinedType;
import moe.wolfgirl.next.typescript.code.type.js.JSPrimitiveType;
import moe.wolfgirl.next.typescript.code.type.js.JSTypeOfType;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.BiFunction;

public interface Types {
    JSPrimitiveType ANY = new JSPrimitiveType("any");
    JSPrimitiveType BOOLEAN = new JSPrimitiveType("boolean");
    JSPrimitiveType NUMBER = new JSPrimitiveType("number");
    JSPrimitiveType STRING = new JSPrimitiveType("string");
    JSPrimitiveType NEVER = new JSPrimitiveType("never");
    JSPrimitiveType VOID = new JSPrimitiveType("void");

    /**
     * Returns a literal type of the input if it's something OK in TS,
     * otherwise, any will be returned.
     *
     * @param content a string, number or boolean
     */
    static JSPrimitiveType literal(Object content) {
        if (!(content instanceof String || content instanceof Number || content instanceof Boolean))
            return ANY;
        return new JSPrimitiveType(ProbeJS.GSON.toJson(content));
    }

    /**
     * Returns a type that will be as-is in the TypeScript to represent
     * keywords/types not covered, e.g. InstanceType.
     */
    static JSPrimitiveType primitive(String type) {
        return new JSPrimitiveType(type);
    }

    static JSArrayType arrayOf(BaseType... types) {
        return new JSArrayType(Arrays.stream(types).toList());
    }

    static JSJoinedType.Intersection and(BaseType... types) {
        return new JSJoinedType.Intersection(Arrays.stream(types).toList());
    }

    static JSJoinedType.Union or(BaseType... types) {
        return new JSJoinedType.Union(Arrays.stream(types).toList());
    }

    static TSParamType parameterized(BaseType base, BaseType... params) {
        return new TSParamType(base, Arrays.stream(params).toList());
    }

    static TSVariableType generic(String symbol) {
        return generic(symbol, Types.ANY);
    }

    static TSVariableType generic(String symbol, BaseType extendOn) {
        return new TSVariableType(symbol, extendOn);
    }

    static BaseType typeMaybeGeneric(Class<?> clazz) {
        if (clazz.getTypeParameters().length == 0) return type(clazz);

        var params = Collections.nCopies(clazz.getTypeParameters().length, ANY).toArray(BaseType[]::new);
        return parameterized(type(clazz), params);
    }

    /**
     * You should ensure that this Class does not have type parameters.
     * <br>
     * Otherwise, use typeMaybeGeneric
     */
    static TSClassType type(Class<?> clazz) {
        return type(new ClassPath(clazz));
    }

    static TSClassType type(ClassPath classPath) {
        return new TSClassType(classPath);
    }

    static JSTypeOfType typeOf(Class<?> clazz) {
        return typeOf(new ClassPath(clazz));
    }

    static JSTypeOfType typeOf(ClassPath classPath) {
        return typeOf(new TSClassType(classPath));
    }

    static JSTypeOfType typeOf(BaseType classType) {
        return new JSTypeOfType(classType);
    }

    static BaseType ignoreContext(BaseType type, BaseType.FormatType formatType) {
        return new ContextShield(type, formatType);
    }

    static BaseType custom(BiFunction<Declaration, BaseType.FormatType, String> formatter, ClassPath... imports) {
        return new CustomType(formatter, imports);
    }
}
