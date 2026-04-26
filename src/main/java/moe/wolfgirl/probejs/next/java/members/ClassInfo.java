package moe.wolfgirl.probejs.next.java.members;

import dev.latvian.mods.rhino.CachedClassInfo;
import dev.latvian.mods.rhino.CachedClassStorage;
import dev.latvian.mods.rhino.CachedFieldInfo;
import dev.latvian.mods.rhino.CachedMethodInfo;
import dev.latvian.mods.rhino.type.ArrayTypeInfo;
import dev.latvian.mods.rhino.type.ParameterizedTypeInfo;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.latvian.mods.rhino.type.VariableTypeInfo;
import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.java.members.other.ClassProvider;
import moe.wolfgirl.probejs.next.java.members.other.HasAnnotation;
import moe.wolfgirl.probejs.next.java.members.other.HasTypeVariable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;

// TODO: Add HideFromJS back when things are tested
// @HideFromJS
public record ClassInfo(
        ClassPath classPath,
        Class<?> clazz,
        List<ConstructorInfo> constructors,
        List<FieldInfo> fields,
        List<MethodInfo> methods,
        TypeInfo superClass,
        List<TypeInfo> interfaces,
        ClassAttributes attributes
) implements HasAnnotation, HasTypeVariable, ClassProvider {

    public static ClassInfo resolve(Class<?> clazz) {
        var superClass = TypeInfo.of(clazz.getGenericSuperclass());
        if (superClass.asClass().equals(Object.class)) {
            superClass = TypeInfo.NONE;
        }

        Map<String, TypeInfo> variableRemaps = getVariableRemaps(clazz);

        return new ClassInfo(
                new ClassPath(clazz),
                clazz,
                findConstructors(clazz, variableRemaps),
                findFields(clazz, variableRemaps),
                findMethods(clazz, variableRemaps),
                superClass,
                Arrays.stream(clazz.getGenericInterfaces()).map(TypeInfo::of).toList(),
                new ClassAttributes(clazz)
        );
    }

    private static List<ConstructorInfo> findConstructors(Class<?> clazz, Map<String, TypeInfo> variableRemaps) {
        CachedClassInfo classInfo = CachedClassStorage.GLOBAL_PUBLIC.get(clazz);
        return classInfo.getConstructors()
                .stream()
                .map(c -> new ConstructorInfo(c, variableRemaps))
                .toList();
    }

    private static List<FieldInfo> findFields(Class<?> clazz, Map<String, TypeInfo> variableRemaps) {
        CachedClassInfo classInfo = CachedClassStorage.GLOBAL_PUBLIC.get(clazz);
        return classInfo.getAccessibleFields(false)
                .stream()
                .map(CachedFieldInfo.Accessible::getInfo)
                .map(f -> new FieldInfo(f, variableRemaps))
                .toList();
    }

    private static List<MethodInfo> findMethods(Class<?> clazz, Map<String, TypeInfo> variableRemaps) {
        CachedClassInfo classInfo = CachedClassStorage.GLOBAL_PUBLIC.get(clazz);
        return classInfo.getAccessibleMethods(false)
                .stream()
                .map(CachedMethodInfo.Accessible::getInfo)
                .filter(m -> shouldIncludeMethod(m.getCached(), clazz))
                .map(m -> MethodInfo.resolve(m, variableRemaps))
                .toList();
    }


    // Find methods that:
    // Not inherited from superclass (or overrides superclass method), as we can reuse the type
    // declaration in the superclass
    // or default methods in interfaces, because TypeScript's interface disappears at runtime,
    // so it requires the class to have all interface methods implemented.
    private static boolean shouldIncludeMethod(Method method, Class<?> clazz) {
        for (Class<?> parent = clazz.getSuperclass(); parent != null; parent = parent.getSuperclass()) {
            try {
                Method parentMethod = parent.getMethod(method.getName(), method.getParameterTypes());
                if (parentMethod.equals(method)) {
                    return false;
                }
            } catch (NoSuchMethodException ignored) {
            }
        }

        if (method.getDeclaringClass() == clazz) {
            return true;
        }

        return !clazz.isInterface() && method.isDefault() && method.getDeclaringClass().isInterface();
    }

    // Since Java's Method can belong to superclass/superinterface, we need to remap the type variables in the method
    // signature to the ones in the current class.
    // example:
    // Collection<E> implements Iterable<E>, but in definition of Iterable, it's Iterable<T>, so forEach(Consumer<T>)
    // will be dumped, we need to find that the T->E and remap the method signature to forEach(Consumer<E>) in the dump
    private static Map<String, TypeInfo> getVariableRemaps(Class<?> clazz) {
        Map<String, TypeInfo> remaps = new LinkedHashMap<>();
        collectVariableRemaps(clazz, Map.of(), remaps);
        return remaps;
    }

    private static void collectVariableRemaps(
            Class<?> currentClass,
            Map<String, TypeInfo> currentTypeArguments,
            Map<String, TypeInfo> remaps
    ) {
        Type genericSuperClass = currentClass.getGenericSuperclass();
        if (genericSuperClass != null) {
            collectParentVariableRemaps(genericSuperClass, currentTypeArguments, remaps);
        }

        for (Type genericInterface : currentClass.getGenericInterfaces()) {
            collectParentVariableRemaps(genericInterface, currentTypeArguments, remaps);
        }
    }

    private static void collectParentVariableRemaps(
            Type parentType,
            Map<String, TypeInfo> currentTypeArguments,
            Map<String, TypeInfo> remaps
    ) {
        Class<?> parentClass = TypeInfo.of(parentType).asClass();
        if (parentClass == null || parentClass == Object.class) {
            return;
        }

        Map<String, TypeInfo> parentTypeArguments = resolveTypeArguments(parentType, parentClass, currentTypeArguments);
        parentTypeArguments.forEach(remaps::putIfAbsent);
        collectVariableRemaps(parentClass, parentTypeArguments, remaps);
    }

    private static Map<String, TypeInfo> resolveTypeArguments(
            Type inheritedType,
            Class<?> inheritedClass,
            Map<String, TypeInfo> currentTypeArguments
    ) {
        TypeVariable<?>[] typeParameters = inheritedClass.getTypeParameters();
        if (typeParameters.length == 0) {
            return Map.of();
        }

        Map<String, TypeInfo> remaps = new LinkedHashMap<>(typeParameters.length);
        if (inheritedType instanceof ParameterizedType parameterizedType) {
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            for (int index = 0; index < typeParameters.length; index++) {
                remaps.put(typeParameters[index].getName(), resolveType(actualTypeArguments[index], currentTypeArguments));
            }
            return remaps;
        }

        for (TypeVariable<?> typeParameter : typeParameters) {
            remaps.put(typeParameter.getName(), TypeInfo.OBJECT);
        }
        return remaps;
    }

    private static TypeInfo resolveType(Type type, Map<String, TypeInfo> currentTypeArguments) {
        TypeInfo resolved = TypeInfo.of(type);
        for (Map.Entry<String, TypeInfo> entry : currentTypeArguments.entrySet()) {
            resolved = remapTypeVariable(resolved, entry.getKey(), entry.getValue());
        }
        return resolved;
    }

    private static TypeInfo remapTypeVariable(TypeInfo typeInfo, String variable, TypeInfo replacement) {
        return switch (typeInfo) {
            case VariableTypeInfo variableTypeInfo ->
                    variableTypeInfo.getName().equals(variable) ? replacement : typeInfo;
            case ArrayTypeInfo arrayTypeInfo ->
                    remapTypeVariable(arrayTypeInfo.componentType(), variable, replacement).asArray();
            case ParameterizedTypeInfo parameterizedTypeInfo -> parameterizedTypeInfo.rawType().withParams(
                    Arrays.stream(parameterizedTypeInfo.params())
                            .map(parameter -> remapTypeVariable(parameter, variable, replacement))
                            .toArray(TypeInfo[]::new)
            );
            case null, default -> typeInfo;
        };

    }

    @Override
    public Annotation[] getAnnotations() {
        return clazz.getAnnotations();
    }

    @Override
    public TypeVariable<?>[] getTypeVariables() {
        return clazz.getTypeParameters();
    }

    @Override
    public Collection<Class<?>> getReferredClasses() {
        Set<Class<?>> classes = new HashSet<>();
        for (ConstructorInfo constructorInfo : constructors) {
            classes.addAll(constructorInfo.getReferredClasses());
        }
        for (FieldInfo fieldInfo : fields) {
            classes.addAll(fieldInfo.getReferredClasses());
        }
        for (MethodInfo methodInfo : methods) {
            classes.addAll(methodInfo.getReferredClasses());
        }
        classes.addAll(superClass.getContainedComponentClasses());
        for (TypeInfo i : interfaces) {
            classes.addAll(i.getContainedComponentClasses());
        }
        return classes;
    }

    public static TypeInfo remapType(TypeInfo typeInfo, Map<String, TypeInfo> typeRemap) {
        return switch (typeInfo) {
            case VariableTypeInfo variableTypeInfo -> typeRemap.getOrDefault(variableTypeInfo.getName(), typeInfo);
            case ArrayTypeInfo arrayTypeInfo -> remapType(arrayTypeInfo.componentType(), typeRemap).asArray();
            case ParameterizedTypeInfo paramTypeInfo -> {
                TypeInfo baseType = paramTypeInfo.rawType(); // Variable can't be base of parameterized type, so no remap
                List<TypeInfo> remappedParams = Arrays.stream(paramTypeInfo.params())
                        .map(param -> remapType(param, typeRemap))
                        .toList();
                yield baseType.withParams(remappedParams.toArray(new TypeInfo[0]));
            }
            case null, default -> typeInfo;
        };
    }


    public enum ClassType {
        CLASS, INTERFACE, ENUM, RECORD, ANNOTATION
    }

    public record ClassAttributes(ClassType classType, boolean isAbstract) {
        public ClassAttributes(Class<?> clazz) {
            this(getClassType(clazz), Modifier.isAbstract(clazz.getModifiers()));
        }

        public boolean isClass() {
            return classType == ClassType.CLASS;
        }

        public boolean isInterface() {
            return classType == ClassType.INTERFACE;
        }

        public boolean isEnum() {
            return classType == ClassType.ENUM;
        }

        public boolean isRecord() {
            return classType == ClassType.RECORD;
        }

        public boolean isAnnotation() {
            return classType == ClassType.ANNOTATION;
        }

        private static ClassType getClassType(Class<?> clazz) {
            if (clazz.isAnnotation()) {
                return ClassType.ANNOTATION;
            } else if (clazz.isEnum()) {
                return ClassType.ENUM;
            } else if (clazz.isRecord()) {
                return ClassType.RECORD;
            } else if (clazz.isInterface()) {
                return ClassType.INTERFACE;
            } else {
                return ClassType.CLASS;
            }
        }
    }
}
