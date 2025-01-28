package moe.wolfgirl.probejs.lang.transpiler;

import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.kubejs.util.ClassWrapper;
import dev.latvian.mods.rhino.type.*;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.typescript.code.type.*;

import java.util.*;

/**
 * Adapts a TypeInfo into a BaseType
 */
public class TypeConverter {
    private static final ClassPath WRAPPER = new ClassPath(ClassWrapper.class);

    public final Map<ClassPath, BaseType> predefinedTypes = new HashMap<>();
    public final ScriptManager scriptManager;

    public TypeConverter(ScriptManager manager) {
        this.scriptManager = manager;
    }

    public void addType(Class<?> clazz, BaseType type) {
        predefinedTypes.put(new ClassPath(clazz), type);
    }

    public BaseType convertType(TypeInfo typeInfo) {
        return convertType(typeInfo, true);
    }

    public BaseType convertType(TypeInfo typeInfo, boolean baseType) {
        if (typeInfo == TypeInfo.NONE) return Types.NEVER;

        return switch (typeInfo) {
            case JSObjectTypeInfo info -> {
                var builder = Types.object();
                for (JSOptionalParam field : info.fields()) {
                    builder.member(field.name(), field.optional(), convertType(field.type()));
                }
                yield builder.build();
            }
            case ClassTypeInfo info -> {
                Class<?> clazz = info.asClass();
                if (predefinedTypes.containsKey(new ClassPath(clazz))) yield predefinedTypes.get(new ClassPath(clazz));
                if (clazz.getTypeParameters().length != 0 && !baseType) {
                    yield Types.parameterized(Types.type(clazz),
                            Collections.nCopies(clazz.getTypeParameters().length, Types.OBJECT)
                                    .toArray(BaseType[]::new)
                    );
                } else {
                    yield Types.type(clazz);
                }
            }
            case ArrayTypeInfo info -> convertType(info.componentType()).asArray();
            case JSFixedArrayTypeInfo info -> {
                var builder = Types.arrayOf();
                for (JSOptionalParam type : info.types()) {
                    builder.member(type.name(), type.optional(), convertType(type.type()));
                }
                yield builder.build();
            }
            case JSFunctionTypeInfo info -> {
                var builder = Types.lambda()
                        .returnType(convertType(info.returnType()));
                for (JSOptionalParam param : info.params()) {
                    builder.param(param.name(), convertType(param.type()), param.optional());
                }
                yield builder.build();
            }
            case JSOrTypeInfo info -> Types.or(info.types().stream().map(this::convertType).toArray(BaseType[]::new));
            case ParameterizedTypeInfo info -> {
                if (info.rawType() == TypeInfo.RAW_OPTIONAL) {
                    yield Types.optional(convertType(info.param(0)));
                }
                BaseType base = convertType(info.rawType());
                var params = Arrays.stream(info.params())
                        .map(p -> this.convertType(p, false))
                        .toArray(BaseType[]::new);
                if (base instanceof TSClassType classType && classType.classPath.equals(WRAPPER)) {
                    yield Types.typeOf(params[0]);
                }
                yield Types.parameterized(base, params);
            }
            case VariableTypeInfo info -> {
                TypeInfo extendsOn = TypeInfo.of(info.asClass());
                if (baseType && extendsOn != TypeInfo.OBJECT) {
                    yield Types.generic(info.getName(), convertType(extendsOn, false));
                }
                yield Types.generic(info.getName());
            }
            case null, default -> Types.ANY;
        };
    }

}
