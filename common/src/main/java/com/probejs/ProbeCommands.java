package com.probejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.datafixers.util.Pair;
import com.probejs.compiler.DocCompiler;
import com.probejs.compiler.SnippetCompiler;
import com.probejs.compiler.SpecialCompiler;
import com.probejs.compiler.formatter.ClassResolver;
import com.probejs.compiler.formatter.NameResolver;
import com.probejs.jdoc.java.ClassInfo;
import com.probejs.jdoc.document.DocumentClass;
import com.probejs.jdoc.jsgen.DocGenerationEventJS;
import com.probejs.jdoc.jsgen.ProbeJSEvents;
import com.probejs.rich.item.RichItemCompiler;
import dev.latvian.mods.kubejs.KubeJSPaths;
import dev.latvian.mods.kubejs.bindings.ItemWrapper;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ProbeCommands {
    public static ServerLevel COMMAND_LEVEL = null;
    public static Thread compileThread = null;
    public static Thread resolveRenderThread = null;

    public static void triggerRender(Consumer<String> sendMessage) {
        if (resolveRenderThread != null && resolveRenderThread.isAlive()) {
            sendMessage.accept("Skipping image rendering due to previous render thread still running.");
        } else if (resolveRenderThread != null) {
            sendMessage.accept("Previous render thread is dead! Please check out latest.log and submit an error report.");
            resolveRenderThread = null;
        }
        resolveRenderThread = new Thread(() -> {

            sendMessage.accept("Resolving items to render...");
            List<Pair<ItemStack, Path>> items = new ArrayList<>();
            for (ItemStack itemStack : ItemWrapper.getList()) {
                Path path = ProbePaths.RICH.resolve(itemStack.kjs$getIdLocation().getNamespace());
                if (!Files.exists(path)) {
                    try {
                        Files.createDirectories(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                String name = itemStack.kjs$getIdLocation().getPath().replace("/", "_");
                if (path.resolve(name + ".png").toFile().exists()) {
                    continue;
                }
                items.add(Pair.of(itemStack, path.resolve(name + ".png")));
            }
            sendMessage.accept("Items resolved, rendering " + items.size() + " items...");
            Minecraft.getInstance().execute(() -> {
                try {
                    RichItemCompiler.render(items);
                    sendMessage.accept("Images rendered.");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            resolveRenderThread = null;
        });

        resolveRenderThread.setUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            sendMessage.accept("Error occurred while rendering images! Please check out latest.log and submit an error report.");
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
        COMMAND_LEVEL = player.getLevel();
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
                sendMessage.accept("Snippets generated.");
                ClassResolver.init();
                NameResolver.init();
                DocCompiler.compile(sendMessage, event);
            } catch (Exception e) {
                ProbeJS.LOGGER.error(e);
                for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                    ProbeJS.LOGGER.error(stackTraceElement);
                }
                player.sendSystemMessage(Component.literal("Uncaught exception happened in wrapper, please report to the Github issue with complete latest.log."), false);
            }
            sendMessage.accept("ProbeJS typing generation finished.");
            compileThread = null;
        });
        compileThread.setUncaughtExceptionHandler((t, e) -> {
            ProbeJS.LOGGER.error(e);
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                ProbeJS.LOGGER.error(stackTraceElement.toString());
            }
            sendMessage.accept("ProbeJS has run into an error! Please check out latest.log and report to GitHub!");
        });
        compileThread.setDaemon(true);
        compileThread.start();

        triggerRender(sendMessage);
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
                                    if (player != null)
                                        triggerDump(player);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(Commands.literal("clear_cache")
                                .requires(source -> source.getServer().isSingleplayer())
                                .executes(context -> {
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
                                            context.getSource().sendSuccess(Component.literal("Aggressive mode is now: %s".formatted(aggressive ? "disabled" : "enabled")), false);
                                            ProbeConfig.INSTANCE.save();
                                            context.getSource().sendSuccess(Component.literal("Changes will be applied next time you start the game."), false);
                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(Commands.literal("toggle_registry_dumps")
                                        .executes(context -> {
                                            ProbeConfig.INSTANCE.allowRegistryObjectDumps = !ProbeConfig.INSTANCE.allowRegistryObjectDumps;
                                            context.getSource().sendSuccess(Component.literal("Dump of object classes in registries: %s".formatted(ProbeConfig.INSTANCE.allowRegistryObjectDumps ? "enabled" : "disabled")), false);
                                            ProbeConfig.INSTANCE.save();
                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(Commands.literal("toggle_dump_req")
                                        .executes(context -> {
                                            ProbeConfig.INSTANCE.requireSingleAndPerm = !ProbeConfig.INSTANCE.requireSingleAndPerm;
                                            context.getSource().sendSuccess(Component.literal("Dump command now %srequire%s single player and cheat enabled".formatted(
                                                    ProbeConfig.INSTANCE.requireSingleAndPerm ? "" : "does not ",
                                                    ProbeConfig.INSTANCE.requireSingleAndPerm ? "s" : ""
                                            )), false);
                                            ProbeConfig.INSTANCE.save();
                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(Commands.literal("toggle_enable")
                                        .executes(context -> {
                                            ProbeConfig.INSTANCE.enabled = !ProbeConfig.INSTANCE.enabled;
                                            context.getSource().sendSuccess(Component.literal("ProbeJS is now %s".formatted(ProbeConfig.INSTANCE.enabled ? "enabled" : "disabled")), false);
                                            ProbeConfig.INSTANCE.save();
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )

                        )
                        .then(Commands.literal("export")
                                .requires(source -> source.getServer().isSingleplayer())
                                .then(Commands.argument("className", StringArgumentType.string())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                ClassInfo.CLASS_CACHE
                                                        .values()
                                                        .stream()
                                                        .map(ClassInfo::getName),
                                                builder)
                                        )
                                        .executes(ctx -> {
                                            String className = StringArgumentType.getString(ctx, "className");
                                            ClassInfo info = ClassInfo.CLASS_NAME_CACHE.get(className);
                                            String[] nameParts = info.getName().split("\\.");
                                            JsonObject document = DocumentClass.fromJava(info).serialize();
                                            JsonArray outArray = new JsonArray();
                                            outArray.add(document);
                                            try {
                                                BufferedWriter writer = Files.newBufferedWriter(KubeJSPaths.EXPORTED.resolve(nameParts[nameParts.length - 1] + ".json"));
                                                JsonWriter jsonWriter = ProbeJS.GSON_WRITER.newJsonWriter(writer);
                                                jsonWriter.setIndent("    ");
                                                ProbeJS.GSON_WRITER.toJson(outArray, JsonArray.class, jsonWriter);
                                                jsonWriter.close();
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                        .then(Commands.literal("test_availability")
                                .executes(ctx -> {
                                    boolean local = ctx.getSource().getServer().isSingleplayer();
                                    boolean perm = ctx.getSource().hasPermission(2);
                                    Consumer<String> sendMessage = s -> ctx.getSource().sendSuccess(Component.literal(s), false);
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
