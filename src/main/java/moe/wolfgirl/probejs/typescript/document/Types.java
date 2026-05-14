package moe.wolfgirl.probejs.typescript.document;

import moe.wolfgirl.probejs.plugin.builtins.alias.RecordTypes;
import moe.wolfgirl.probejs.typescript.ClassPath;
import moe.wolfgirl.probejs.plugin.builtins.alias.RegistryTypes;
import moe.wolfgirl.probejs.typescript.Documents;
import moe.wolfgirl.probejs.typescript.document.base.Type;
import moe.wolfgirl.probejs.typescript.document.types.ArrayType;
import moe.wolfgirl.probejs.typescript.document.types.ClassType;
import moe.wolfgirl.probejs.typescript.document.types.ParamType;
import moe.wolfgirl.probejs.typescript.document.types.VariableType;
import moe.wolfgirl.probejs.typescript.document.types.special.*;
import moe.wolfgirl.probejs.utils.TypeUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Types {
    RawType ANY = raw("any");
    RawType NEVER = raw("never");
    RawType OBJECT = raw("object");
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

    static OptionalType optional(Type type) {
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

    static ObjectType.Builder newObject() {
        return new ObjectType.Builder();
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

    static void markAsInput(Type type) {
        switch (type) {
            case ClassType classType -> {
                var classPath = classType.classPath;
                // If we have alias, alias will refer to the original type as input, so we don't need to check
                // for functional interface
                if (Documents.INSTANCE.hasAlias(classPath)) classType.asInput();
                else {
                    try {
                        Class<?> clazz = classPath.loadClass();
                        if (!RecordTypes.SKIP_RECORDS.contains(clazz) && clazz.isRecord()) classType.asInput();
                    } catch (Throwable ignore) {
                    }
                }
            }
            case ArrayType arrayType -> markAsInput(arrayType.componentType);
            case OptionalType optionalType -> markAsInput(optionalType.componentType);
            case ParamType paramType -> {
                markAsInput(paramType.baseType);
                if (TypeUtils.isFunctionalInterface(paramType.baseType)) return;
                // Special handling for RegistryTypes, we don't mark the params as input
                if (paramType.baseType instanceof ClassType classType && RegistryTypes.HOLDER_TYPES.contains(classType.classPath)) {
                    return;
                }
                for (Type typeArg : paramType.typeArgs) {
                    markAsInput(typeArg);
                }
            }

            case null, default -> {
            }
        }
    }


}
