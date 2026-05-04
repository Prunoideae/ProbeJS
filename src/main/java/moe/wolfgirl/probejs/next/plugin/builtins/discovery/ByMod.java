package moe.wolfgirl.probejs.next.plugin.builtins.discovery;

import moe.wolfgirl.probejs.ProbeConfig;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.next.java.ClassRegistry;
import moe.wolfgirl.probejs.next.plugin.ProbeJSPlugin;
import net.neoforged.fml.ModList;

import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;

public class ByMod extends ProbeJSPlugin {
    @Override
    public Set<Class<?>> provideClassForDiscovery() {
        Set<Class<?>> classes = new HashSet<>();
        for (String modId : ProbeConfig.INSTANCE.fullScanMods.get()) {
            try {
                classes.addAll(getClassesFromMod(modId));
            } catch (Exception e) {
                ProbeJS.LOGGER.error("Error while scanning mod %s: %s".formatted(modId, e.getMessage()));
            }
        }
        return classes;
    }

    private Set<Class<?>> getClassesFromMod(String modId) throws Exception {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        var modInfo = ModList.get().getModFileById(modId);
        if (modInfo == null) return Set.of();
        var modFile = modInfo.getFile().getFilePath().toFile();
        Set<Class<?>> classes = new HashSet<>();
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
                    if (ClassRegistry.shouldSkipClass(name)) continue;
                    var clazz = Class.forName(name, false, loader);
                    if (clazz.isAnonymousClass()) continue;
                    classes.add(clazz);
                } catch (Throwable ignore) { // Prevent over-propagating that halts the whole scanning
                    ProbeJS.LOGGER.error("Error while loading class %s, don't worry, it's just one class.".formatted(name));
                }
            }
        }

        return classes;
    }
}
