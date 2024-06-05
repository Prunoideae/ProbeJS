package moe.wolfgirl.next.transpiler;

import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.java.type.TypeDescriptor;
import moe.wolfgirl.next.java.type.impl.*;
import moe.wolfgirl.next.typescript.code.type.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapts a TypeDescriptor into a BaseType
 */
public class TypeConverter {
    public final Map<ClassPath, BaseType> predefinedTypes = new HashMap<>();

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
            return wildcardType.stream().findAny().map(this::convertType).orElse(TSPrimitiveType.ANY);
        }
        throw new RuntimeException("Unknown subclass of TypeDescriptor.");
    }
}
