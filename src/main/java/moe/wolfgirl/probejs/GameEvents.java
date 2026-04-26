package moe.wolfgirl.probejs;

import com.mojang.brigadier.Command;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.client.KubeJSClient;
import dev.latvian.mods.kubejs.script.ScriptType;
import moe.wolfgirl.probejs.events.CodeGenerationEventJS;
import moe.wolfgirl.probejs.events.ProbeEvents;
import moe.wolfgirl.probejs.next.PackageDump;
import moe.wolfgirl.probejs.next.java.ClassRegistry;
import moe.wolfgirl.probejs.utils.GameUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.HoeItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;


import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@EventBusSubscriber(value = Dist.CLIENT)
public class GameEvents {
    private static final int MOD_LIMIT = 200;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void playerJoined(ClientPlayerNetworkEvent.LoggingIn event) {
        var player = event.getPlayer();
        ProbeConfig config = ProbeConfig.INSTANCE;

        if (config.enabled.get() && Minecraft.getInstance().isLocalServer()) {
            if (config.modHash.get() == -1) {
                player.sendSystemMessage(Component.translatable("probejs.hello").kjs$gold());
                if (ModList.get().size() >= MOD_LIMIT) {
                    player.sendSystemMessage(
                            Component.translatable("probejs.performance", ModList.get().size())
                    );
                    config.classScanning.set(false);
                    config.complete.set(false);
                }
            }
            if (config.modHash.get() != GameUtils.modHash()) {
                if (!ProbeDumpingThread.exists()) { // Not very possible but anyway
                    ProbeDumpingThread.create(player::sendSystemMessage).start();
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
                            .append(Component.literal("Wiki Page")
                                    .kjs$aqua()
                                    .kjs$underlined()
                                    .kjs$clickOpenUrl("https://kubejs.com/wiki/addons/third-party/probejs")
                                    .kjs$hover(Component.literal("https://kubejs.com/wiki/addons/third-party/probejs")))
            );

            // Reload creative mode tabs
            var params = new CreativeModeTab.ItemDisplayParameters(
                    player.connection.enabledFeatures(),
                    player.canUseGameMasterBlocks() && Minecraft.getInstance().options.operatorItemsTab().get(),
                    player.level().registryAccess()
            );

            CreativeModeTabs.tabs().stream()
                    .filter(t -> t.getType() != CreativeModeTab.Type.CATEGORY && t.getType() != CreativeModeTab.Type.SEARCH)
                    .forEach(t -> t.buildContents(params));
        }

        // CreativeModeTabs.CACHED_PARAMETERS = null;
        // CreativeModeTabs.tryRebuildTabContents(
        //     player.connection.enabledFeatures(),
        //     player.canUseGameMasterBlocks() && Minecraft.getInstance().options.operatorItemsTab().get(),
        //     player.level().registryAccess()
        //);
    }

    @SubscribeEvent
    public static void registerCommand(RegisterClientCommandsEvent event) {
        var dispatcher = event.getDispatcher();
        dispatcher.register(
                Commands.literal("probejs")
                        .then(Commands.literal("dump")
                                .requires(source -> ProbeConfig.INSTANCE.enabled.get() && source.hasPermission(2))
                                .executes(context -> {
                                    Consumer<Component> messageSender = component -> context.getSource().sendSystemMessage(component);
                                    if (ProbeDumpingThread.exists()) {
                                        messageSender.accept(Component.translatable("probejs.already_running"));
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    KubeJSClient.reloadClientScripts();
                                    ProbeDumpingThread.create(messageSender).start();
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
                                .requires(source -> !ProbeConfig.INSTANCE.enabled.get() && source.hasPermission(2))
                                .executes(context -> {
                                    ProbeConfig.INSTANCE.enabled.set(true);
                                    context.getSource().sendSystemMessage(Component.translatable("probejs.hello_again").kjs$aqua());
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
                        .then(Commands.literal("toggle_beans")
                                .requires(source -> ProbeConfig.INSTANCE.enabled.get() && source.hasPermission(2))
                                .executes(context -> {
                                    boolean flag = !ProbeConfig.INSTANCE.beans.get();
                                    ProbeConfig.INSTANCE.beans.set(flag);
                                    context.getSource().sendSystemMessage(flag ?
                                            Component.translatable("probejs.generate_beans") :
                                            Component.translatable("probejs.no_generate_beans"));
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
                        .then(Commands.literal("generate")
                                // I found it might be not that useful, maybe later
                                .requires(source -> false && ProbeConfig.INSTANCE.enabled.get() && source.hasPermission(2))
                                .then(Commands.argument("script", new CodeGenerationEventJS.ScriptArgument())
                                        .executes(context -> {
                                            var scriptTarget = context.getArgument("script", String.class);
                                            var codegenEvent = new CodeGenerationEventJS();
                                            ProbeEvents.CODEGEN.post(ScriptType.CLIENT, scriptTarget, codegenEvent);
                                            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(ProbePaths.GENERATED_CODE.resolve("%s.js".formatted(scriptTarget)))) {
                                                bufferedWriter.write(String.join("\n", codegenEvent.getContent()));
                                            } catch (IOException e) {
                                                context.getSource().sendFailure(Component.literal("Unable to open file..."));
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        }))
                        )
                        .then(Commands.literal("test")
                                .requires(source -> true)
                                .executes(context -> {
                                    new Thread(() -> {
                                        ClassRegistry.INSTANCE.putClass(HoeItem.class, 0);
                                        ClassRegistry.INSTANCE.discover();
                                        var tree = ClassRegistry.INSTANCE.resolveTree();
                                        for (var node : tree.traverse()) {
                                            ProbeJS.LOGGER.info("%s -> %s".formatted(
                                                    node.getClassPath(),
                                                    node.getSubPackages()
                                            ));
                                        }
                                        PackageDump dump = new PackageDump(ProbePaths.PACKAGES);
                                        dump.dump();
                                    }).start();
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
        );
    }

    @SubscribeEvent
    public static void rightClickedBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getSide() == LogicalSide.SERVER) GameStates.LAST_RIGHTCLICKED = event.getPos();
    }

    @SubscribeEvent
    public static void rightClickedEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getSide() == LogicalSide.SERVER) GameStates.LAST_ENTITY = event.getTarget();
    }

    @SubscribeEvent
    public static void changedDimension(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof Player player && !(player instanceof FakePlayer)) {
            GameStates.LAST_RIGHTCLICKED = null;
            GameStates.LAST_ENTITY = null;
        }
    }
}

