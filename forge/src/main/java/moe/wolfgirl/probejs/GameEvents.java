package moe.wolfgirl.probejs;

import com.mojang.brigadier.Command;
import dev.latvian.mods.kubejs.KubeJS;
import moe.wolfgirl.probejs.lang.linter.Linter;
import moe.wolfgirl.probejs.utils.GameUtils;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Consumer;

@Mod.EventBusSubscriber
public class GameEvents {

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
                Linter.defaultLint(player::sendSystemMessage);
            }
            player.sendSystemMessage(
                    Component.translatable("probejs.wiki")
                            .append(Component.literal("Github Page")
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
                                    KubeJS.PROXY.reloadClientInternal();
                                    ProbeDump dump = new ProbeDump();
                                    dump.defaultScripts();
                                    try {
                                        Consumer<Component> reportProgress = component -> context.getSource().sendSystemMessage(component);
                                        dump.trigger(reportProgress);
                                    } catch (Throwable e) {
                                        throw new RuntimeException(e);
                                    }
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
                                .requires(source -> source.hasPermission(2))
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
        );
    }
}

