package moe.wolfgirl.probejs.lang.java;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.java.clazz.Clazz;
import moe.wolfgirl.probejs.lang.java.clazz.members.ConstructorInfo;
import moe.wolfgirl.probejs.lang.java.clazz.members.FieldInfo;
import moe.wolfgirl.probejs.lang.java.clazz.members.MethodInfo;
import moe.wolfgirl.probejs.lang.java.clazz.members.ParamInfo;
import moe.wolfgirl.probejs.lang.java.type.TypeDescriptor;
import moe.wolfgirl.probejs.lang.java.type.impl.VariableType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ClassRegistry {
    public static final ClassRegistry REGISTRY = new ClassRegistry();

    public Map<ClassPath, Clazz> foundClasses = new HashMap<>();

    public void fromPackage(Collection<ClassPath> classPaths) {
        for (ClassPath pack : classPaths) {
            if (!foundClasses.containsKey(pack)) {
                foundClasses.put(pack, pack.toClazz());
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
            if (c.isSynthetic()) continue;
            if (c.isAnonymousClass()) continue;
            if (!foundClasses.containsKey(new ClassPath(c))) {
                try {
                    Clazz clazz = new Clazz(c);
                    foundClasses.put(clazz.classPath, clazz);
                } catch (Throwable ignored) {
                }
            }
        }
    }

    private Set<Class<?>> retrieveClass(Clazz clazz) {
        Set<Class<?>> classes = new HashSet<>();

        for (ConstructorInfo constructor : clazz.constructors) {
            for (ParamInfo param : constructor.params) {
                classes.addAll(param.type.getClasses());
            }
            for (VariableType variableType : constructor.variableTypes) {
                classes.addAll(variableType.getClasses());
            }
        }

        for (MethodInfo method : clazz.methods) {
            for (ParamInfo param : method.params) {
                classes.addAll(param.type.getClasses());
            }
            for (VariableType variableType : method.variableTypes) {
                classes.addAll(variableType.getClasses());
            }
            classes.addAll(method.returnType.getClasses());
        }

        for (FieldInfo field : clazz.fields) {
            classes.addAll(field.type.getClasses());
        }

        for (VariableType variableType : clazz.variableTypes) {
            classes.addAll(variableType.getClasses());
        }

        if (clazz.superClass != null)
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

    public Collection<Clazz> getFoundClasses() {
        return foundClasses.values();
    }

    public void writeTo(Path path) throws IOException {
        try (var writer = Files.newBufferedWriter(path)) {
            for (ClassPath classPath : foundClasses.keySet()) {
                writer.write(classPath.getClassPathJava() + "\n");
            }
        }
    }

    public void loadFrom(Path path) {
        try (var reader = Files.newBufferedReader(path)) {
            for (String className : (Iterable<String>) reader.lines()::iterator) {
                try {
                    Class<?> loaded = Class.forName(className);
                    fromClasses(Collections.singleton(loaded));
                } catch (Throwable ignored) {
                }
            }
        } catch (IOException ignored) {
        }
    }
}
