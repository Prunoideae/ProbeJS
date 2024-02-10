package com.probejs.repl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public enum REPLCommand {
    @SerializedName("evaluate")
    EVALUATE,
    @SerializedName("getPacks")
    GET_PACKS,
    @SerializedName("getGlobals")
    GET_GLOBALS;

    public static class Payload {
        public String id;
        public REPLCommand command;
        public JsonObject payload;
    }

    public static JsonElement process(Payload payload) {
        return switch (payload.command) {
            case EVALUATE -> evaluate(payload.payload);
            case GET_PACKS -> getPacks(payload.payload);
            case GET_GLOBALS -> getGlobals(payload.payload);
        };
    }

    private static EvalManager getManager(String type) {
        return switch (type) {
            case "startup_scripts" -> EvalManager.STARTUP_SCRIPTS;
            case "client_scripts" -> EvalManager.CLIENT_SCRIPTS;
            case "server_scripts" -> EvalManager.SERVER_SCRIPTS;
            default -> throw new RuntimeException("Unknown script manager %s.".formatted(type));
        };
    }

    public static JsonElement evaluate(JsonObject payload) {
        /*
         * {
         *  "type": "server_script", etc
         *  "pack": "server_script", etc
         *  "input": "1", etc
         * }
         */
        String type = payload.get("type").getAsString();
        String pack = payload.get("pack").getAsString();
        String input = payload.get("input").getAsString();

        EvalManager manager = getManager(type);
        Evaluator evaluator = manager.getEvaluator(pack);
        return EvalManager.jsToJson(evaluator.getContext(), evaluator.evaluate(input));
    }

    public static JsonElement getPacks(JsonObject payload) {
        /*
         * {
         *  "type": "server_script", etc
         * }
         */
        String type = payload.get("type").getAsString();
        return EvalManager.jsToJson(null, getManager(type).getScriptPacks());
    }

    public static JsonElement getGlobals(JsonObject payload) {
        /*
         * {
         *  "type": "server_script", etc
         *  "pack": "server_script", etc
         * }
         */
        String type = payload.get("type").getAsString();
        String pack = payload.get("pack").getAsString();
        return EvalManager.jsToJson(null, getManager(type).getEvaluator(pack).getTopLevelVariables());
    }
}
