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
            if (!ProbeConfig.INSTANCE.shouldProbingAggressive()) return;
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                for (Mod mod : Platform.getMods().stream().sorted(Comparator.comparing(Mod::getModId)).toList()) {
                    computeModHash(mod, digest);
                }
                computeKubeJSObjectHash(digest);
                String hash = byte2Hex(digest.digest());
                if (!hash.equals(ProbeConfig.getModHash())) {
                    ProbeConfig.writeModHash(hash);
                    ProbeConfig.INSTANCE.modChanged = true;
                    player.sendSystemMessage(Component.literal("Mod list has changed, dumping new docs..."));
                    ProbeCommands.triggerDump(player);
                }
            } catch (NoSuchAlgorithmException ignored) {

            }
            if (ProbeConfig.INSTANCE.firstLoad) {
                player.sendSystemMessage(Component.literal("This is the first time you are running ProbeJS. An automatic dump will be triggered."));
                player.sendSystemMessage(Component.literal("The aggressive mode will be turned off, you can still turn it on by ")
                        .append(Component.literal("/probejs configure toggle_aggressive").kjs$underlined().kjs$green().kjs$clickSuggestCommand("/probejs configure toggle_aggressive"))
                        .append(Component.literal(" later.").kjs$white()));
                ProbeConfig.INSTANCE.noAggressiveProbing = true;
                ProbeConfig.INSTANCE.firstLoad = false;
                if (Platform.getMods().size() > ProbeConfig.MOD_COUNT) {
                    player.sendSystemMessage(Component.literal("There are more than " + ProbeConfig.MOD_COUNT + " mods installed. Disabling some features to prevent lag..."));
                    ProbeConfig.INSTANCE.disableRecipeJsonDump = true;
                    player.sendSystemMessage(Component.literal("Recipe JSON dumps are disabled."));
                }
                ProbeConfig.INSTANCE.save();
            } else {
                player.sendSystemMessage(Component.literal("Aggressive probing is on. Remember to disable it in release!").kjs$red().kjs$underlined());
                player.sendSystemMessage(Component.literal("Use ")
                        .append(Component.literal("/probejs configure toggle_aggressive").kjs$underlined().kjs$green().kjs$clickSuggestCommand("/probejs configure toggle_aggressive"))
                        .append(Component.literal(" to disable.").kjs$white())
                );
            }
        }
    }
}
