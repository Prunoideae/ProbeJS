package com.probejs.info;


import com.probejs.formatter.ClassResolver;
import com.probejs.info.type.ITypeInfo;
import com.probejs.info.type.InfoTypeResolver;
import com.probejs.info.type.TypeInfoParameterized;
import com.probejs.info.type.TypeInfoVariable;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClassInfo {
    public static final Map<Class<?>, ClassInfo> CLASS_CACHE = new HashMap<>();

    public static ClassInfo getOrCache(Class<?> clazz) {
        //No computeIfAbsent because new ClassInfo will call getOrCache for superclass lookup
        //This will cause a CME because multiple updates occurred in one computeIfAbsent
        if (CLASS_CACHE.containsKey(clazz))
            return CLASS_CACHE.get(clazz);
        ClassInfo info = new ClassInfo(clazz);
        CLASS_CACHE.put(clazz, info);
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

    private static Map<String, ITypeInfo> resolveTypeOverrides(ITypeInfo typeInfo) {
        Map<String, ITypeInfo> caughtTypes = new HashMap<>();
        if (typeInfo instanceof TypeInfoParameterized parType) {
            List<ITypeInfo> rawClassNames = Arrays.stream(parType.getResolvedClass().getTypeParameters()).map(InfoTypeResolver::resolveType).collect(Collectors.toList());
            List<ITypeInfo> parTypeNames = parType.getParamTypes();
            for (int i = 0; i < parTypeNames.size(); i++) {
                caughtTypes.put(rawClassNames.get(i).getTypeName(), parTypeNames.get(i));
            }
        }
        return caughtTypes;
    }

    private ClassInfo(Class<?> clazz) {
        clazzRaw = clazz;
        name = MethodInfo.RUNTIME.getMappedClass(clazzRaw);
        modifiers = clazzRaw.getModifiers();
        isInterface = clazzRaw.isInterface();
        constructorInfo = Arrays.stream(clazzRaw.getConstructors()).map(ConstructorInfo::new).collect(Collectors.toList());
        superClass = clazzRaw.getSuperclass() == Object.class || clazzRaw.getSuperclass() == null ? null : getOrCache(clazzRaw.getSuperclass());
        interfaces = Arrays.stream(clazzRaw.getInterfaces()).map(ClassInfo::getOrCache).collect(Collectors.toList());
        parameters = Arrays.stream(clazzRaw.getTypeParameters()).map(InfoTypeResolver::resolveType).collect(Collectors.toList());
        methodInfo = Arrays.stream(clazzRaw.getMethods())
                .filter(m -> !m.isSynthetic())
                .filter(m -> m.getDeclaringClass() == clazz)
                .map(m -> new MethodInfo(m, clazz))
                .filter(m -> ClassResolver.acceptMethod(m.getName()))
                .filter(m -> !m.shouldHide())
                .collect(Collectors.toList());
        fieldInfo = Arrays.stream(clazzRaw.getFields())
                .filter(f -> f.getDeclaringClass() == clazz)
                .map(FieldInfo::new)
                .filter(f -> ClassResolver.acceptField(f.getName()))
                .filter(f -> !f.shouldHide())
                .filter(f -> !f.isTransient())
                .collect(Collectors.toList());
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

}
