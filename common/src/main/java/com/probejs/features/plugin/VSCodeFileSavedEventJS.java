package com.probejs.features.plugin;

import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.server.ServerEventJS;
import net.minecraft.server.MinecraftServer;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

public class VSCodeFileSavedEventJS extends ServerEventJS {
     public static final FileSystem FILE_SYSTEM = FileSystems.getDefault();

    public final Path file;

    public VSCodeFileSavedEventJS(Path file, MinecraftServer server) {
        super(server);
        this.file = file;
    }

    public static void postToHandlers(VSCodeFileSavedEventJS event) {
        ProbeJSEvents.VSC_FILE_SAVED.forEachListener(ScriptType.SERVER, container -> {
            PathMatcher matcher = (PathMatcher) container.extraId;
            if (matcher.matches(event.file)) {
                try {
                    container.handle(event, null);
                } catch (Exception ignored) {
                }
            }
        });
    }
}
