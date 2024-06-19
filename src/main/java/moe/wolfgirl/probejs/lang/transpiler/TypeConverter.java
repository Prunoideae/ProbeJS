package moe.wolfgirl.probejs.lang.transpiler;

import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.rhino.type.*;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.java.type.TypeDescriptor;
import moe.wolfgirl.probejs.lang.java.type.impl.*;
import moe.wolfgirl.probejs.lang.typescript.code.type.*;
import moe.wolfgirl.probejs.lang.typescript.code.type.js.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapts a TypeDescriptor into a BaseType
 */
public class TypeConverter {

    public final Map<ClassPath, BaseType> predefinedTypes = new HashMap<>();
    public final ScriptManager scriptManager;

    public TypeConverter(ScriptManager manager) {
        this.scriptManager = manager;
    }

    public void addType(Class<?> clazz, BaseType type) {
        predefinedTypes.put(new ClassPath(clazz), type);
    }

    public BaseType convertType(TypeDescriptor descriptor) {
        if (descriptor instanceof ClassType classType) {
            return predefinedTypes.getOrDefault(
                    classType.classPath,
                    new TSClassType(classType.classPath)
            );
        } else if (descriptor instanceof ArrayType arrayType) {
            return new TSArrayType(convertType(arrayType.component));
        } else if (descriptor instanceof ParamType paramType) {
            BaseType base = convertType(paramType.base);
            if (base == Types.ANY) return Types.ANY;
            List<BaseType> params = paramType.params.stream().map(this::convertType).toList();
            return new TSParamType(base, params);
        } else if (descriptor instanceof VariableType variableType) {
            List<TypeDescriptor> desc = variableType.descriptors;
            switch (desc.size()) {
                case 0 -> {
                    return new TSVariableType(variableType.symbol, null);
                }
                case 1 -> {
                    return new TSVariableType(variableType.symbol, convertType(desc.getFirst()));
                }
                default -> {
                    List<BaseType> converted = desc.stream().map(this::convertType).toList();
                    return new TSVariableType(variableType.symbol, new JSJoinedType.Intersection(converted));
                }
            }
        } else if (descriptor instanceof WildcardType wildcardType) {
            return wildcardType.stream().findAny().map(this::convertType).orElse(Types.ANY);
        }
        throw new RuntimeException("Unknown subclass of TypeDescriptor.");
    }

    public BaseType convertType(TypeInfo typeInfo) {
        if (typeInfo == TypeInfo.NONE) return Types.NEVER;

        return switch (typeInfo) {
            case JSObjectTypeInfo info -> {
                var builder = Types.object();
                for (JSOptionalParam field : info.fields()) {
                    builder.member(field.name(), field.optional(), convertType(field.type()));
                }
                yield builder.build();
            }
            case ClassTypeInfo info -> predefinedTypes.getOrDefault(
                    new ClassPath(info.asClass()), Types.typeMaybeGeneric(info.asClass()));
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
                var params = Arrays.stream(info.params()).map(this::convertType).toArray(BaseType[]::new);
                yield Types.parameterized(convertType(info.rawType()), params);
            }
            case null, default -> Types.ANY;
        };
    }

}
