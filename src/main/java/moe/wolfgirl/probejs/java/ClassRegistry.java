package moe.wolfgirl.probejs.java;

import com.mojang.blaze3d.systems.RenderSystem;
import moe.wolfgirl.probejs.GameStates;
import moe.wolfgirl.probejs.ProbeConfig;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.typescript.ClassPath;
import moe.wolfgirl.probejs.java.members.ClassInfo;
import moe.wolfgirl.probejs.utils.GameUtils;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

public class ClassRegistry {
    public static final ClassRegistry INSTANCE = new ClassRegistry();

    private final Set<String> deniedClasses = new HashSet<>();
    private final Map<ClassPath, ClassRecord> classMap = new HashMap<>();

    public record ClassRecord(ClassInfo info, int recursionDepth) {
        public void writeTo(BufferedWriter writer) throws Exception {
            writer.write("%s\t%d\n".formatted(info.classPath(), recursionDepth));
        }

        static List<ClassRecord> readFrom(BufferedReader reader) throws Exception {
            List<ClassRecord> records = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length != 2) continue;
                ClassPath classPath = new ClassPath(parts[0]);
                int depth = Integer.parseInt(parts[1]);
                records.add(new ClassRecord(ClassInfo.resolve(classPath.loadClass()), depth));
            }
            return records;
        }
    }

    public Map<ClassPath, ClassInfo> getAllClasses() {
        Map<ClassPath, ClassInfo> map = new HashMap<>();
        for (var entry : classMap.entrySet()) {
            map.put(entry.getKey(), entry.getValue().info);
        }
        return map;
    }

    public PackageTree resolveTree() {
        PackageTree tree = new PackageTree();
        for (var entry : classMap.entrySet()) {
            tree.addClassPath(entry.getKey());
        }
        return tree;
    }

    @Nullable
    public ClassInfo getClassInfo(ClassPath classPath) {
        var record = classMap.get(classPath);
        if (record == null) return null;
        return record.info;
    }


    public void discover() {
        CountDownLatch done = new CountDownLatch(1);
        int maxRecursionDepth = ProbeConfig.INSTANCE.recursionDepth.get();

        GameStates.DUMP_STATE.setStatus(Component.literal("Finding more classes..."));
        try {
            Thread.sleep(100); // Just to make sure the status is updated before we start the discovery
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        RenderSystem.recordRenderCall(() -> {
            // We run the discovery in the render thread to make sure we have access to all classes
            var loader = getClass().getClassLoader();
            Set<ClassRecord> records = new HashSet<>(classMap.values());
            while (true) {
                records.removeIf(record -> record.recursionDepth >= maxRecursionDepth);
                if (records.isEmpty()) break;
                Set<ClassRecord> newRecords = new HashSet<>();
                for (ClassRecord record : records) {
                    int nextDepth = record.recursionDepth + 1;
                    for (Class<?> c : record.info.getReferredClasses()) {
                        if (deniedClasses.contains(c.getName())) continue;
                        try {
                            if (c.isPrimitive()) continue;
                            ClassPath classPath = new ClassPath(c);
                            if (!classMap.containsKey(classPath)) {
                                Class.forName(c.getName(), false, loader);
                                ClassInfo classInfo = ClassInfo.resolve(c);
                                ClassRecord newRecord = new ClassRecord(classInfo, nextDepth);
                                classMap.put(classPath, newRecord);
                                newRecords.add(newRecord);
                            }
                        } catch (Throwable e) {
                            ProbeJS.LOGGER.error("Error occurred when resolving class %s".formatted(c));
                            GameUtils.logException(e);
                        }
                    }
                }
                records = newRecords;
            }
            done.countDown();
        });

        try {
            done.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void denyClasses(Set<String> classNames) {
        deniedClasses.addAll(classNames);
    }

    public void putClass(Class<?> clazz, int recursionDepth) {
        ClassPath classPath = new ClassPath(clazz);
        if (!classMap.containsKey(classPath)) {
            try {
                classMap.put(classPath, new ClassRecord(ClassInfo.resolve(clazz), recursionDepth));
            } catch (Throwable e) {
                ProbeJS.LOGGER.error("Error occurred when resolving class %s".formatted(clazz));
                GameUtils.logException(e);
            }
        }
    }

    private void reportClassesFound(int count) {
        GameStates.DUMP_STATE.setStatus(Component.literal("Found %d initial classes".formatted(count)));
    }

    public void fetchInitialClasses() {
        Set<Class<?>> allowedClasses = new HashSet<>();

        ProbeJSPlugin.forEachWithPriority("provideClassForDiscovery", plugin -> {
            allowedClasses.addAll(plugin.provideClassForDiscovery());
        });

        for (Class<?> allowedClass : allowedClasses) {
            putClass(allowedClass, 0);
            reportClassesFound(classMap.size());
        }

        // We fetch the plugins first, then use it in class scanning to avoid repetitive plugin instantiation
        List<ProbeJSPlugin> pluginCache = new ArrayList<>();
        ProbeJSPlugin.forEachWithPriority("allowClassInDiscovery", pluginCache::add);

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
                        boolean allowed = allowedClasses.contains(clazz);
                        if (!allowed) {
                            for (ProbeJSPlugin plugin : pluginCache) {
                                if (plugin.allowClassInDiscovery(clazz)) {
                                    allowed = true;
                                    break;
                                }
                            }
                        }
                        if (allowed) {
                            putClass(clazz, 0);
                            reportClassesFound(classMap.size());
                        }
                    } catch (Throwable ignore) {
                        ProbeJS.LOGGER.error("Error while loading class %s, don't worry, it's just one class.".formatted(name));
                    }
                }
            } catch (IOException ignore) {
            }
        }
    }

    public static boolean shouldSkipClass(String className) {
        if (className.contains("com.mojang.blaze3d.systems.TimerQuery")) return true;
        if (className.startsWith("architectury_inject")) return true; // Classes injected by arch
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

    private static List<File> findModFiles() {
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

    public void clear() {
        classMap.clear();
        deniedClasses.clear();
    }

    public void writeTo(Path storePath) {
        try (BufferedWriter writer = Files.newBufferedWriter(storePath)) {
            for (var record : classMap.values()) {
                record.writeTo(writer);
            }
        } catch (Exception e) {
            ProbeJS.LOGGER.error("Error occurred when writing class registry to file!");
            GameUtils.logException(e);
        }
    }

    public void readFrom(Path storePath) {
        if (!Files.exists(storePath)) return;
        try (BufferedReader reader = Files.newBufferedReader(storePath)) {
            List<ClassRecord> records = ClassRecord.readFrom(reader);
            for (ClassRecord record : records) {
                classMap.put(record.info.classPath(), record);
            }
        } catch (Exception e) {
            ProbeJS.LOGGER.error("Error occurred when reading class registry from file!");
            GameUtils.logException(e);
        }
    }
}
