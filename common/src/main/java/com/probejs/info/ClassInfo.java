package com.probejs.info;


import com.probejs.ProbeConfig;
import com.probejs.ProbeJS;
import com.probejs.formatter.ClassResolver;
import com.probejs.info.type.ITypeInfo;
import com.probejs.info.type.InfoTypeResolver;
import dev.latvian.mods.rhino.util.HideFromJS;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class ClassInfo {
    public static final Map<Class<?>, ClassInfo> CLASS_CACHE = new HashMap<>();
    public static final Map<String, ClassInfo> CLASS_NAME_CACHE = new HashMap<>();

    public static ClassInfo getOrCache(Class<?> clazz) {
        //No computeIfAbsent because new ClassInfo will call getOrCache for superclass lookup
        //This will cause a CME because multiple updates occurred in one computeIfAbsent
        if (CLASS_CACHE.containsKey(clazz))
            return CLASS_CACHE.get(clazz);
        ClassInfo info = new ClassInfo(clazz);
        CLASS_CACHE.put(clazz, info);
        CLASS_NAME_CACHE.put(info.getName(), info);
        return info;
    }

    private final Class<?> clazzRaw;
    private final String name;
    private final int modifiers;
    private final boolean isInterface;
    private final List<ITypeInfo> parameters;
    private final List<MethodInfo> methodInfo;
    private final List<FieldInfo> fieldInfo;
    private final List<ConstructorInfo> constructorInfo;
    private final ClassInfo superClass;
    private final List<ClassInfo> interfaces;
    private final List<Annotation> annotations;

    private ClassInfo(Class<?> clazz) {
        clazzRaw = clazz;
        name = MethodInfo.getMappedOrDefaultClass(clazzRaw);
        modifiers = clazzRaw.getModifiers();
        isInterface = clazzRaw.isInterface();
        superClass = (clazzRaw.getSuperclass() == Object.class || clazzRaw.getSuperclass() == null) ? null : getOrCache(clazzRaw.getSuperclass());
        interfaces = Arrays.stream(clazzRaw.getInterfaces()).map(ClassInfo::getOrCache).collect(Collectors.toList());
        parameters = Arrays.stream(clazzRaw.getTypeParameters()).map(InfoTypeResolver::resolveType).collect(Collectors.toList());
        annotations = new ArrayList<>();
        annotations.addAll(List.of(clazz.getAnnotations()));

        if (clazz.isAnnotationPresent(HideFromJS.class)) {
            methodInfo = List.of();
            fieldInfo = List.of();
            constructorInfo = List.of();
            return;
        }
        Package pack = clazz.getPackage();
        if (pack != null) {
            try {
                annotations.addAll(List.of(pack.getAnnotations()));
            } catch (NullPointerException e) {
                //Don't know why, might because all package-info classes are not loaded by weird reason
            }
        }
        List<ConstructorInfo> conInfo = new ArrayList<>();
        try {
            conInfo = Arrays.stream(clazzRaw.getConstructors()).map(ConstructorInfo::new).collect(Collectors.toList());
        } catch (Error | Exception e) {
            ProbeJS.LOGGER.warn("Unable to access constructors of class %s".formatted(name));
        }
        constructorInfo = conInfo;

        List<MethodInfo> metInfo = new ArrayList<>();
        try {
            metInfo = Arrays.stream(clazzRaw.getMethods())
                    .filter(m -> !m.isSynthetic())
                    .filter(m -> m.getDeclaringClass() == clazz || m.isDefault())
                    .map(m -> new MethodInfo(m, clazz))
                    .filter(m -> (ClassResolver.acceptMethod(m.getName()) || ProbeConfig.INSTANCE.allowObfuscated))
                    .filter(m -> !m.shouldHide())
                    .collect(Collectors.toList());
        } catch (Error | Exception e) {
            ProbeJS.LOGGER.warn("Unable to access methods of class %s".formatted(name));
            e.printStackTrace();
        }
        methodInfo = metInfo;

        List<FieldInfo> fldInfo = new ArrayList<>();
        try {
            fldInfo = Arrays.stream(clazzRaw.getFields())
                    .filter(f -> f.getDeclaringClass() == clazz)
                    .map(FieldInfo::new)
                    .filter(f -> ClassResolver.acceptField(f.getName()) || ProbeConfig.INSTANCE.allowObfuscated)
                    .filter(f -> !f.shouldHide())
                    .filter(f -> !f.isTransient())
                    .collect(Collectors.toList());
        } catch (Error | Exception e) {
            ProbeJS.LOGGER.warn("Unable to access fields of class %s".formatted(name));
        }
        fieldInfo = fldInfo;
    }


    public boolean isInterface() {
        return isInterface;
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(modifiers);
    }

    public ClassInfo getSuperClass() {
        return superClass;
    }

    public List<ClassInfo> getInterfaces() {
        return interfaces;
    }

    public List<FieldInfo> getFieldInfo() {
        return fieldInfo;
    }

    public List<ConstructorInfo> getConstructorInfo() {
        return constructorInfo;
    }


    public List<MethodInfo> getMethodInfo() {
        return methodInfo;
    }

    public List<ITypeInfo> getParameters() {
        return parameters;
    }

    public boolean isEnum() {
        return clazzRaw.isEnum();
    }

    public Class<?> getClazzRaw() {
        return clazzRaw;
    }

    public String getName() {
        return name;
    }

    public ITypeInfo getSuperClassType() {
        return InfoTypeResolver.resolveType(clazzRaw.getGenericSuperclass());
    }

    public List<ITypeInfo> getInterfaceTypes() {
        return Arrays.stream(clazzRaw.getGenericInterfaces()).map(InfoTypeResolver::resolveType).collect(Collectors.toList());
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }
}
