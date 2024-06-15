package moe.wolfgirl.probejs;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import moe.wolfgirl.probejs.utils.GameUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

public class GameEvents {
    public static void playerJoined(ServerPlayer player) {
        ProbeConfig config = ProbeConfig.INSTANCE;
        try {
            if (config.enabled.get()) {
                if (config.aggressive.get() || config.modHash.get() == -1) {
                    if (config.modHash.get() == -1) {
                        player.sendSystemMessage(Component.translatable("probejs.hello").kjs$gold());
                    }
                    if (config.registryHash.get() != GameUtils.registryHash()) {
                        ProbeDump dump = new ProbeDump();
                        dump.defaultScripts();
                        dump.trigger(player::sendSystemMessage);
                    }
                } else {
                    player.sendSystemMessage(
                            Component.translatable("probejs.enabled_warning")
                                    .append(Component.literal("/probejs disable")
                                            .kjs$clickSuggestCommand("/probejs disable")
                                            .kjs$aqua()
                                    ));
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("probejs")
                        .then(Commands.literal("dump")
                                .requires(source -> ProbeConfig.INSTANCE.enabled.get() &&
                                        source.getServer().isSingleplayer() &&
                                        source.hasPermission(2))
                                .executes(context -> {
                                    context.getSource().getServer().kjs$runCommandSilent("kubejs reload client_scripts");
                                    context.getSource().getServer().kjs$runCommandSilent("reload");
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
                                .requires(source -> ProbeConfig.INSTANCE.enabled.get() &&
                                        source.getServer().isSingleplayer() &&
                                        source.hasPermission(2))
                                .executes(context -> {
                                    ProbeConfig.INSTANCE.enabled.set(false);
                                    context.getSource().sendSystemMessage(Component.translatable("probejs.bye_bye").kjs$gold());
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .then(Commands.literal("enable")
                                .executes(context -> {
                                    ProbeConfig.INSTANCE.enabled.set(true);
                                    context.getSource().sendSystemMessage(Component.translatable("probejs.hello_again").kjs$aqua());
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
        );
    }
}

