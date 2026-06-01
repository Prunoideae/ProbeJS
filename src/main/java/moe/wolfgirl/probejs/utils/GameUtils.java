package moe.wolfgirl.probejs.utils;

import dev.latvian.mods.kubejs.script.KubeJSContext;
import dev.latvian.mods.kubejs.server.ServerScriptManager;
import moe.wolfgirl.probejs.ProbeJS;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GameUtils {

    @Nullable
    public static ServerScriptManager getServerScriptManager() {
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer == null) return null;
        return currentServer.getServerResources().managers().kjs$getServerScriptManager();
    }

    public static MinecraftServer getCurrentServer() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server instanceof IntegratedServer ? server : null;
    }

    public static KubeJSContext getCurrentContext(){
        ServerScriptManager manager = getServerScriptManager();
        if (manager == null) return null;
        return (KubeJSContext) manager.contextFactory.enter();
    }

    public static void logException(Throwable t) {
        ProbeJS.LOGGER.error(t);
        for (StackTraceElement stackTraceElement : t.getStackTrace()) {
            ProbeJS.LOGGER.error(stackTraceElement.toString());
        }
    }

    public static Collection<ResourceKey<? extends Registry<?>>> getRegistries(RegistryAccess access) {
        return access.registries()
                .map(RegistryAccess.RegistryEntry::key)
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    public static <T> ResourceKey<Registry<T>> castKey(ResourceKey<?> key) {
        return (ResourceKey<Registry<T>>) key;
    }

    public static List<String> readData(String resourcePath, Map<String, String> formatter) throws IOException {
        var modFile = ProbeJS.MOD_CONTAINER.getModInfo().getOwningFile().getFile();
        var resourceFile = modFile.findResource(resourcePath);
        if (!Files.exists(resourceFile)) throw new FileNotFoundException("Resource not found: " + resourcePath);

        return Files.readAllLines(resourceFile).stream().map(line -> {
            for (var entry : formatter.entrySet()) {
                line = line.replace("$%s$".formatted(entry.getKey()), entry.getValue());
            }
            return line;
        }).toList();
    }
}
