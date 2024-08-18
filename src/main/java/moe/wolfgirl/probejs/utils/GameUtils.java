package moe.wolfgirl.probejs.utils;

import dev.latvian.mods.kubejs.server.ServerScriptManager;
import moe.wolfgirl.probejs.ProbeJS;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.neoforged.neoforgespi.language.IModInfo;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class GameUtils {
    public static long modHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (IModInfo mod : ModList.get().getMods()) {
                digest.update((mod.getModId() + mod.getVersion()).getBytes());
            }
            ByteBuffer buffer = ByteBuffer.wrap(digest.digest());
            return buffer.getLong();
        } catch (NoSuchAlgorithmException e) {
            return -1;
        }
    }

    public static long registryHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) return -1;
            RegistryAccess access = server.registryAccess();

            for (ResourceKey<? extends Registry<?>> key : BuiltInRegistries.REGISTRY
                    .registryKeySet()
                    .stream()
                    .sorted()
                    .toList()) {
                Registry<?> registry = access.registry(key).orElse(null);
                if (registry == null) continue;
                registry.keySet()
                        .stream()
                        .map(ResourceLocation::toString)
                        .sorted()
                        .forEach(s -> digest.update(s.getBytes()));
            }
            ByteBuffer buffer = ByteBuffer.wrap(digest.digest());
            return buffer.getLong();
        } catch (NoSuchAlgorithmException e) {
            return -1;
        }
    }

    @Nullable
    public static ServerScriptManager getServerScriptManager() {
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer == null) return null;
        return currentServer.getServerResources().managers().kjs$getServerScriptManager();
    }

    public static void logException(Throwable t) {
        ProbeJS.LOGGER.error(t);
        for (StackTraceElement stackTraceElement : t.getStackTrace()) {
            ProbeJS.LOGGER.error(stackTraceElement.toString());
        }
    }
}
