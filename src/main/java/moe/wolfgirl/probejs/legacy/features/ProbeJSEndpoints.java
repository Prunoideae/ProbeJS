package moe.wolfgirl.probejs.legacy.features;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.apps.tinyserver.http.response.HTTPResponse;
import dev.latvian.apps.tinyserver.http.response.HTTPStatus;
import dev.latvian.mods.kubejs.web.KJSHTTPRequest;
import dev.latvian.mods.kubejs.web.LocalWebServerRegistry;
import moe.wolfgirl.probejs.GameStates;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.legacy.utils.GameUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.*;

public class ProbeJSEndpoints {
    private static final UUID IDENTITY = UUID.randomUUID();

    private static HTTPResponse recipeIds(KJSHTTPRequest req) {
        Multimap<String, String> typeIds = ArrayListMultimap.create();

        for (Map.Entry<String, JsonObject> entry : GameStates.RECIPE_IDS.entrySet()) {
            String key = entry.getKey();
            JsonObject recipeJson = entry.getValue();
            String recipeType = recipeJson.has("type") ? entry.getValue().get("type").getAsString() : "unknown";
            if (!recipeType.contains(":")) recipeType = "minecraft:" + recipeType;
            typeIds.put(recipeType, key);
        }

        return HTTPResponse.ok().json(ProbeJS.GSON.toJson(typeIds.asMap()));
    }

    private static HTTPResponse langKeys(KJSHTTPRequest req) {
        if (Language.getInstance() instanceof ClientLanguage clientLanguage) {
            return HTTPResponse.ok().json(ProbeJS.GSON.toJson(clientLanguage.storage));
        } else {
            return HTTPResponse.noContent();
        }
    }

    private static HTTPResponse getMissingLangKeys(KJSHTTPRequest req) {
        if (Language.getInstance() instanceof ClientLanguage clientLanguage) {
            if (Minecraft.getInstance().getLanguageManager().getSelected().equals(Language.DEFAULT)) {
                return HTTPResponse.ok().json(ProbeJS.GSON.toJson(Map.of()));
            }

            ClientLanguage defaultLanguage = GameStates.DEFAULT_LANGUAGE.get();
            Map<String, String> missingInCurrent = new HashMap<>();

            for (String s : defaultLanguage.storage.keySet()) {
                if (clientLanguage.getOrDefault(s).equals(defaultLanguage.getOrDefault(s))) {
                    missingInCurrent.put(s, clientLanguage.getOrDefault(s));
                }
            }
            return HTTPResponse.ok().json(ProbeJS.GSON.toJson(missingInCurrent));
        } else {
            return HTTPResponse.noContent();
        }
    }

    private static HTTPResponse getRecipeJson(KJSHTTPRequest req) {
        var recipeId = req.query("recipe-id").value();
        if (recipeId != null && GameStates.RECIPE_IDS.containsKey(recipeId)) {
            return HTTPResponse.ok().json(ProbeJS.GSON.toJson(GameStates.RECIPE_IDS.get(recipeId)));
        } else {
            return HTTPResponse.noContent();
        }
    }

    private static HTTPResponse getIdentity(KJSHTTPRequest req) {
        return HTTPResponse.ok().json(ProbeJS.GSON.toJson(IDENTITY.toString()));
    }

    private static HTTPResponse getRecipeTypes(KJSHTTPRequest req) {
        try {
            return HTTPResponse.ok().json(ProbeJS.GSON.toJson(ProbeJSWebDoc.listRecipeTypes()));
        } catch (RuntimeException e) {
            return HTTPStatus.INTERNAL_ERROR.json(ProbeJS.GSON.toJson(e.toString()));
        }
    }

    private static HTTPResponse getRecipeDoc(KJSHTTPRequest req) {
        try {
            JsonArray jsonArray = ProbeJS.GSON.fromJson(req.mainBody().text(), JsonArray.class);
            List<String> recipes = new ArrayList<>();
            for (JsonElement jsonElement : jsonArray) {
                if (jsonElement.isJsonPrimitive()) {
                    recipes.addAll(ProbeJSWebDoc.provideRecipeDocs(ResourceLocation.parse(jsonElement.getAsString())));
                }
            }

            recipes.addFirst("    // map<, > => {k: v,...}");
            recipes.addFirst("    // fluid_stack => \"nx fluid_string\", e.g. \"1000x minecraft:water\"");
            recipes.addFirst("    // ingredient => \"item_string\" or \"#item_tag\", e.g. \"#c:ores\"");
            recipes.addFirst("    // item_stack => \"nx item_string\", e.g. 1x minecraft:apple");
            recipes.addFirst("    // Chained functions are all optional");
            recipes.addFirst("ServerEvents.recipes(event => {");
            recipes.addLast("})");
            return HTTPResponse.ok().json(ProbeJS.GSON.toJson(String.join("\n", recipes)));
        } catch (IOException e) {
            return HTTPStatus.INTERNAL_ERROR.json("\"Unable to read content\"");
        } catch (RuntimeException e) {
            return HTTPStatus.INTERNAL_ERROR.json(ProbeJS.GSON.toJson(e.toString()));
        }
    }

    private static HTTPResponse getCommands(KJSHTTPRequest req) {
        var player = Minecraft.getInstance().player;
        if (player == null) return HTTPStatus.INTERNAL_ERROR.json("\"Not in game yet.\"");
        var lines = ProbeJSWebDoc.provideCommands();
        var result = new JsonObject();
        result.addProperty("playerName", player.getGameProfile().getName());
        result.add("commands", lines);
        return HTTPResponse.ok().json(ProbeJS.GSON.toJson(result));
    }

    private static HTTPResponse runCommand(KJSHTTPRequest req) {
        try {
            var command = ProbeJS.GSON.fromJson(req.mainBody().text(), JsonObject.class).get("command").getAsString();
            var server = GameUtils.getCurrentServer();
            if (server == null) return HTTPStatus.INTERNAL_ERROR.json("\"Not in game yet, can't execute commands.\"");
            var player = server.getPlayerList().getPlayers().getFirst();
            var css = new DelegatedCommandSourceStack(player.createCommandSourceStack(), player.position(), player);
            server.getCommands().performPrefixedCommand(css, command);
            return HTTPResponse.ok().json(ProbeJS.GSON.toJson(css.getCommandMessages()));
        } catch (IOException e) {
            return HTTPStatus.INTERNAL_ERROR.json("\"Unable to read content\"");
        }
    }

    public static void register(LocalWebServerRegistry registry) {
        registry.get("/api/probejs/recipe-ids", ProbeJSEndpoints::recipeIds);
        registry.get("/api/probejs/recipe-id", ProbeJSEndpoints::getRecipeJson);
        registry.get("/api/probejs/lang-keys", ProbeJSEndpoints::langKeys);
        registry.get("/api/probejs/missing-lang-keys", ProbeJSEndpoints::getMissingLangKeys);
        registry.get("/api/probejs/identity", ProbeJSEndpoints::getIdentity);
        registry.get("/api/probejs/list-supported-recipes", ProbeJSEndpoints::getRecipeTypes);
        registry.get("/api/probejs/list-commands", ProbeJSEndpoints::getCommands);
        registry.post("/api/probejs/get-recipe-docs", ProbeJSEndpoints::getRecipeDoc);
    }

    public static void registerWithAuth(LocalWebServerRegistry registry) {
        registry.post("/api/probejs/run-command", ProbeJSEndpoints::runCommand);
    }
}
