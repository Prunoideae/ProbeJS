package moe.wolfgirl.probejs.utils;

import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import net.minecraft.resources.ResourceLocation;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class GameUtils {
    public static long modHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (Mod mod : Platform.getMods()) {
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
            RegistryInfo.MAP.values()
                    .stream()
                    .flatMap(info -> info.objects.keySet()
                            .stream()
                            .map(ResourceLocation::toString)
                            .map(s -> info.key.location() + "/" + s)
                    )
                    .sorted()
                    .forEach(key -> digest.update(key.getBytes()));
            ByteBuffer buffer = ByteBuffer.wrap(digest.digest());
            return buffer.getLong();
        } catch (NoSuchAlgorithmException e) {
            return -1;
        }

    }
}
