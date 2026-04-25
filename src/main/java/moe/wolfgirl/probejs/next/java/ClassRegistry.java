package moe.wolfgirl.probejs.next.java;

import com.mojang.blaze3d.systems.RenderSystem;
import moe.wolfgirl.probejs.ProbeConfig;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.java.members.ClassInfo;
import moe.wolfgirl.probejs.utils.GameUtils;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class ClassRegistry {
    public static final ClassRegistry INSTANCE = new ClassRegistry();

    private final Set<String> deniedClasses = new HashSet<>();
    private final Map<ClassPath, ClassRecord> classMap = new HashMap<>();

    public record ClassRecord(ClassInfo info, int recursionDepth) {
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
            classMap.put(classPath, new ClassRecord(ClassInfo.resolve(clazz), recursionDepth));
        }
    }
}
