package moe.wolfgirl.probejs.lang.java;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.latvian.mods.rhino.type.VariableTypeInfo;
import dev.latvian.mods.rhino.util.HideFromJS;
import moe.wolfgirl.probejs.ProbeConfig;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.java.clazz.Clazz;
import moe.wolfgirl.probejs.lang.java.clazz.members.ConstructorInfo;
import moe.wolfgirl.probejs.lang.java.clazz.members.FieldInfo;
import moe.wolfgirl.probejs.lang.java.clazz.members.MethodInfo;
import moe.wolfgirl.probejs.lang.java.clazz.members.ParamInfo;
import moe.wolfgirl.probejs.utils.GameUtils;
import moe.wolfgirl.probejs.utils.ProbeFileUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@HideFromJS
public class ClassRegistry {
    public static final ClassRegistry REGISTRY = new ClassRegistry();
    public static final Set<String> DENIED_CLASSES = new HashSet<>();
    private final Map<ClassPath, Clazz> foundClasses = new HashMap<>();

    static {
        DENIED_CLASSES.add("foundry.veil.api.client.imgui.VeilImGuiUtil");
        DENIED_CLASSES.add("foundry.veil.api.client.imgui.VeilImGui");
    }

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
        ClassLoader thisLoader = getClass().getClassLoader();
        for (Class<?> c : classes) {
            if (DENIED_CLASSES.contains(c.getName())) continue;
            try {
                // We test if the class actually exists from forName
                // I think some runtime class can have non-existing Class<?> object due to .getSuperClass
                // or .getInterfaces
                Class.forName(c.getName(), false, thisLoader);
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
                classes.addAll(param.type.getContainedComponentClasses());
            }
            for (VariableTypeInfo variableType : constructor.variableTypes) {
                classes.addAll(variableType.getContainedComponentClasses());
            }
        }

        for (MethodInfo method : clazz.methods) {
            for (ParamInfo param : method.params) {
                classes.addAll(param.type.getContainedComponentClasses());
            }
            for (VariableTypeInfo variableType : method.variableTypes) {
                classes.addAll(variableType.getContainedComponentClasses());
            }
            classes.addAll(method.returnType.getContainedComponentClasses());
        }

        for (FieldInfo field : clazz.fields) {
            classes.addAll(field.type.getContainedComponentClasses());
        }

        for (VariableTypeInfo variableType : clazz.variableTypes) {
            classes.addAll(variableType.getContainedComponentClasses());
        }

        if (clazz.superClass != null)
            classes.addAll(clazz.superClass.getContainedComponentClasses());
        for (TypeInfo i : clazz.interfaces) {
            classes.addAll(i.getContainedComponentClasses());
        }

        return classes;
    }

    @SuppressWarnings("BusyWait")
    public void discoverClasses() {
        MutableBoolean bool = new MutableBoolean(false);

        RenderSystem.recordRenderCall(() -> {
            var loader = getClass().getClassLoader();
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
                        if (c.isPrimitive()) continue;
                        Class.forName(c.getName(), false, loader);
                        Clazz clazz = new Clazz(c);
                        clazz.recursionDepth = recursion;
                        putClass(clazz.classPath, clazz);
                        currentClasses.add(clazz);
                    } catch (Throwable err) {
                        ProbeJS.LOGGER.error("Error occurred when resolving class %s".formatted(c));
                        GameUtils.logException(err);
                    }
                }
                recursion++;
            }
            bool.setTrue();
        });

        while (bool.isFalse()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Collection<Clazz> getFoundClasses() {
        int allowedDepth = ProbeConfig.INSTANCE.recursionDepth.get();
        return foundClasses.values()
                .stream()
                .filter(clazz -> clazz.recursionDepth <= allowedDepth)
                .filter(clazz -> !clazz.classPath.getName().contains("package-info"))
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
