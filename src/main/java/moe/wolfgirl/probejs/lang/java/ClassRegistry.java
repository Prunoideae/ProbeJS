package moe.wolfgirl.probejs.lang.java;

import dev.latvian.mods.rhino.util.HideFromJS;
import moe.wolfgirl.probejs.ProbeConfig;
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
import java.util.stream.Collectors;

@HideFromJS
public class ClassRegistry {
    public static final ClassRegistry REGISTRY = new ClassRegistry();

    private final Map<ClassPath, Clazz> foundClasses = new HashMap<>();

    public void putClass(ClassPath classPath, Clazz clazz) {
        if (classPath.getName().contains("-")) return;
        foundClasses.put(classPath, clazz);
    }

    public void fromClazz(Collection<Clazz> classes) {
        for (Clazz c : classes) {
            if (!foundClasses.containsKey(c.classPath)) {
                putClass(c.classPath, c);
            }
        }
    }

    public void fromClasses(Collection<Class<?>> classes, int recursionDepth) {
        for (Class<?> c : classes) {
            try {
                // We test if the class actually exists from forName
                // I think some runtime class can have non-existing Class<?> object due to .getSuperClass
                // or .getInterfaces
                Class.forName(c.getName());
            } catch (Throwable ignore) {
                continue;
            }

            try {
                if (c.isSynthetic()) continue;
                if (c.isAnonymousClass()) continue;
                if (!foundClasses.containsKey(new ClassPath(c))) {
                    Clazz clazz = new Clazz(c);
                    clazz.recursionDepth = recursionDepth;
                    putClass(clazz.classPath, clazz);
                }
            } catch (Throwable ignored) {
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
        // We mark the recursion depth of the class, so a class with depth X
        // will need X jumps from any found classes to be referenced
        Set<Clazz> currentClasses = new HashSet<>(foundClasses.values());
        int recursion = 1;
        while (!currentClasses.isEmpty()) {
            Set<Class<?>> fetchedClass = new HashSet<>();
            for (Clazz currentClass : currentClasses) {
                fetchedClass.addAll(retrieveClass(currentClass));
            }
            fetchedClass.removeIf(clazz -> foundClasses.containsKey(new ClassPath(clazz)));
            currentClasses.clear();
            for (Class<?> c : fetchedClass) {
                try {
                    Class.forName(c.getName());
                    Clazz clazz = new Clazz(c);
                    clazz.recursionDepth = recursion;
                    putClass(clazz.classPath, clazz);
                    currentClasses.add(clazz);
                } catch (Throwable ignore) {
                }
            }
            recursion++;
        }
    }

    public Collection<Clazz> getFoundClasses() {
        int allowedDepth = ProbeConfig.INSTANCE.recursionDepth.get();
        return foundClasses.values()
                .stream()
                .filter(clazz -> clazz.recursionDepth <= allowedDepth)
                .collect(Collectors.toSet());
    }

    public void writeTo(Path path) throws IOException {
        try (var writer = Files.newBufferedWriter(path)) {
            for (Map.Entry<ClassPath, Clazz> entry : foundClasses.entrySet()) {
                writer.write("%s\t%s\n".formatted(
                        entry.getKey().getClassPathJava(),
                        entry.getValue().recursionDepth
                ));
            }
        }
    }

    public void loadFrom(Path path) {
        try (var reader = Files.newBufferedReader(path)) {
            for (String parts : (Iterable<String>) reader.lines()::iterator) {
                try {
                    String[] classRecursion = parts.split("\t");
                    Class<?> loaded = Class.forName(classRecursion[0]);
                    fromClasses(Collections.singleton(loaded), Integer.parseInt(classRecursion[1]));
                } catch (Throwable ignored) {
                }
            }
        } catch (Exception ignored) {
        }
    }
}
