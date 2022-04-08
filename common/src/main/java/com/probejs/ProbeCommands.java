package com.probejs;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.probejs.compiler.SnippetCompiler;
import com.probejs.compiler.TypingCompiler;
import com.probejs.document.Manager;
import com.probejs.document.comment.CommentHandler;
import com.probejs.document.parser.processor.DocumentProviderHandler;
import com.probejs.formatter.ClassResolver;
import com.probejs.formatter.NameResolver;
import dev.latvian.mods.kubejs.KubeJSPaths;
import dev.latvian.mods.kubejs.server.ServerSettings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.ReloadCommand;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.WorldData;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

public class ProbeCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                Commands.literal("probejs")
                        .then(Commands.literal("dump")
                                .requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
                                .executes(context -> {
                                    try {
                                        if (ProbeConfig.INSTANCE.autoExport)
                                            export(context.getSource());
                                        SnippetCompiler.compile();
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
                                    context.getSource().sendSuccess(new TextComponent("ProbeJS typing generation finished."), false);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(Commands.literal("clear_cache")
                                .requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
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
                                .then(Commands.literal("toggle_bean").executes(context -> {
                                    ProbeConfig.INSTANCE.dumpMethod = !ProbeConfig.INSTANCE.dumpMethod;
                                    context.getSource().sendSuccess(new TextComponent("Keep method while beaning set to: %s".formatted(ProbeConfig.INSTANCE.dumpMethod)), false);
                                    ProbeConfig.INSTANCE.save();
                                    return Command.SINGLE_SUCCESS;
                                }))
                                .then(Commands.literal("toggle_mixin").executes(context -> {
                                    ProbeConfig.INSTANCE.disabled = !ProbeConfig.INSTANCE.disabled;
                                    context.getSource().sendSuccess(new TextComponent("OnEvent mixin wrapper set to: %s".formatted(ProbeConfig.INSTANCE.disabled ? "disabled" : "enabled")), false);
                                    ProbeConfig.INSTANCE.save();
                                    context.getSource().sendSuccess(new TextComponent("Changes will be applied next time you start the game."), false);
                                    return Command.SINGLE_SUCCESS;
                                }))
                                .then(Commands.literal("toggle_snippet_order").executes(context -> {
                                    ProbeConfig.INSTANCE.vanillaOrder = !ProbeConfig.INSTANCE.vanillaOrder;
                                    context.getSource().sendSuccess(new TextComponent("In snippets, which will appear first: %s".formatted(ProbeConfig.INSTANCE.vanillaOrder ? "mod_id" : "member_type")), false);
                                    ProbeConfig.INSTANCE.save();
                                    return Command.SINGLE_SUCCESS;
                                }))
                                .then(Commands.literal("toggle_classname_snippets").executes(context -> {
                                    ProbeConfig.INSTANCE.exportClassNames = !ProbeConfig.INSTANCE.exportClassNames;
                                    context.getSource().sendSuccess(new TextComponent("Export class name as snippets set to: %s".formatted(ProbeConfig.INSTANCE.exportClassNames)), false);
                                    ProbeConfig.INSTANCE.save();
                                    return Command.SINGLE_SUCCESS;
                                }))
                                .then(Commands.literal("toggle_autoexport").executes(context -> {
                                    ProbeConfig.INSTANCE.autoExport = !ProbeConfig.INSTANCE.autoExport;
                                    context.getSource().sendSuccess(new TextComponent("Auto-export for KubeJS set to: %s".formatted(ProbeConfig.INSTANCE.autoExport)), false);
                                    ProbeConfig.INSTANCE.save();
                                    return Command.SINGLE_SUCCESS;
                                }))
                        )
                //.then(Commands.literal("test")
                //        .requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
                //       .executes(context -> {
                //          RegistryCompiler.getBuilderTypes();
                //         return Command.SINGLE_SUCCESS;
                //    }))
        );
    }

    private static void export(CommandSourceStack source) {
        if (ServerSettings.dataExport != null) {
            return;
        }

        ServerSettings.source = source;
        ServerSettings.dataExport = new JsonObject();
        source.sendSuccess(new TextComponent("Reloading server and exporting data..."), false);

        MinecraftServer minecraftServer = source.getServer();
        PackRepository packRepository = minecraftServer.getPackRepository();
        WorldData worldData = minecraftServer.getWorldData();
        Collection<String> collection = packRepository.getSelectedIds();
        packRepository.reload();
        Collection<String> collection2 = Lists.newArrayList(collection);
        Collection<String> collection3 = worldData.getDataPackConfig().getDisabled();

        for (String string : packRepository.getAvailableIds()) {
            if (!collection3.contains(string) && !collection2.contains(string)) {
                collection2.add(string);
            }
        }

        ReloadCommand.reloadPacks(collection2, source);
    }
}
