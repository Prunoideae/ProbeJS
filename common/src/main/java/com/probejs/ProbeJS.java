package com.probejs;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.PlayerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProbeJS {
    public static final String MOD_ID = "probejs";
    public static final Logger LOGGER = LogManager.getLogger("probejs");

    public static void init() {
        CommandRegistrationEvent.EVENT.register((dispatcher, selection) -> ProbeCommands.register(dispatcher));
        if (!ProbeConfig.INSTANCE.aggressiveProbing) {
            PlayerEvent.PLAYER_JOIN.register(ProbeCommands::alertAggressiveDump);
        }
    }
}
