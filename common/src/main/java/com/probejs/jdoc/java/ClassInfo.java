package com.probejs.jdoc.java;


import com.probejs.ProbeJS;
import com.probejs.docs.formatter.ClassResolver;
import com.probejs.docs.formatter.NameResolver;
import com.probejs.jdoc.java.type.ITypeInfo;
import com.probejs.jdoc.java.type.InfoTypeResolver;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.kubejs.server.ServerScriptManager;
import dev.latvian.mods.rhino.JavaMembers;
import dev.latvian.mods.rhino.util.HideFromJS;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class ClassInfo {
    public static final Map<Class<?>, ClassInfo> CLASS_CACHE = new HashMap<>();
    public static final Map<String, ClassInfo> CLASS_NAME_CACHE = new HashMap<>();

    private static int classCount = 0;

    public static ClassInfo getOrCache(Class<?> clazz) {
        //No computeIfAbsent because new ClassInfo will call getOrCache for superclass lookup
        //This will cause a CME because multiple updates occurred in one computeIfAbsent
        if (CLASS_CACHE.containsKey(clazz))
            return CLASS_CACHE.get(clazz);
        classCount += 1;
        if (classCount > 0 && classCount % 10000 == 0) {
            ProbeJS.LOGGER.info("%s classes loaded".formatted(classCount));
        }
        ClassInfo info = ClassResolver.acceptClass(clazz.getName()) ? new ClassInfo(clazz) : new ClassInfo(Object.class);
        CLASS_CACHE.put(clazz, info);
        CLASS_NAME_CACHE.put(info.getName(), info);
        return info;
    }

    private final Class<?> clazzRaw;
    private final String name;
    private final int modifiers;
    private final boolean isInterface;
    private final List<ITypeInfo> parameters;
    private List<MethodInfo> methodInfo;
    private List<FieldInfo> fieldInfo;
    private List<ConstructorInfo> constructorInfo;
    private final ClassInfo superClass;
    private final List<ClassInfo> interfaces;
    private final List<Annotation> annotations;

    private ClassInfo(Class<?> clazz) {
        clazzRaw = clazz;
        name = MethodInfo.getRemappedOrOriginalClass(clazzRaw);
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

        try {
            ScriptManager manager = ServerScriptManager.getScriptManager();
            JavaMembers members = JavaMembers.lookupClass(manager.context, manager.topLevelScope, clazz, clazz, false);
            constructorInfo = members.getAccessibleConstructors().stream().map(ConstructorInfo::new).collect(Collectors.toList());
            methodInfo = members.getAccessibleMethods(manager.context, false).stream()
                    //.filter(m -> !hasIdenticalParentMethod(m.method, clazz))
                    .map(m -> new MethodInfo(m, clazz))
                    .collect(Collectors.toList());
            Set<String> occurred = new HashSet<>();
            Set<String> duplicated = new HashSet<>();

            for (MethodInfo info : methodInfo) {
                String key = getOccurrenceKey(info);
                if (!occurred.contains(key)) {
                    occurred.add(key);
                } else {
                    duplicated.add(key);
                }
            }

            Set<MethodInfo> needsExplicit = methodInfo.stream()
                    .filter(info -> duplicated.contains(getOccurrenceKey(info)))
                    .collect(Collectors.toSet());

            for (MethodInfo info : needsExplicit) {
                MethodInfo explicit = new MethodInfo(info.getMethod(), info.getName(), clazz);
                explicit.setName(explicit.getExplicitName());
                methodInfo.add(explicit);
            }

            fieldInfo = members.getAccessibleFields(manager.context, false)
                    .stream()
                    .filter(f -> f.field.getDeclaringClass() == clazz)
                    .map(FieldInfo::new)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            e.printStackTrace();
            ProbeJS.LOGGER.warn("Error occurred when resolving class %s! Touching the class with KubeJS will likely to crash too!".formatted(clazz.getName()));
            constructorInfo = List.of();
            methodInfo = List.of();
            fieldInfo = List.of();
            NameResolver.putResolvedName(clazz.getName(), new NameResolver.ResolvedName(List.of("probejs", "internal", "NotResolved")));
        }
    }

    private String getOccurrenceKey(MethodInfo info) {
        return "%s(%s)".formatted(info.getName(), info.getParams().size());
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

    private static boolean hasIdenticalParentMethod(Method method, Class<?> clazz) {
        if (method.isDefault())
            return false;

        Class<?> parent = clazz.getSuperclass();
        if (parent == null)
            return false;
        while (parent != null) {
            try {
                Method parentMethod = parent.getMethod(method.getName(), method.getParameterTypes());
                // Check if the generic return type is the same
                return parentMethod.getGenericReturnType().equals(method.getGenericReturnType());
            } catch (NoSuchMethodException e) {
                parent = parent.getSuperclass();
            }
        }
        return false;
    }
}
