package moe.wolfgirl.probejs.lang.transpiler;

import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.desc.*;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.java.type.TypeDescriptor;
import moe.wolfgirl.probejs.lang.java.type.impl.*;
import moe.wolfgirl.probejs.lang.typescript.code.member.ParamDecl;
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
    public static final String PROBEJS_PREFIX = "$$probejs$$";
    public static final DescriptionContext PROBEJS = new DescriptionContext() {
        @Override
        public String typeName(Class<?> type) {
            return PROBEJS_PREFIX + type.getName();
        }
    };

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
            Generics generics = paramType.getAnnotation(Generics.class);
            if (generics != null) {
                BaseType baseType = new TSClassType(new ClassPath(generics.base()));
                List<BaseType> params = Arrays.stream(generics.value())
                        .map(c -> (BaseType) new TSClassType(new ClassPath(c)))
                        .toList();
                return new TSParamType(baseType, params);
            }

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
                                    t.name, convertType(t.type),
                                    t.varArg, t.optional
                            )).toList(),
                    convertType(lambdaType.returnType)
            );
        }
        return baseType;
    }

    public BaseType convertType(TypeDescJS typeDesc) {
        if (typeDesc instanceof ArrayDescJS arrayDesc) {
            return convertType(arrayDesc.type()).asArray();
        } else if (typeDesc instanceof FixedArrayDescJS fixedArrayDesc) {
            return new JSArrayType(Arrays.stream(fixedArrayDesc.types())
                    .map(this::convertType)
                    .toList());
        } else if (typeDesc instanceof GenericDescJS genericDesc) {
            if (genericDesc.type() instanceof PrimitiveDescJS primitiveDesc && primitiveDesc.type().equals("Map")) {
                if (genericDesc.types().length != 2) return Types.ANY;
                BaseType valueType = convertType(genericDesc.types()[1]);
                return Types.custom(
                        (decl, formatType) -> "{[k: string]: %s}".formatted(valueType.line(decl, formatType)),
                        valueType.getUsedClassPaths().toArray(new ClassPath[0])
                );
            }

            return new TSParamType(
                    convertType(genericDesc.type()),
                    Arrays.stream(genericDesc.types()).map(this::convertType).toList()
            );
        } else if (typeDesc instanceof ObjectDescJS objectDesc) {
            Map<String, BaseType> members = new HashMap<>();
            for (ObjectDescJS.Entry type : objectDesc.types()) {
                String name = type.key();
                if (type.optional()) name = name + "?";
                members.put(name, convertType(type.value()));
            }
            return new JSObjectType(members);
        } else if (typeDesc instanceof OrDescJS orDesc) {
            return new JSJoinedType.Union(
                    Arrays.stream(orDesc.types()).map(this::convertType).toList());
        } else if (typeDesc instanceof PrimitiveDescJS primitiveDesc) {
            String content = primitiveDesc.type();
            if (content.startsWith(PROBEJS_PREFIX)) {
                content = content.substring(PROBEJS_PREFIX.length());
                String[] parts = content.split("\\.");
                parts[parts.length - 1] = "$" + parts[parts.length - 1];
                return new TSClassType(new ClassPath(Arrays.stream(parts).toList()));
            } else {
                return Types.primitive(content);
            }
        }
        
        throw new RuntimeException("Unknown TypeDescJS");
    }
}
