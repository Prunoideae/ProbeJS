package moe.wolfgirl.next.transpiler;

import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.script.ScriptManager;
import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.java.type.TypeDescriptor;
import moe.wolfgirl.next.java.type.impl.*;
import moe.wolfgirl.next.typescript.code.member.ParamDecl;
import moe.wolfgirl.next.typescript.code.type.*;
import moe.wolfgirl.next.typescript.code.type.js.*;

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
            if (!scriptManager.isClassAllowed(classType.classPath.getClassPath())) return Types.ANY;

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
                    return new TSVariableType(variableType.symbol, convertType(desc.get(0)));
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

    public BaseType convertType(BaseType baseType) {
        if (baseType instanceof JSArrayType jsArrayType) {
            return new JSArrayType(jsArrayType.components.stream().map(this::convertType).toList());
        } else if (baseType instanceof JSJoinedType.Union union) {
            return new JSJoinedType.Union(union.types.stream().map(this::convertType).toList());
        } else if (baseType instanceof JSJoinedType.Intersection intersection) {
            return new JSJoinedType.Intersection(intersection.types.stream().map(this::convertType).toList());
        } else if (baseType instanceof JSObjectType jsObjectType) {
            Map<String, BaseType> members = new HashMap<>();
            for (Map.Entry<String, BaseType> entry : jsObjectType.members.entrySet()) {
                String key = entry.getKey();
                BaseType member = entry.getValue();
                members.put(key, convertType(member));
            }
            return new JSObjectType(members);
        } else if (baseType instanceof TSArrayType arrayType) {
            return new TSArrayType(convertType(arrayType.component));
        } else if (baseType instanceof TSClassType classType) {
            return predefinedTypes.getOrDefault(classType.classPath, classType);
        } else if (baseType instanceof TSParamType paramType) {
            return new TSParamType(convertType(paramType.baseType), paramType.params.stream().map(this::convertType).toList());
        } else if (baseType instanceof JSTypeOfType typeOfType) {
            return new JSTypeOfType(convertType(typeOfType.inner));
        } else if (baseType instanceof TSVariableType variableType) {
            if (variableType.extendsType == null) return variableType;
            return new TSVariableType(variableType.symbol, convertType(variableType.extendsType));
        } else if (baseType instanceof JSLambdaType lambdaType) {
            return new JSLambdaType(
                    lambdaType.params.stream()
                            .map(t -> new ParamDecl(
                                    t.name(), convertType(t.type()),
                                    t.varArg(), t.optional()
                            )).toList(),
                    convertType(lambdaType.returnType),
                    lambdaType.varArg
            );
        }

        return baseType;
    }
}
