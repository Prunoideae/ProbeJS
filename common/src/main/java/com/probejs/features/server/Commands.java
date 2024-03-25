package com.probejs.features.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.probejs.features.extension.VSCodeManager;
import com.probejs.features.repl.EvalManager;

public enum Commands {
    @SerializedName("evaluate")
    EVALUATE,
    @SerializedName("getPacks")
    GET_PACKS,
    @SerializedName("getGlobals")
    GET_GLOBALS,

    @SerializedName("onFileSaved")
    ON_FILE_SAVED;

    public static class Payload {
        public String id;
        public Commands command;
        public JsonObject payload;
    }

    public static JsonElement process(Payload payload) {
        return switch (payload.command) {
            case EVALUATE -> EvalManager.evaluate(payload.payload);
            case GET_PACKS -> EvalManager.getPacks(payload.payload);
            case GET_GLOBALS -> EvalManager.getGlobals(payload.payload);
            case ON_FILE_SAVED -> VSCodeManager.onFilesChanged(payload.payload);
        };
    }

}
