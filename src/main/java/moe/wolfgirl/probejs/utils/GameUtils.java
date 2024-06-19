package moe.wolfgirl.probejs.utils;

import dev.latvian.mods.kubejs.registry.RegistryInfo;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;

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
