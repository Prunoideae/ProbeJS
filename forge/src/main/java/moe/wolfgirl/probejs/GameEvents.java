package moe.wolfgirl.probejs;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import moe.wolfgirl.probejs.linter.Linter;
import moe.wolfgirl.probejs.linter.LintingWarning;
import moe.wolfgirl.probejs.utils.GameUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mod.EventBusSubscriber
public class GameEvents {

    @SubscribeEvent
    public static void playerJoined(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
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
                    player.sendSystemMessage(
                            Component.translatable("probejs.wiki")
                                    .append(Component.literal("Github Page")
                                            .kjs$aqua()
                                            .kjs$underlined()
                                            .kjs$clickOpenUrl("https://kubejs.com/wiki/addons/third-party/probejs")
                                            .kjs$hover(Component.literal("https://kubejs.com/wiki/addons/third-party/probejs")))
                    );

                    // checks for stuffs
                    player.server.kjs$runCommandSilent("probejs lint");
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();
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
                        .then(Commands.literal("scope_isolation")
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
                                .requires(source -> ProbeConfig.INSTANCE.enabled.get() &&
                                        source.getServer().isSingleplayer() &&
                                        source.hasPermission(2))
                                .executes(context -> {
                                    try {
                                        List<Component> warnings = new ArrayList<>();

                                        var startup = Linter.STARTUP_SCRIPT.get();
                                        for (LintingWarning lintingWarning : startup.lint()) {
                                            warnings.add(lintingWarning.defaultFormatting(startup.scriptPath));
                                        }

                                        var server = Linter.SERVER_SCRIPT.get();
                                        for (LintingWarning lintingWarning : server.lint()) {
                                            warnings.add(lintingWarning.defaultFormatting(server.scriptPath));
                                        }
                                        var client = Linter.CLIENT_SCRIPT.get();
                                        for (LintingWarning lintingWarning : client.lint()) {
                                            warnings.add(lintingWarning.defaultFormatting(client.scriptPath));
                                        }

                                        for (Component warning : warnings) {
                                            context.getSource().sendSystemMessage(warning);
                                        }
                                    } catch (Throwable e) {
                                        ProbeJS.LOGGER.error(e.getMessage());
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
        );
    }
}

