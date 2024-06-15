package moe.wolfgirl.probejs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.PlayerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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


    public static void init() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> GameEvents.registerCommand(dispatcher));
        PlayerEvent.PLAYER_JOIN.register(GameEvents::playerJoined);
    }
}
