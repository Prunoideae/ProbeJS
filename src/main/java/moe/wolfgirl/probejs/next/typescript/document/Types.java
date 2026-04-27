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

public interface Types {
    RawType ANY = raw("any");
    RawType NEVER = raw("never");
    RawType UNKNOWN = raw("unknown");
    RawType VOID = raw("void");
    RawType BOOLEAN = raw("boolean");
    RawType NUMBER = raw("number");
    RawType STRING = raw("string");

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

    static VariableType variable(String name, @Nullable Code type) {
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
}
