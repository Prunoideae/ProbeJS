package com.probejs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.probejs.jdoc.Serde;
import com.probejs.recipe.component.ComponentConverter;
import com.probejs.specials.assign.ClassAssignmentManager;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.PlayerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;

public class ProbeJS {
    public static final String MOD_ID = "probejs";
    public static final Logger LOGGER = LogManager.getLogger("probejs");
    public static final Gson GSON = new GsonBuilder()
            .serializeSpecialFloatingPointValues()
            .disableHtmlEscaping()
            .create();
    public static final Gson GSON_WRITER = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    public static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    public static void init() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> ProbeCommands.register(dispatcher));
        PlayerEvent.PLAYER_JOIN.register(ProbeJSEvents::playerJoined);
        Serde.init();
        ClassAssignmentManager.init(ComponentConverter.PROBEJS_CONTEXT);
    }
}
