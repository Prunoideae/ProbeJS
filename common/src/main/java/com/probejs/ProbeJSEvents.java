package com.probejs;

import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.codec.digest.DigestUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;

public class ProbeJSEvents {
    private static void computeHash(Mod mod, MessageDigest digest) {
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
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                for (Mod mod : Platform.getMods().stream().sorted(Comparator.comparing(Mod::getModId)).toList()) {
                    computeHash(mod, digest);
                }
                String hash = byte2Hex(digest.digest());
                if (!hash.equals(ProbeConfig.INSTANCE.modHash)) {
                    ProbeConfig.INSTANCE.modHash = hash;
                    ProbeConfig.INSTANCE.save();
                    player.sendSystemMessage(Component.literal("Mod list has changed, dumping new docs..."));
                    ProbeCommands.triggerDump(player);
                }
            } catch (NoSuchAlgorithmException ignored) {

            }

        }
    }
}
