package moe.wolfgirl.probejs.plugin.builtins.extras;

import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.snippet.SnippetRegisterer;
import moe.wolfgirl.probejs.utils.GameUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;

public class RegistrySnippets extends ProbeJSPlugin {
    @Override
    public void addSnippets(SnippetRegisterer registerer) {
        MinecraftServer currentServer = GameUtils.getCurrentServer();
        if (currentServer == null) return;
        RegistryAccess registryAccess = currentServer.registryAccess();

        for (ResourceKey<? extends Registry<?>> key : GameUtils.getRegistries(registryAccess)) {
            String registryName = key.location().getPath();
            var registry = registryAccess.registry(key).orElse(null);
            if (registry == null) continue;
            if (registry.keySet().isEmpty()) continue;
            registerer.snippet(registryName)
                    .prefix("@" + registryName)
                    .description("Creates a registry entry for `%s`.".formatted(registryName))
                    .literal("\"")
                    .registry(GameUtils.castKey(key))
                    .literal("\"");
        }
    }
}
