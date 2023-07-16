package com.probejs;

import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;

public class ProbeJSEvents {

    private static void computeKubeJSObjectHash(MessageDigest digest) {
        RegistryInfo.MAP
                .values()
                .stream()
                .flatMap(info -> info.objects.keySet()
                        .stream()
                        .map(ResourceLocation::toString)
                        .map(s -> info.key.location() + "/" + s)
                )
                .sorted()
                .forEach(key -> digest.update(key.getBytes()));
    }

    private static void computeModHash(Mod mod, MessageDigest digest) {
        String idVersion = mod.getModId() + mod.getVersion();
        digest.update(idVersion.getBytes());
    }

    private static String byte2Hex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static void playerJoined(ServerPlayer player) {
        if (player.server.isSingleplayer() && player.hasPermissions(2)) {
            if (!ProbeConfig.INSTANCE.enabled) return;
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                for (Mod mod : Platform.getMods().stream().sorted(Comparator.comparing(Mod::getModId)).toList()) {
                    computeModHash(mod, digest);
                }
                computeKubeJSObjectHash(digest);
                String hash = byte2Hex(digest.digest());
                if (!hash.equals(ProbeConfig.INSTANCE.modHash)) {
                    ProbeConfig.INSTANCE.modHash = hash;
                    ProbeConfig.INSTANCE.save();
                    player.sendSystemMessage(Component.literal("Mod list has changed, dumping new docs..."));
                    ProbeCommands.triggerDump(player, true);
                }
            } catch (NoSuchAlgorithmException ignored) {

            }

        }
    }
}
