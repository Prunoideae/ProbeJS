package moe.wolfgirl.probejs;

import com.mojang.brigadier.Command;
import moe.wolfgirl.probejs.gui.DumpScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
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

@EventBusSubscriber(value = Dist.CLIENT)
public class GameEvents {
    private static final int MOD_LIMIT = 200;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void playerJoined(ClientPlayerNetworkEvent.LoggingIn event) {
        var player = event.getPlayer();
        if (Minecraft.getInstance().isLocalServer()) {
            player.sendSystemMessage(
                    Component.translatable("probejs.wiki")
                            .append(Component.literal("Wiki Page")
                                    .kjs$aqua()
                                    .kjs$underlined()
                                    .kjs$clickOpenUrl("https://kubejs.com/wiki/addons/third-party/probejs")
                                    .kjs$hover(Component.literal("https://kubejs.com/wiki/addons/third-party/probejs")))
            );

            // Reload creative mode tabs
            var params = new CreativeModeTab.ItemDisplayParameters(
                    player.connection.enabledFeatures(),
                    player.canUseGameMasterBlocks() && Minecraft.getInstance().options.operatorItemsTab().get(),
                    player.level().registryAccess()
            );

            CreativeModeTabs.tabs().stream()
                    .filter(t -> t.getType() != CreativeModeTab.Type.CATEGORY && t.getType() != CreativeModeTab.Type.SEARCH)
                    .forEach(t -> t.buildContents(params));
        }

        // CreativeModeTabs.CACHED_PARAMETERS = null;
        // CreativeModeTabs.tryRebuildTabContents(
        //     player.connection.enabledFeatures(),
        //     player.canUseGameMasterBlocks() && Minecraft.getInstance().options.operatorItemsTab().get(),
        //     player.level().registryAccess()
        //);
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

