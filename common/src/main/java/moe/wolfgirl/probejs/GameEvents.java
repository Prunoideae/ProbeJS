package moe.wolfgirl.probejs;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import moe.wolfgirl.probejs.utils.GameUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class GameEvents {
    public static void playerJoined(ServerPlayer player) {
        ProbeConfig config = ProbeConfig.INSTANCE;
        try {
            if (config.enabled.get() && config.aggressive.get()) {
                if (config.registryHash.get() != GameUtils.registryHash()) {
                    ProbeDump dump = new ProbeDump();
                    dump.defaultScripts();
                    dump.trigger();
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
                                    ProbeDump dump = new ProbeDump();
                                    dump.defaultScripts();
                                    try {
                                        dump.trigger();
                                    } catch (Throwable e) {
                                        throw new RuntimeException(e);
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
        );
    }
}

