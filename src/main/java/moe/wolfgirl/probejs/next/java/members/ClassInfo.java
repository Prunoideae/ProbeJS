package moe.wolfgirl.probejs.next.java.members;

import dev.latvian.mods.rhino.CachedClassInfo;
import dev.latvian.mods.rhino.CachedClassStorage;
import dev.latvian.mods.rhino.CachedFieldInfo;
import dev.latvian.mods.rhino.CachedMethodInfo;
import dev.latvian.mods.rhino.type.TypeInfo;
import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.java.members.other.ClassProvider;
import moe.wolfgirl.probejs.next.java.members.other.HasAnnotation;
import moe.wolfgirl.probejs.next.java.members.other.HasTypeVariable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

        return new ClassInfo(
                new ClassPath(clazz),
                clazz,
                findConstructors(clazz),
                findFields(clazz),
                findMethods(clazz),
                superClass,
                Arrays.stream(clazz.getGenericInterfaces()).map(TypeInfo::of).toList(),
                new ClassAttributes(clazz)
        );
    }

    private static List<MethodInfo> findMethods(Class<?> clazz) {
        CachedClassInfo classInfo = CachedClassStorage.GLOBAL_PUBLIC.get(clazz);
        return classInfo.getAccessibleMethods(false)
                .stream()
                .map(CachedMethodInfo.Accessible::getInfo)
                .filter(m -> shouldIncludeMethod(m.getCached(), clazz))
                .map(MethodInfo::new)
                .toList();
    }

    private static List<ConstructorInfo> findConstructors(Class<?> clazz) {
        CachedClassInfo classInfo = CachedClassStorage.GLOBAL_PUBLIC.get(clazz);
        return classInfo.getConstructors()
                .stream()
                .map(ConstructorInfo::new)
                .toList();
    }

    private static List<FieldInfo> findFields(Class<?> clazz) {
        CachedClassInfo classInfo = CachedClassStorage.GLOBAL_PUBLIC.get(clazz);
        return classInfo.getAccessibleFields(false)
                .stream()
                .map(CachedFieldInfo.Accessible::getInfo)
                .map(FieldInfo::new)
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
