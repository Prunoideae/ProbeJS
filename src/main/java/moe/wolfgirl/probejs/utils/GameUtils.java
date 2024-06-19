package moe.wolfgirl.probejs.utils;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.neoforged.neoforgespi.language.IModInfo;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Collectors;

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
}
