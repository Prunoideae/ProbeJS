package moe.wolfgirl.probejs;

import com.mojang.brigadier.Command;
import dev.latvian.mods.kubejs.KubeJS;
import moe.wolfgirl.probejs.features.bridge.ProbeServer;
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


import java.net.UnknownHostException;
import java.util.function.Consumer;

@EventBusSubscriber(value = Dist.CLIENT)
public class GameEvents {
    private static final int MOD_LIMIT = 350;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void playerJoined(ClientPlayerNetworkEvent.LoggingIn event) {
        var player = event.getPlayer();
        ProbeConfig config = ProbeConfig.INSTANCE;

        if (config.enabled.get()) {
            if (config.modHash.get() == -1) {
                player.sendSystemMessage(Component.translatable("probejs.hello").kjs$gold());
            }
            if (config.registryHash.get() != GameUtils.registryHash()) {
                (new Thread(() -> {  // Don't stall the client
                    ProbeDump dump = new ProbeDump();
                    dump.defaultScripts();
                    try {
                        dump.trigger(player::sendSystemMessage);
                        Linter.defaultLint(player::sendSystemMessage);
                    } catch (Throwable e) {
                        ProbeJS.LOGGER.error(e.getMessage());
                        throw new RuntimeException(e);
                    }
                })).start();
            } else {
                player.sendSystemMessage(
                        Component.translatable("probejs.enabled_warning")
                                .append(Component.literal("/probejs disable")
                                        .kjs$clickSuggestCommand("/probejs disable")
                                        .kjs$aqua()
                                ));
                if (ModList.get().size() >= MOD_LIMIT && ProbeConfig.INSTANCE.complete.get()) {
                    player.sendSystemMessage(
                            Component.translatable("probejs.performance", ModList.get().size())
                    );
                }

                Linter.defaultLint(player::sendSystemMessage);
            }
            player.sendSystemMessage(
                    Component.translatable("probejs.wiki")
                            .append(Component.literal("Wiki Page")
                                    .kjs$aqua()
                                    .kjs$underlined()
                                    .kjs$clickOpenUrl("https://kubejs.com/wiki/addons/third-party/probejs")
                                    .kjs$hover(Component.literal("https://kubejs.com/wiki/addons/third-party/probejs")))
            );

            if (config.interactive.get() && GlobalStates.SERVER == null) {
                try {
                    GlobalStates.SERVER = new ProbeServer(config.interactivePort.get());
                    GlobalStates.SERVER.start();
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
                player.sendSystemMessage(
                        Component.translatable("probejs.interactive", config.interactivePort.get())
                );
            }
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
                                    KubeJS.PROXY.reloadClientInternal();
                                    ProbeDump dump = new ProbeDump();
                                    dump.defaultScripts();
                                    (new Thread(() -> {
                                        try {
                                            Consumer<Component> reportProgress = component -> context.getSource().sendSystemMessage(component);
                                            dump.trigger(reportProgress);
                                        } catch (Throwable e) {
                                            ProbeJS.LOGGER.error(e.getMessage());
                                            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                                                ProbeJS.LOGGER.error(stackTraceElement.toString());
                                            }
                                            throw new RuntimeException(e);
                                        }
                                    })).start();
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

