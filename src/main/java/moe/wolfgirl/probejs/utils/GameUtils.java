package moe.wolfgirl.probejs.utils;

import dev.latvian.mods.kubejs.server.ServerScriptManager;
import moe.wolfgirl.probejs.ProbeJS;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.ModList;
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

    public static void logException(Throwable t) {
        ProbeJS.LOGGER.error(t);
        for (StackTraceElement stackTraceElement : t.getStackTrace()) {
            ProbeJS.LOGGER.error(stackTraceElement.toString());
        }
    }
}
