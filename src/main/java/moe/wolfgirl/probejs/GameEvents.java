package moe.wolfgirl.probejs;

import com.mojang.brigadier.Command;
import dev.latvian.mods.kubejs.KubeJS;
import moe.wolfgirl.probejs.lang.linter.Linter;
import moe.wolfgirl.probejs.utils.GameUtils;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;


import java.util.function.Consumer;

@EventBusSubscriber(value = Dist.CLIENT)
public class GameEvents {
    private static final int MOD_LIMIT = 200;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void playerJoined(ClientPlayerNetworkEvent.LoggingIn event) {
        var player = event.getPlayer();
        ProbeConfig config = ProbeConfig.INSTANCE;

        if (config.enabled.get()) {
            if (config.modHash.get() == -1) {
                player.sendSystemMessage(Component.translatable("probejs.hello").kjs$gold());
                if (ModList.get().size() >= MOD_LIMIT) {
                    player.sendSystemMessage(
                            Component.translatable("probejs.performance", ModList.get().size())
                    );
                    config.classScanning.set(false);
                }
            }
            if (config.registryHash.get() != GameUtils.registryHash()) {
                if (!ProbeDumpingThread.exists()) { // Not very possible but anyway
                    ProbeDumpingThread.create(player::sendSystemMessage).start();
                }
            } else {
                player.sendSystemMessage(
                        Component.translatable("probejs.enabled_warning")
                                .append(Component.literal("/probejs disable")
                                        .kjs$clickSuggestCommand("/probejs disable")
                                        .kjs$aqua()
                                ));

            }
            player.sendSystemMessage(
                    Component.translatable("probejs.wiki")
                            .append(Component.literal("Wiki Page")
                                    .kjs$aqua()
                                    .kjs$underlined()
                                    .kjs$clickOpenUrl("https://kubejs.com/wiki/addons/third-party/probejs")
                                    .kjs$hover(Component.literal("https://kubejs.com/wiki/addons/third-party/probejs")))
            );
        }
    }

    @SubscribeEvent
    public static void registerCommand(RegisterClientCommandsEvent event) {
        var dispatcher = event.getDispatcher();
        dispatcher.register(
                Commands.literal("probejs")
                        .then(Commands.literal("dump")
                                .requires(source -> ProbeConfig.INSTANCE.enabled.get() && source.hasPermission(2))
                                .executes(context -> {
                                    Consumer<Component> messageSender = component -> context.getSource().sendSystemMessage(component);
                                    if (ProbeDumpingThread.exists()) {
                                        messageSender.accept(Component.translatable("probejs.already_running"));
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    KubeJS.PROXY.reloadClientInternal();
                                    ProbeDumpingThread.create(messageSender).start();
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .then(Commands.literal("disable")
                                .requires(source -> ProbeConfig.INSTANCE.enabled.get() && source.hasPermission(2))
                                .executes(context -> {
                                    ProbeConfig.INSTANCE.enabled.set(false);
                                    context.getSource().sendSystemMessage(Component.translatable("probejs.bye_bye").kjs$gold());
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .then(Commands.literal("enable")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                    ProbeConfig.INSTANCE.enabled.set(true);
                                    context.getSource().sendSystemMessage(Component.translatable("probejs.hello_again").kjs$aqua());
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .then(Commands.literal("scope_isolation")
                                .requires(source -> ProbeConfig.INSTANCE.enabled.get() && source.hasPermission(2))
                                .executes(context -> {
                                    boolean flag = !ProbeConfig.INSTANCE.isolatedScopes.get();
                                    ProbeConfig.INSTANCE.isolatedScopes.set(flag);
                                    context.getSource().sendSystemMessage(flag ?
                                            Component.translatable("probejs.isolation").kjs$aqua() :
                                            Component.translatable("probejs.no_isolation").kjs$aqua());
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .then(Commands.literal("lint")
                                .requires(source -> ProbeConfig.INSTANCE.enabled.get() && source.hasPermission(2))
                                .executes(context -> {
                                    Linter.defaultLint(context.getSource()::sendSystemMessage);
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .then(Commands.literal("complete_dump")
                                .requires(source -> ProbeConfig.INSTANCE.enabled.get() && source.hasPermission(2))
                                .executes(context -> {
                                    boolean flag = !ProbeConfig.INSTANCE.complete.get();
                                    ProbeConfig.INSTANCE.complete.set(flag);
                                    context.getSource().sendSystemMessage(flag ?
                                            Component.translatable("probejs.complete") :
                                            Component.translatable("probejs.no_complete"));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .then(Commands.literal("decompile")
                                .requires(source -> ProbeConfig.INSTANCE.enabled.get() && source.hasPermission(2))
                                .executes(context -> {
                                    boolean flag = !ProbeConfig.INSTANCE.enableDecompiler.get();
                                    ProbeConfig.INSTANCE.enableDecompiler.set(flag);
                                    context.getSource().sendSystemMessage(flag ?
                                            Component.translatable("probejs.decompile") :
                                            Component.translatable("probejs.no_decompile"));
                                    if (flag) ProbeConfig.INSTANCE.modHash.set(-2L);
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
        );
    }

    @SubscribeEvent
    public static void rightClickedBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getSide() == LogicalSide.SERVER) GlobalStates.LAST_RIGHTCLICKED = event.getPos();
    }

    @SubscribeEvent
    public static void rightClickedEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getSide() == LogicalSide.SERVER) GlobalStates.LAST_ENTITY = event.getTarget();
    }

    @SubscribeEvent
    public static void changedDimension(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof Player player && !(player instanceof FakePlayer)) {
            GlobalStates.LAST_RIGHTCLICKED = null;
            GlobalStates.LAST_ENTITY = null;
        }
    }
}

