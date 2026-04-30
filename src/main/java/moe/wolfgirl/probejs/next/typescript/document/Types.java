package moe.wolfgirl.probejs.next.typescript.document;

import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.typescript.document.base.Code;
import moe.wolfgirl.probejs.next.typescript.document.base.Type;
import moe.wolfgirl.probejs.next.typescript.document.types.ArrayType;
import moe.wolfgirl.probejs.next.typescript.document.types.ClassType;
import moe.wolfgirl.probejs.next.typescript.document.types.ParamType;
import moe.wolfgirl.probejs.next.typescript.document.types.VariableType;
import moe.wolfgirl.probejs.next.typescript.document.types.special.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Types {
    RawType ANY = raw("any");
    RawType NEVER = raw("never");
    RawType UNKNOWN = raw("unknown");
    RawType VOID = raw("void");
    RawType BOOLEAN = raw("boolean");
    RawType NUMBER = raw("number");
    RawType STRING = raw("string");
    RawType THIS = raw("this");
    RawType REGEXP = raw("RegExp");

    static ClassType clazz(ClassPath classPath) {
        return new ClassType(classPath);
    }

    static ClassType clazz(String classPath) {
        return new ClassType(new ClassPath(classPath));
    }

    static ClassType clazz(Class<?> clazz) {
        return new ClassType(new ClassPath(clazz));
    }

    static ArrayType arrayOf(ClassPath classPath) {
        return new ArrayType(new ClassType(classPath));
    }

    static ParamType parameterized(Type base, Type... typeArgs) {
        return new ParamType(base, List.of(typeArgs));
    }

    static VariableType variable(String name, @Nullable Type type) {
        return new VariableType(name, type);
    }

    static VariableType variable(String name) {
        return variable(name, null);
    }

    static RawType raw(String raw) {
        return new RawType(raw);
    }

    static LiteralType literal(String literal) {
        return new LiteralType(literal);
    }

    static OptionalType optional(Code type) {
        return new OptionalType(type);
    }

    static TypeOfType typeOf(Type type) {
        return new TypeOfType(type);
    }

    static JoinedType.UnionType union(Type... types) {
        return new JoinedType.UnionType(List.of(types));
    }

    static JoinedType.UnionType union(List<Type> types) {
        return new JoinedType.UnionType(types);
    }

    static ObjectType object(Consumer<ObjectType.Builder> builder) {
        ObjectType.Builder b = new ObjectType.Builder();
        builder.accept(b);
        return b.build();
    }

    static FixedArrayType fixedArray(Consumer<ObjectType.Builder> builder) {
        ObjectType.Builder b = new ObjectType.Builder();
        builder.accept(b);
        return b.build().asFixedArray();
    }

    static LambdaType lambda(Consumer<LambdaType.Builder> builder) {
        LambdaType.Builder b = new LambdaType.Builder();
        builder.accept(b);
        return b.build();
    }

    static NamespacedType namespaced(ClassPath namespace, String typeName) {
        return new NamespacedType(namespace, typeName);
    }

    static Type remapType(Predicate<Type> matcher, Function<Type, Type> remapper, Type inputType) {
        if (matcher.test(inputType)) {
            return remapper.apply(inputType);
        } else return switch (inputType) {
            case ArrayType arrayType -> new ArrayType(remapType(matcher, remapper, arrayType.componentType));
            case ParamType paramType ->
                    new ParamType(remapType(matcher, remapper, paramType.baseType), paramType.typeArgs.stream().map(type -> remapType(matcher, remapper, type)).toList());
            case VariableType variableType ->
                    new VariableType(variableType.name, variableType.typeInfo == null ? null : remapType(matcher, remapper, variableType.typeInfo));
            default -> inputType;
        };
    }

    static Type wrapped(String formatter, Type wrapped) {
        return new WrappedType(formatter, wrapped);
    }
}
