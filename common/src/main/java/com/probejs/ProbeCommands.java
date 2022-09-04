package com.probejs;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.probejs.compiler.SchemaCompiler;
import com.probejs.compiler.SnippetCompiler;
import com.probejs.compiler.TypingCompiler;
import com.probejs.document.Manager;
import com.probejs.document.comment.CommentHandler;
import com.probejs.document.parser.processor.DocumentProviderHandler;
import com.probejs.formatter.ClassResolver;
import com.probejs.formatter.NameResolver;
import com.probejs.info.ClassInfo;
import com.probejs.info.MethodInfo;
import com.probejs.jdoc.Serde;
import com.probejs.jdoc.document.DocumentClass;
import dev.latvian.mods.kubejs.KubeJSPaths;
import dev.latvian.mods.kubejs.item.ingredient.IngredientJS;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ProbeCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                Commands.literal("probejs")
                        .then(Commands.literal("dump")
                                //SINGLE PLAYER IS NEEDED
                                .requires(source -> source.getServer().isSingleplayer() && source.hasPermission(2))
                                .executes(context -> {
                                    Instant start = Instant.now();
                                    try {
                                        SnippetCompiler.compile();
                                        SchemaCompiler.compile();
                                        DocumentProviderHandler.init();
                                        CommentHandler.init();
                                        Manager.init();
                                        ClassResolver.init();
                                        NameResolver.init();
                                        TypingCompiler.compile();
                                        if (ProbeConfig.INSTANCE.exportClassNames)
                                            SnippetCompiler.compileClassNames();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        context.getSource().sendSuccess(new TextComponent("Uncaught exception happened in wrapper, please report to the Github issue with complete latest.log."), false);
                                    }
                                    Instant end = Instant.now();
                                    Duration duration = Duration.between(start, end);
                                    long sub = TimeUnit.MILLISECONDS.convert(duration.getNano(), TimeUnit.NANOSECONDS);
                                    context.getSource().sendSuccess(new TextComponent("ProbeJS typing generation finished in %s.%03ds.".formatted(duration.getSeconds(), sub)), false);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(Commands.literal("clear_cache")
                                .requires(source -> source.getServer().isSingleplayer())
                                .executes(context -> {
                                    Path path = KubeJSPaths.EXPORTED.resolve("cachedEvents.json");
                                    if (Files.exists(path)) {
                                        if (path.toFile().delete()) {
                                            context.getSource().sendSuccess(new TextComponent("Cache files removed."), false);
                                        } else {
                                            context.getSource().sendSuccess(new TextComponent("Failed to remove cache files."), false);
                                        }
                                    } else {
                                        context.getSource().sendSuccess(new TextComponent("No cached files to be cleared."), false);
                                    }
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(Commands.literal("configure")
                                .requires(source -> source.getServer().isSingleplayer())
                                .then(Commands.literal("toggle_bean")
                                        .executes(context -> {
                                            ProbeConfig.INSTANCE.dumpMethod = !ProbeConfig.INSTANCE.dumpMethod;
                                            context.getSource().sendSuccess(new TextComponent("Keep method while beaning set to: %s".formatted(ProbeConfig.INSTANCE.dumpMethod)), false);
                                            ProbeConfig.INSTANCE.save();
                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(Commands.literal("toggle_aggressive")
                                        .executes(context -> {
                                            ProbeConfig.INSTANCE.noAggressiveProbing = !ProbeConfig.INSTANCE.noAggressiveProbing;
                                            context.getSource().sendSuccess(new TextComponent("Aggressive mode is now: %s".formatted(ProbeConfig.INSTANCE.noAggressiveProbing ? "disabled" : "enabled")), false);
                                            ProbeConfig.INSTANCE.save();
                                            context.getSource().sendSuccess(new TextComponent("Changes will be applied next time you start the game."), false);
                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(Commands.literal("toggle_snippet_order")
                                        .executes(context -> {
                                            ProbeConfig.INSTANCE.vanillaOrder = !ProbeConfig.INSTANCE.vanillaOrder;
                                            context.getSource().sendSuccess(new TextComponent("In snippets, which will appear first: %s".formatted(ProbeConfig.INSTANCE.vanillaOrder ? "mod_id" : "member_type")), false);
                                            ProbeConfig.INSTANCE.save();
                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(Commands.literal("toggle_classname_snippets")
                                        .executes(context -> {
                                            ProbeConfig.INSTANCE.exportClassNames = !ProbeConfig.INSTANCE.exportClassNames;
                                            context.getSource().sendSuccess(new TextComponent("Export class name as snippets set to: %s".formatted(ProbeConfig.INSTANCE.exportClassNames)), false);
                                            ProbeConfig.INSTANCE.save();
                                            return Command.SINGLE_SUCCESS;
                                        }))
                        )
                        .then(Commands.literal("test")
                                .requires(source -> true)
                                .executes(context -> {
                                    try {
                                        DocumentClass document = DocumentClass.fromJava(ClassInfo.getOrCache(IngredientJS.class));
                                        String serialized = ProbeJS.GSON.toJson(document.serialize());
                                        ProbeJS.LOGGER.info(document.serialize().toString());
                                        DocumentClass clazz = (DocumentClass) Serde.deserializeDocument(ProbeJS.GSON.fromJson(serialized, JsonObject.class));
                                        ProbeJS.LOGGER.info(clazz.getMethods().equals(document.getMethods()));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    return Command.SINGLE_SUCCESS;
                                }))
        );
    }

}
