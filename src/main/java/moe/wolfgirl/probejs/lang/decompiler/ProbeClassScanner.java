package moe.wolfgirl.probejs.lang.decompiler;

import moe.wolfgirl.probejs.ProbeConfig;
import moe.wolfgirl.probejs.ProbeJS;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ProbeClassScanner {
    private final Set<Class<?>> scannedClasses = new HashSet<>();

    private static boolean shouldRejectClassPath(String name) {
        if (name.contains("com.mojang.blaze3d.systems.TimerQuery")) return true;
        var paths = name.split("\\.");

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
            if (name.startsWith(prefix)) return true;
        }
        return false;
    }

    public void acceptFile(File file) throws IOException {
        ClassLoader loader = getClass().getClassLoader();
        try (var jarFile = new ZipFile(file)) {
            var entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) continue;
                String name = entry.getName();
                if (!name.endsWith(".class")) continue;
                name = name.substring(0, name.length() - 6);
                name = name.replace("/", ".");
                try {
                    // Skipping due to mojang is weird or other problems
                    if (shouldRejectClassPath(name)) continue;
                    scannedClasses.add(Class.forName(name, false, loader));
                } catch (Throwable ignore) {
                    ProbeJS.LOGGER.error("Error while loading class %s, consider add it to excluded classpaths.".formatted(name));
                }
            }
        }
    }

    public Set<Class<?>> getScannedClasses() {
        return scannedClasses;
    }
}
