package com.probejs.next.java;

import com.probejs.ProbeJS;
import com.probejs.next.java.clazz.Clazz;
import com.probejs.next.java.clazz.ClassPath;
import com.probejs.next.java.clazz.members.ConstructorInfo;
import com.probejs.next.java.clazz.members.FieldInfo;
import com.probejs.next.java.clazz.members.MethodInfo;
import com.probejs.next.java.clazz.members.ParamInfo;
import com.probejs.next.java.type.TypeDescriptor;
import com.probejs.next.java.type.impl.VariableType;

import java.util.*;

public class ClassRegistry {
    public Map<ClassPath, Clazz> foundClasses = new HashMap<>();

    public void fromPackage(Collection<ClassPath> classPaths) {
        for (ClassPath pack : classPaths) {
            if (!foundClasses.containsKey(pack)) {
                try {
                    foundClasses.put(pack, pack.toClazz());
                } catch (ClassNotFoundException e) {
                    ProbeJS.LOGGER.warn("Failed to load classes %s!".formatted(pack.getClassPath()));
                }
            }
        }
    }

    public void fromClazz(Collection<Clazz> classes) {
        for (Clazz c : classes) {
            if (!foundClasses.containsKey(c.classPath)) {
                foundClasses.put(c.classPath, c);
            }
        }
    }

    public void fromClasses(Collection<Class<?>> classes) {
        for (Class<?> c : classes) {
            if (!foundClasses.containsKey(new ClassPath(c))) {
                Clazz clazz = new Clazz(c);
                foundClasses.put(clazz.classPath, clazz);
            }
        }
    }

    private Set<Class<?>> retrieveClass(Clazz clazz) {
        Set<Class<?>> classes = new HashSet<>();

        for (ConstructorInfo constructor : clazz.constructors) {
            for (ParamInfo param : constructor.params) {
                classes.addAll(param.getType().getClasses());
            }
            for (VariableType variableType : constructor.getVariableTypes()) {
                classes.addAll(variableType.getClasses());
            }
        }

        for (MethodInfo method : clazz.methods) {
            for (ParamInfo param : method.params) {
                classes.addAll(param.getType().getClasses());
            }
            for (VariableType variableType : method.getVariableTypes()) {
                classes.addAll(variableType.getClasses());
            }
            classes.addAll(method.returnType.getClasses());
        }

        for (FieldInfo field : clazz.fields) {
            classes.addAll(field.type.getClasses());
        }

        for (VariableType variableType : clazz.getVariableTypes()) {
            classes.addAll(variableType.getClasses());
        }

        classes.addAll(clazz.superClass.getClasses());
        for (TypeDescriptor i : clazz.interfaces) {
            classes.addAll(i.getClasses());
        }

        return classes;
    }

    public void discoverClasses() {
        Set<Clazz> currentClasses = new HashSet<>(foundClasses.values());

        while (!currentClasses.isEmpty()) {
            Set<Class<?>> fetchedClass = new HashSet<>();
            for (Clazz currentClass : currentClasses) {
                fetchedClass.addAll(retrieveClass(currentClass));
            }
            fetchedClass.removeIf(clazz -> foundClasses.containsKey(new ClassPath(clazz)));
            currentClasses.clear();
            for (Class<?> c : fetchedClass) {
                Clazz clazz = new Clazz(c);
                foundClasses.put(clazz.classPath, clazz);
                currentClasses.add(clazz);
            }
        }
    }

    public Map<ClassPath, Clazz> getFoundClasses() {
        return foundClasses;
    }
}
