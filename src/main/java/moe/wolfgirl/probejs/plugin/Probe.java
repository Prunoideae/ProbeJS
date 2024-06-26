package moe.wolfgirl.probejs.plugin;

import dev.latvian.mods.kubejs.level.BlockContainerJS;
import dev.latvian.mods.kubejs.script.KubeJSContext;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.Context;
import moe.wolfgirl.probejs.GlobalStates;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@Info("Debugging utility for easier check on players, blocks, items, etc.")
public class Probe {
    public static final Probe INSTANCE = new Probe();

    private void testSourceFile(Context context) {
        var linep = new int[]{0};
        String source = Context.getSourcePositionFromStack(context, linep);

        if (!source.contains("test")) {
            throw new RuntimeException("This function is only available in a file containing \"test\", or files under a \"test\" folder.");
        }
    }

    public Player getCurrentPlayer(Context context) { //TODO: remove placeholder when the context bug is fixed
        KubeJSContext kContext = (KubeJSContext) context;
        testSourceFile(context);
        if (kContext.getType() == ScriptType.CLIENT) {
            return Minecraft.getInstance().player;
        } else {
            MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
            if (currentServer == null || !currentServer.isSingleplayer()) return null;
            return currentServer.getPlayerList()
                    .getPlayers()
                    .getFirst();
        }
    }

    public BlockContainerJS getLastRightClickedBlock(Context context) {
        testSourceFile(context);
        if (GlobalStates.LAST_RIGHTCLICKED == null) return null;
        Player currentPlayer = getCurrentPlayer(context);
        if (currentPlayer == null) return null;
        return currentPlayer.level().kjs$getBlock(GlobalStates.LAST_RIGHTCLICKED);
    }

    public Entity getLastRightClickedEntity(Context context) {
        testSourceFile(context);
        if (GlobalStates.LAST_ENTITY == null) return null;
        return GlobalStates.LAST_ENTITY;
    }
}
