package moe.wolfgirl.probejs;

import com.mojang.brigadier.Command;
import moe.wolfgirl.probejs.gui.DumpScreen;
import moe.wolfgirl.probejs.misc.Require;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class GameEvents {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void playerJoined(ClientPlayerNetworkEvent.LoggingIn event) {
        var player = event.getPlayer();
        if (Minecraft.getInstance().isLocalServer()) {
            player.sendSystemMessage(Component.literal("Welcome to ProbeJS! To open the dump screen, use the command ").kjs$gold()
                    .append(Component.literal("/probejs")
                            .kjs$blue()
                            .kjs$underlined()
                            .kjs$hover(Component.literal("Click to run /probejs"))
                            .kjs$clickRunCommand("/probejs"))
            );

            if (Require.usedRequire && !Require.usageReported) {
                player.sendSystemMessage(Component.literal("require() is used in the script, remember to change to Java.loadClass() before releasing!").kjs$darkRed());
                player.sendSystemMessage(Component.literal("You can use the Convert button in the dump screen to automatically convert require() to Java.loadClass()").kjs$gold());
                Require.usageReported = true;
            }
        }
    }

    @SubscribeEvent
    public static void playerTicking(PlayerTickEvent.Post event) {
        // injecting require would cause too many checks (and we don't get player instance easily)
        var player = event.getEntity();
        if (!Minecraft.getInstance().isLocalServer() || player.tickCount < 40) return;
        if (Require.usedRequire && !Require.usageReported) {

            player.sendSystemMessage(Component.literal("require() is used in the script, remember to change to Java.loadClass() before releasing!").kjs$darkRed());
            player.sendSystemMessage(Component.literal("You can use the Convert button in the dump screen to automatically convert require() to Java.loadClass()").kjs$gold());
            Require.usageReported = true;
        }
    }

    @SubscribeEvent
    public static void registerCommand(RegisterClientCommandsEvent event) {
        var dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("probejs")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    DumpScreen.open();
                    return Command.SINGLE_SUCCESS;
                })
        );
    }

    @SubscribeEvent
    public static void rightClickedBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getSide() == LogicalSide.SERVER) GameStates.LAST_RIGHTCLICKED = event.getPos();
    }

    @SubscribeEvent
    public static void rightClickedEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getSide() == LogicalSide.SERVER) GameStates.LAST_ENTITY = event.getTarget();
    }

    @SubscribeEvent
    public static void changedDimension(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof Player player && !(player instanceof FakePlayer)) {
            GameStates.LAST_RIGHTCLICKED = null;
            GameStates.LAST_ENTITY = null;
        }
    }
}

