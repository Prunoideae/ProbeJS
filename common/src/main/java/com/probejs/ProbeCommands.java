package com.probejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.probejs.compiler.DocCompiler;
import com.probejs.compiler.SnippetCompiler;
import com.probejs.formatter.ClassResolver;
import com.probejs.formatter.NameResolver;
import com.probejs.formatter.formatter.jdoc.FormatterClass;
import com.probejs.info.ClassInfo;
import com.probejs.jdoc.document.DocumentClass;
import dev.latvian.mods.kubejs.KubeJSPaths;
import dev.latvian.mods.kubejs.item.ingredient.IngredientJS;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ProbeCommands {
    public static ServerLevel COMMAND_LEVEL = null;
    public static boolean isRunning = false;
    public static Thread runningThread = null;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                Commands.literal("probejs")
                        .then(Commands.literal("dump")
                                //SINGLE PLAYER IS NEEDED
                                .requires(source -> ProbeConfig.INSTANCE.requireSingleAndPerm && (source.getServer().isSingleplayer() && source.hasPermission(2)))
                                .executes(context -> {
                                    if (runningThread != null && runningThread.isAlive()) {
                                        context.getSource().sendSuccess(Component.literal("ProbeJS is running! Please wait for current dump to finish."), false);
                                        return Command.SINGLE_SUCCESS;
                                    } else if (runningThread != null) {
                                        context.getSource().sendFailure(Component.literal("ProbeJS dumping thread is dead! Please check out latest.log and submit an error report."));
                                        runningThread = null;
                                    }
                                    COMMAND_LEVEL = context.getSource().getLevel();
                                    Instant start = Instant.now();
                                    Consumer<String> sendMessage = s -> {
                                        Instant end = Instant.now();
                                        Duration duration = Duration.between(start, end);
                                        long sub = TimeUnit.MILLISECONDS.convert(duration.getNano(), TimeUnit.NANOSECONDS);
                                        context.getSource().sendSuccess(Component.literal(s + " [%s.%03ds]".formatted(duration.getSeconds(), sub)), false);
                                    };
                                    runningThread = new Thread(() -> {
                                        try {
                                            sendMessage.accept("Started generating type files...");
                                            SnippetCompiler.compile();
                                            sendMessage.accept("Snippets generated.");
                                            ClassResolver.init();
                                            NameResolver.init();
                                            DocCompiler.compile(sendMessage);
                                        } catch (Exception e) {
                                            ProbeJS.LOGGER.error(e);
                                            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                                                ProbeJS.LOGGER.error(stackTraceElement);
                                            }
                                            context.getSource().sendSuccess(Component.literal("Uncaught exception happened in wrapper, please report to the Github issue with complete latest.log."), false);
                                        }
                                        sendMessage.accept("ProbeJS typing generation finished.");
                                        runningThread = null;
                                    });
                                    runningThread.setUncaughtExceptionHandler((t, e) -> {
                                        ProbeJS.LOGGER.error(e);
                                        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                                            ProbeJS.LOGGER.error(stackTraceElement.toString());
                                        }
                                        sendMessage.accept("ProbeJS has run into an error! Please check out latest.log and report to GitHub!");
                                    });
                                    runningThread.setDaemon(true);
                                    runningThread.start();
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(Commands.literal("clear_cache")
                                .requires(source -> source.getServer().isSingleplayer())
                                .executes(context -> {
                                    for (File file : Objects.requireNonNull(ProbePaths.CACHE.toFile().listFiles())) {
                                        if (file.isFile()) {
                                            if (file.delete()) {
                                                ProbeJS.LOGGER.info("Removed %s".formatted(file.getName()));
                                            } else {
                                                ProbeJS.LOGGER.info("Failed to remove %s".formatted(file.getName()));
                                            }
                                        }
                                    }
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(Commands.literal("configure")
                                .requires(source -> source.getServer().isSingleplayer())
                                .then(Commands.literal("toggle_bean")
                                        .executes(context -> {
                                            ProbeConfig.INSTANCE.dumpMethod = !ProbeConfig.INSTANCE.dumpMethod;
                                            context.getSource().sendSuccess(Component.literal("Keep method while beaning set to: %s".formatted(ProbeConfig.INSTANCE.dumpMethod)), false);
                                            ProbeConfig.INSTANCE.save();
                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(Commands.literal("toggle_aggressive")
                                        .executes(context -> {
                                            ProbeConfig.INSTANCE.noAggressiveProbing = !ProbeConfig.INSTANCE.noAggressiveProbing;
                                            context.getSource().sendSuccess(Component.literal("Aggressive mode is now: %s".formatted(ProbeConfig.INSTANCE.noAggressiveProbing ? "disabled" : "enabled")), false);
                                            ProbeConfig.INSTANCE.save();
                                            context.getSource().sendSuccess(Component.literal("Changes will be applied next time you start the game."), false);
                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(Commands.literal("toggle_snippet_order")
                                        .executes(context -> {
                                            ProbeConfig.INSTANCE.vanillaOrder = !ProbeConfig.INSTANCE.vanillaOrder;
                                            context.getSource().sendSuccess(Component.literal("In snippets, which will appear first: %s".formatted(ProbeConfig.INSTANCE.vanillaOrder ? "mod_id" : "member_type")), false);
                                            ProbeConfig.INSTANCE.save();
                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(Commands.literal("toggle_classname_snippets")
                                        .executes(context -> {
                                            ProbeConfig.INSTANCE.exportClassNames = !ProbeConfig.INSTANCE.exportClassNames;
                                            context.getSource().sendSuccess(Component.literal("Export class name as snippets set to: %s".formatted(ProbeConfig.INSTANCE.exportClassNames)), false);
                                            ProbeConfig.INSTANCE.save();
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
                                    ServerPlayer player = ctx.getSource().getPlayer();
                                    boolean local = player != null && player.isLocalPlayer();
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

                                    }
                                    return Command.SINGLE_SUCCESS;
                                }))
        );
    }

}
