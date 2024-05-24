package moe.wolfgirl.next.java.clazz;

import moe.wolfgirl.next.java.base.ClassPathProvider;
import moe.wolfgirl.next.java.base.TypeVariableHolder;
import moe.wolfgirl.next.java.clazz.members.ConstructorInfo;
import moe.wolfgirl.next.java.clazz.members.FieldInfo;
import moe.wolfgirl.next.java.clazz.members.MethodInfo;
import moe.wolfgirl.next.java.clazz.members.ParamInfo;
import moe.wolfgirl.next.java.type.TypeAdapter;
import moe.wolfgirl.next.java.type.TypeDescriptor;
import moe.wolfgirl.next.java.type.impl.VariableType;
import moe.wolfgirl.next.utils.RemapperUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class Clazz extends TypeVariableHolder implements ClassPathProvider {
    public final ClassPath classPath;
    public final List<ConstructorInfo> constructors;
    public final List<FieldInfo> fields;
    public final List<MethodInfo> methods;
    public final TypeDescriptor superClass;
    public final List<TypeDescriptor> interfaces;
    public final ClassAttribute attribute;

    public Clazz(Class<?> clazz) {
        super(clazz.getTypeParameters(), clazz.getAnnotations());

        this.classPath = new ClassPath(clazz);
        this.constructors = RemapperUtils.getConstructors(clazz)
                .stream()
                .map(ConstructorInfo::new)
                .collect(Collectors.toList());
        this.fields = RemapperUtils.getFields(clazz)
                .stream()
                .map(FieldInfo::new)
                .collect(Collectors.toList());
        this.methods = RemapperUtils.getMethods(clazz)
                .stream()
                .filter(m -> !m.method.isSynthetic())
                .filter(m -> !hasIdenticalParentMethodAndEnsureNotDirectlyImplementsInterfaceSinceTypeScriptDoesNotHaveInterfaceAtRuntimeInTypeDeclarationFilesJustBecauseItSucks(m.method, clazz))
                .map(MethodInfo::new)
                .collect(Collectors.toList());
        this.superClass = TypeAdapter.getTypeDescription(clazz.getAnnotatedSuperclass());
        this.interfaces = Arrays.stream(clazz.getAnnotatedInterfaces())
                .map(TypeAdapter::getTypeDescription)
                .collect(Collectors.toList());
        this.attribute = new ClassAttribute(clazz);
    }

    @Override
    public Collection<ClassPath> getClassPaths() {
        Set<ClassPath> paths = new HashSet<>();
        for (ConstructorInfo constructor : constructors) {
            paths.addAll(constructor.getClassPaths());
        }
        for (FieldInfo field : fields) {
            paths.addAll(field.getClassPaths());
        }
        for (MethodInfo method : methods) {
            paths.addAll(method.getClassPaths());
        }
        paths.addAll(superClass.getClassPaths());
        for (TypeDescriptor i : interfaces) {
            paths.addAll(i.getClassPaths());
        }
        for (VariableType variableType : variableTypes) {
            paths.addAll(variableType.getClassPaths());
        }
        return paths;
    }

    @Override
    public int hashCode() {
        return classPath.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Clazz clazz = (Clazz) o;
        return Objects.equals(classPath, clazz.classPath);
    }

    public Set<ClassPath> getUsedClasses() {
        Set<ClassPath> used = new HashSet<>();

        for (MethodInfo method : methods) {
            used.addAll(method.returnType.getClassPaths());
            for (ParamInfo param : method.params) {
                used.addAll(param.type.getClassPaths());
            }
        }

        for (FieldInfo field : fields) {
            used.addAll(field.type.getClassPaths());
        }

        for (ConstructorInfo constructor : constructors) {
            for (ParamInfo param : constructor.params) {
                used.addAll(param.type.getClassPaths());
            }
        }

        used.addAll(superClass.getClassPaths());
        for (TypeDescriptor i : interfaces) {
            used.addAll(i.getClassPaths());
        }

        for (VariableType variableType : variableTypes) {
            used.addAll(variableType.getClassPaths());
        }

        return used;
    }

    private static boolean hasIdenticalParentMethodAndEnsureNotDirectlyImplementsInterfaceSinceTypeScriptDoesNotHaveInterfaceAtRuntimeInTypeDeclarationFilesJustBecauseItSucks(Method method, Class<?> clazz) {
        Class<?> parent = clazz.getSuperclass();
        if (parent == null)
            return false;
        while (parent != null && !parent.isInterface()) {
            try {
                Method parentMethod = parent.getMethod(method.getName(), method.getParameterTypes());
                return parentMethod.equals(method);
            } catch (NoSuchMethodException e) {
                parent = parent.getSuperclass();
            }
        }
        return false;
    }

    public enum ClassType {
        INTERFACE,
        ENUM,
        RECORD,
        CLASS
    }

    public static class ClassAttribute {

        public final ClassType type;
        public final boolean isAbstract;
        public final boolean isFunctionalInterface;
        public final Class<?> raw;


        public ClassAttribute(Class<?> clazz) {
            if (clazz.isInterface()) {
                this.type = ClassType.INTERFACE;
            } else if (clazz.isEnum()) {
                this.type = ClassType.ENUM;
            } else if (clazz.isRecord()) {
                this.type = ClassType.RECORD;
            } else {
                this.type = ClassType.CLASS;
            }

            int modifiers = clazz.getModifiers();
            this.isAbstract = Modifier.isAbstract(modifiers);
            this.isFunctionalInterface = type == ClassType.INTERFACE &&
                    Arrays.stream(clazz.getMethods()).filter(m -> !m.isDefault()).count() == 1;
            this.raw = clazz;
        }
    }
}
