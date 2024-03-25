package com.probejs;

import com.probejs.features.repl.EvalManager;
import com.probejs.features.server.Server;
import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;
import dev.latvian.mods.kubejs.KubeJSPaths;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;

public class ProbeJSEvents {
    public static Server SERVER;
    public static MinecraftServer CURRENT_SERVER;

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

    public static void worldCleanup(MinecraftServer server) {
        if (SERVER != null) {
            try {
                SERVER.stop();
                EvalManager.SERVER_SCRIPTS.reset();
                SERVER = null;
                CURRENT_SERVER = null;
            } catch (InterruptedException ignored) {
            }
        }
    }

    public static void playerJoined(ServerPlayer player) {
        if (player.server.isSingleplayer() && player.hasPermissions(2)) {

            if (ProbeConfig.INSTANCE.enabled && ProbeConfig.INSTANCE.interactive == 1) {
                // So it's safe to open up the port
                // Interactive mode is 0 by default, it will only be set once the ProbeJS extension does so.
                try {
                    SERVER = new Server(ProbeConfig.INSTANCE.interactivePort);
                    SERVER.start();
                    player.sendSystemMessage(Component.literal("ProbeJS Websocket Server started."));
                } catch (Exception e) {
                    player.sendSystemMessage(Component.literal("Failed to start WebSocket server, probably the port is in use."));
                }
                CURRENT_SERVER = player.server;
            }

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
                player.sendSystemMessage(Component.literal("ProbeJS now supports downloading of recipe schema scripts. These scripts are for adding recipe support for mods that don't have recipe support yet."));
                player.sendSystemMessage(Component.literal("To configure ProbeJS, open ")
                        .append(Component.literal("this file").kjs$underlined().kjs$aqua().kjs$clickOpenFile(KubeJSPaths.CONFIG.resolve("probejs.json").toString()))
                        .append(Component.literal("."))
                );
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
