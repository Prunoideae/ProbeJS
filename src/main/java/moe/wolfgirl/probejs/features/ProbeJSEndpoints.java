package moe.wolfgirl.probejs.features;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import dev.latvian.apps.tinyserver.http.response.HTTPResponse;
import dev.latvian.mods.kubejs.web.KJSHTTPRequest;
import dev.latvian.mods.kubejs.web.LocalWebServerRegistry;
import moe.wolfgirl.probejs.GameStates;
import moe.wolfgirl.probejs.ProbeJS;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.locale.Language;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    public static void register(LocalWebServerRegistry registry) {
        registry.get("/api/probejs/recipe-ids", ProbeJSEndpoints::recipeIds);
        registry.get("/api/probejs/recipe-id", ProbeJSEndpoints::getRecipeJson);
        registry.get("/api/probejs/lang-keys", ProbeJSEndpoints::langKeys);
        registry.get("/api/probejs/missing-lang-keys", ProbeJSEndpoints::getMissingLangKeys);
        registry.get("/api/probejs/identity", ProbeJSEndpoints::getIdentity);
    }
}
