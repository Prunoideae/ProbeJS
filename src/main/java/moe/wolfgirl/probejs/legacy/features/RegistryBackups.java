package moe.wolfgirl.probejs.legacy.features;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import moe.wolfgirl.probejs.ProbeJS;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes all registry objects to a json file for tsserver, in order to remove annoying connection requirement.
 */
public class RegistryBackups {

    public static void dump(Path path) throws IOException {
        MinecraftServer minecraftServer = ServerLifecycleHooks.getCurrentServer();
        if (minecraftServer == null) return;

        var registryAccess = minecraftServer.registryAccess();
        JsonObject allObjects = new JsonObject();
        for (RegistryAccess.RegistryEntry<?> registryEntry : ((Iterable<RegistryAccess.RegistryEntry<?>>) registryAccess.registries()::iterator)) {
            JsonArray thisObject = new JsonArray();
            allObjects.add(registryEntry.key().location().toString(), thisObject);
            for (ResourceLocation resourceLocation : registryEntry.value().keySet()) {
                thisObject.add(resourceLocation.toString());
            }
        }

        try (var writer = ProbeJS.GSON_WRITER.newJsonWriter(Files.newBufferedWriter(path))) {
            ProbeJS.GSON.toJson(allObjects, writer);
        }
    }
}
