package com.probejs;

import com.google.gson.Gson;
import com.probejs.formatter.formatter.FormatterRecipeId;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProbeJS {
    public static final String MOD_ID = "probejs";
    public static final Logger LOGGER = LogManager.getLogger("probejs");
    public static final Gson GSON = new Gson();

    public static void init() {
        CommandRegistrationEvent.EVENT.register((dispatcher, selection) -> ProbeCommands.register(dispatcher));
    }
}
