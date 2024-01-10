package com.probejs;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.probejs.docs.DocCompiler;
import com.probejs.jdoc.jsgen.RemoteSchema;
import com.probejs.specials.SnippetCompiler;
import com.probejs.specials.SpecialCompiler;
import com.probejs.docs.formatter.ClassResolver;
import com.probejs.docs.formatter.NameResolver;
import com.probejs.jdoc.jsgen.DocGenerationEventJS;
import com.probejs.jdoc.jsgen.ProbeJSEvents;
import com.probejs.rich.fluid.RichFluidCompiler;
import com.probejs.rich.item.RichItemCompiler;
import com.probejs.rich.lang.RichLangCompiler;
import dev.architectury.platform.Platform;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ProbeCommands {
    public static ServerLevel COMMAND_LEVEL = null;
    public static Thread compileThread = null;
    public static Thread resolveRenderThread = null;

    @SuppressWarnings("unchecked")
    public static <T> Registry<T> getRegistry(ResourceKey<Registry<T>> registryKey) {
        var builtinRegistry = RegistryInfo.of(registryKey).getVanillaRegistry();
        if (builtinRegistry == null) {
            builtinRegistry = COMMAND_LEVEL.registryAccess().registry(registryKey).get();
        }
        return (Registry<T>) builtinRegistry;
    }

    public static void triggerRender(Consumer<String> sendMessage) {
        if (resolveRenderThread != null && resolveRenderThread.isAlive()) {
            sendMessage.accept("Skipping image rendering due to previous render thread still running.");
        } else if (resolveRenderThread != null) {
            sendMessage.accept("Previous render thread is dead! Please check out latest.log and submit an error report.");
            resolveRenderThread = null;
        }
        resolveRenderThread = new Thread(() -> {

            sendMessage.accept("Resolving things to render...");
            var items = RichItemCompiler.resolve();
            sendMessage.accept("Items resolved, will render " + items.size() + " items.");
            var fluids = RichFluidCompiler.resolve();
            sendMessage.accept("Fluids resolved, will render " + fluids.size() + " fluids.");
            Minecraft.getInstance().execute(() -> {
                try {
                    // RichItemCompiler.render(items);
                    // RichFluidCompiler.render(fluids);
                    sendMessage.accept("Images rendered.");
                } catch (Throwable e) {
                    sendMessage.accept("Error occurred while rendering images! Please check out latest.log and submit an error report.");
                    ProbeJS.LOGGER.error("Error:", e);
                }
            });
            resolveRenderThread = null;
        });

        resolveRenderThread.setUncaughtExceptionHandler((t, e) -> {
            sendMessage.accept("Error occurred while rendering images! Please check out latest.log and submit an error report.");
            ProbeJS.LOGGER.error("Error:", e);
            resolveRenderThread = null;
        });
        resolveRenderThread.setDaemon(true);
        resolveRenderThread.start();

    }

    public static void triggerDump(ServerPlayer player) {
        if (compileThread != null && compileThread.isAlive()) {
            player.sendSystemMessage(Component.literal("ProbeJS is running! Please wait for current dump to finish."), false);
            return;
        } else if (compileThread != null) {
            player.sendSystemMessage(Component.literal("ProbeJS dumping thread is dead! Please check out latest.log and submit an error report."), false);
            compileThread = null;
        }

        player.server.kjs$runCommandSilent("reload");
        player.server.kjs$runCommandSilent("kubejs dump_internals events");
        ProbeConfig.reload();
        COMMAND_LEVEL = (ServerLevel) player.level();
        Instant start = Instant.now();
        Consumer<String> sendMessage = s -> {
            Instant end = Instant.now();
            Duration duration = Duration.between(start, end);
            long sub = TimeUnit.MILLISECONDS.convert(duration.getNano(), TimeUnit.NANOSECONDS);
            player.sendSystemMessage(Component.literal(s + " [%s.%03ds]".formatted(duration.getSeconds(), sub)), false);
        };
        compileThread = new Thread(() -> {
            try {
                SpecialCompiler.specialCompilers.clear();
                // Send out js generation event, this should happen before class crawling so probe can resolve everything later
                DocGenerationEventJS event = new DocGenerationEventJS();
                ProbeJSEvents.DOC_GEN.post(ScriptType.SERVER, event);
                sendMessage.accept("Started generating type files...");
                SnippetCompiler.compile(event);
                RichItemCompiler.compile();
                RichFluidCompiler.compile();
                RichLangCompiler.compile();
                sendMessage.accept("Snippets generated.");
                ClassResolver.init();
                NameResolver.init();
                DocCompiler.compile(sendMessage, event);
                if (ProbeConfig.INSTANCE.pullSchema && ProbeConfig.INSTANCE.modChanged) {
                    RemoteSchema.dumpSchemas(sendMessage);
                }
            } catch (Exception e) {
                ProbeJS.LOGGER.error("Uncaught exception has occurred!", e);
                player.sendSystemMessage(Component.literal("Uncaught exception happened in wrapper, please report to the Github issue with complete latest.log."), false);
            }
            sendMessage.accept("ProbeJS typing generation finished.");
            compileThread = null;
        });
        compileThread.setUncaughtExceptionHandler((t, e) -> {
            ProbeJS.LOGGER.error("Uncaught exception has occurred!", e);
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                ProbeJS.LOGGER.error(stackTraceElement.toString());
            }
            sendMessage.accept("ProbeJS has run into an error! Please check out latest.log and report to GitHub!");
        });
        compileThread.setDaemon(true);
        compileThread.start();

        // triggerRender(sendMessage);
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("probejs")
                        .then(Commands.literal("dump")
                                //SINGLE PLAYER IS NEEDED
                                .requires(source -> ProbeConfig.INSTANCE.requireSingleAndPerm
                                        && (source.getServer().isSingleplayer()
                                        && source.hasPermission(2))
                                        && ProbeConfig.INSTANCE.enabled
                                )
                                .executes(context -> {
                                    var player = context.getSource().getPlayer();
                                    if (player != null) {
                                        // Before dump, notify player if there are too many mods
                                        if (Platform.getMods().size() >= ProbeConfig.MOD_COUNT) {
                                            player.sendSystemMessage(Component.literal("There are more than " + ProbeConfig.MOD_COUNT + " mods installed. You might want to disable some feature to prevent lag in VSCode."), false);
                                        }
                                        triggerDump(player);
                                    }
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(Commands.literal("clear_cache")
                                .requires(source -> source.getServer().isSingleplayer())
                                .executes(context -> {
                                    ProbeConfig.INSTANCE.docsTimestamp = -1;
                                    for (File file : Objects.requireNonNull(ProbePaths.CACHE.toFile().listFiles())) {
                                        // delete everything, including folders, folders might not be empty
                                        try {
                                            if (file.isFile()) {
                                                if (file.delete()) {
                                                    ProbeJS.LOGGER.info("Deleted file: " + file.getName());
                                                } else {
                                                    ProbeJS.LOGGER.warn("Failed to delete file: " + file.getName());
                                                }
                                            } else {
                                                FileUtils.deleteDirectory(file);
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(Commands.literal("configure")
                                .requires(source -> source.getServer().isSingleplayer())
                                .then(Commands.literal("toggle_aggressive")
                                        .executes(context -> {
                                            boolean aggressive = ProbeConfig.INSTANCE.toggleAggressiveProbing();
                                            context.getSource().sendSuccess(() -> Component.literal("Aggressive mode is now: %s".formatted(aggressive ? "disabled" : "enabled")), false);
                                            ProbeConfig.INSTANCE.save();
                                            context.getSource().sendSuccess(() -> Component.literal("Changes will be applied next time you start the game."), false);
                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(Commands.literal("toggle_registry_dumps")
                                        .executes(context -> {
                                            ProbeConfig.INSTANCE.allowRegistryObjectDumps = !ProbeConfig.INSTANCE.allowRegistryObjectDumps;
                                            context.getSource().sendSuccess(() -> Component.literal("Dump of object classes in registries: %s".formatted(ProbeConfig.INSTANCE.allowRegistryObjectDumps ? "enabled" : "disabled")), false);
                                            ProbeConfig.INSTANCE.save();
                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(Commands.literal("toggle_registry_literals")
                                        .executes(context -> {
                                            ProbeConfig.INSTANCE.allowRegistryLiteralDumps = !ProbeConfig.INSTANCE.allowRegistryLiteralDumps;
                                            context.getSource().sendSuccess(() -> Component.literal("Dump of literals in registries: %s".formatted(ProbeConfig.INSTANCE.allowRegistryLiteralDumps ? "enabled" : "disabled")), false);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                                .then(Commands.literal("toggle_dump_req")
                                        .executes(context -> {
                                            ProbeConfig.INSTANCE.requireSingleAndPerm = !ProbeConfig.INSTANCE.requireSingleAndPerm;
                                            context.getSource().sendSuccess(() -> Component.literal("Dump command now %srequire%s single player and cheat enabled".formatted(
                                                    ProbeConfig.INSTANCE.requireSingleAndPerm ? "" : "does not ",
                                                    ProbeConfig.INSTANCE.requireSingleAndPerm ? "s" : ""
                                            )), false);
                                            ProbeConfig.INSTANCE.save();
                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(Commands.literal("toggle_enable")
                                        .executes(context -> {
                                            ProbeConfig.INSTANCE.enabled = !ProbeConfig.INSTANCE.enabled;
                                            context.getSource().sendSuccess(() -> Component.literal("ProbeJS is now %s".formatted(ProbeConfig.INSTANCE.enabled ? "enabled" : "disabled")), false);
                                            ProbeConfig.INSTANCE.save();
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                                .then(Commands.literal("toggle_recipe_json")
                                        .executes(context -> {
                                            ProbeConfig.INSTANCE.disableRecipeJsonDump = !ProbeConfig.INSTANCE.disableRecipeJsonDump;
                                            context.getSource().sendSuccess(() -> Component.literal("Snippets of Recipe JSON is now %s".formatted(ProbeConfig.INSTANCE.disableRecipeJsonDump ? "disabled" : "enabled")), false);
                                            ProbeConfig.INSTANCE.save();
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                                .then(Commands.literal("toggle_json_intermediates")
                                        .executes(context -> {
                                            ProbeConfig.INSTANCE.dumpJSONIntermediates = !ProbeConfig.INSTANCE.dumpJSONIntermediates;
                                            context.getSource().sendSuccess(() -> Component.literal("JSON intermediates dumping is now %s".formatted(ProbeConfig.INSTANCE.dumpJSONIntermediates ? "enabled" : "disabled")), false);
                                            ProbeConfig.INSTANCE.save();
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                                .then(Commands.literal("toggle_schema_download")
                                        .executes(context -> {
                                            ProbeConfig.INSTANCE.pullSchema = !ProbeConfig.INSTANCE.pullSchema;
                                            context.getSource().sendSuccess(() -> Component.literal("Schema downloading is now %s".formatted(ProbeConfig.INSTANCE.pullSchema ? "enabled" : "disabled")), false);
                                            ProbeConfig.INSTANCE.save();
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )


                        )
                        .then(Commands.literal("test_availability")
                                .executes(ctx -> {
                                    boolean local = ctx.getSource().getServer().isSingleplayer();
                                    boolean perm = ctx.getSource().hasPermission(2);
                                    Consumer<String> sendMessage = s -> ctx.getSource().sendSuccess(() -> Component.literal(s), false);
                                    if (local && perm) {
                                        sendMessage.accept("You should can execute ProbeJS dump.");
                                    } else {
                                        if (!local) {
                                            sendMessage.accept("This doesn't seem to be a Local environment, or the executor is not a player at all!");
                                        } else {
                                            sendMessage.accept("It doesn't seem like you have permission to execute the dump command!");
                                        }
                                        sendMessage.accept("If you're sure that you need a dump, run \"/probejs configure toggle_dump_req\" to turn off the check!");
                                    }
                                    return Command.SINGLE_SUCCESS;
                                }))
        );
    }

}
