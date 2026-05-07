package moe.wolfgirl.probejs.typescript.transpiler;

import dev.latvian.mods.kubejs.util.ClassWrapper;
import dev.latvian.mods.kubejs.util.KubeResourceLocation;
import dev.latvian.mods.rhino.type.*;
import moe.wolfgirl.probejs.typescript.ClassPath;
import moe.wolfgirl.probejs.typescript.document.Types;
import moe.wolfgirl.probejs.typescript.document.base.Type;
import moe.wolfgirl.probejs.typescript.document.types.ClassType;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class TypeConverter {
    private final Map<ClassPath, Type> predefinedTypes = new HashMap<>();

    public TypeConverter() {
        addType(String.class, Types.STRING);
        addType(KubeResourceLocation.class, Types.clazz(ResourceLocation.class));
    }

    public void addType(Class<?> clazz, Type code) {
        predefinedTypes.put(new ClassPath(clazz), code);
    }

    public Type convertType(TypeInfo typeInfo) {
        return convertType(typeInfo, true, new HashSet<>());
    }

    public Type convertType(TypeInfo typeInfo, boolean canHaveParams, Set<String> seenVariables) {
        if (typeInfo == TypeInfo.NONE) return Types.NEVER;

        return switch (typeInfo) {
            case ClassTypeInfo info -> {
                if (info.isVoid()) yield Types.VOID;
                else if (info.isBoolean()) yield Types.BOOLEAN;
                else if (info.isByte() || info.isShort() || info.isInt() || info.isLong() || info.isFloat() || info.isDouble())
                    yield Types.NUMBER;
                else if (info.isCharacter()) yield Types.STRING;

                Class<?> clazz = info.asClass();
                ClassPath classPath = new ClassPath(clazz);
                if (predefinedTypes.containsKey(classPath)) yield predefinedTypes.get(classPath);
                ClassType classType = new ClassType(classPath);
                if (clazz.getTypeParameters().length != 0 && canHaveParams) {
                    Type[] params = Collections.nCopies(clazz.getTypeParameters().length, Types.ANY).toArray(new Type[0]);
                    yield classType.withParams(params);
                } else yield classType;
            }
            case ArrayTypeInfo info -> convertType(info.componentType(), true, seenVariables).asArray();
            case VariableTypeInfo info -> {
                // T extends List<T>
                TypeInfo bound = info.getMainBound();
                boolean recursive = seenVariables.contains(info.getName());
                seenVariables.add(info.getName());
                if (bound == TypeInfo.NONE || recursive) yield Types.variable(info.getName());
                else yield Types.variable(info.getName(), convertType(bound, true, seenVariables));
            }
            case ParameterizedTypeInfo info -> {
                TypeInfo base = info.rawType();
                if (base == TypeInfo.RAW_OPTIONAL) yield Types.optional(convertType(info.param(0)));
                if (base.asClass().equals(ClassWrapper.class)) yield Types.typeOf(convertType(info.param(0)));

                Type baseType = convertType(base, false, seenVariables);
                var params = Arrays.stream(info.params())
                        .map(param -> convertType(param, true, seenVariables))
                        .toArray(Type[]::new);
                yield baseType.withParams(params);
            }
            case null, default -> Types.ANY;
        };
    }
}
