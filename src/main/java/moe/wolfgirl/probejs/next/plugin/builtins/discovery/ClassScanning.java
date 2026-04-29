package moe.wolfgirl.probejs.next.plugin.builtins.discovery;

import moe.wolfgirl.probejs.ProbeConfig;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.next.plugin.ProbeJSPlugin;
import net.neoforged.fml.ModList;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

public class ClassScanning extends ProbeJSPlugin {

    @Override
    public Set<Class<?>> provideClassForDiscovery() {
        Set<Class<?>> classes = new HashSet<>();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        for (File modFile : findModFiles()) {
            try (var jarFile = new ZipFile(modFile)) {
                var entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    var entry = entries.nextElement();
                    if (entry.isDirectory()) continue;
                    String name = entry.getName();
                    if (!name.endsWith(".class")) continue;
                    name = name.substring(0, name.length() - 6);
                    name = name.replace("/", ".");
                    try {
                        // Skipping due to mojang is weird or other problems
                        if (shouldSkipClass(name)) continue;
                        var clazz = Class.forName(name, false, loader);
                        // You won't refer to anonymous classes anywhere, and they are hard to dump
                        if (clazz.isAnonymousClass()) continue;
                        classes.add(clazz);
                    } catch (Throwable ignore) {
                        ProbeJS.LOGGER.error("Error while loading class %s, don't worry, it's just one class.".formatted(name));
                    }
                }
            } catch (IOException ignore) {
            }
        }

        return classes;
    }

    public static List<File> findModFiles() {
        ModList modList = ModList.get();
        return modList.getModFiles().stream()
                .map(fileInfo -> fileInfo.getFile().getFilePath())
                .map(path -> {
                    try {
                        return path.toFile();
                    } catch (Exception ignore) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static boolean shouldSkipClass(String className) {
        if (className.contains("com.mojang.blaze3d.systems.TimerQuery")) return true;
        var paths = className.split("\\.");
        // Mixin class in a mixin package
        // You must be a very terrible person if you don't name things like this.
        boolean mixinPackage = false;
        for (String path : paths) {
            if (path.equals("mixin") || path.equals("mixins")) {
                mixinPackage = true;
                break;
            }
        }
        if (mixinPackage) return true;
        for (String prefix : ProbeConfig.INSTANCE.excludedPaths.get()) {
            if (className.startsWith(prefix)) return true;
        }
        return false;
    }
}
