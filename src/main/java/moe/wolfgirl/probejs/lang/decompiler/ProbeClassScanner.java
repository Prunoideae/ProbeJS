package moe.wolfgirl.probejs.lang.decompiler;

import org.jetbrains.java.decompiler.main.extern.IContextSource;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ProbeClassScanner {
    private final Set<Class<?>> scannedClasses = new HashSet<>();

    public void acceptFile(File file) throws IOException {
        try (var jarFile = new ZipFile(file)) {
            var entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) continue;
                String name = entry.getName();
                if (!name.endsWith(IContextSource.CLASS_SUFFIX)) continue;
                name = name.substring(0, name.length() - IContextSource.CLASS_SUFFIX.length());
                name = name.replace("/", ".");
                try {
                    // Skipping due to mojang is weird
                    if (name.contains("com.mojang.blaze3d.systems.TimerQuery")) continue;
                    scannedClasses.add(Class.forName(name));
                } catch (Throwable ignore) {
                }
            }
        }
    }

    public Set<Class<?>> getScannedClasses() {
        return scannedClasses;
    }
}
