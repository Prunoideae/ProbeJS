package moe.wolfgirl.probejs.features;

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

public class ProbeJSEndpoints {
    private static HTTPResponse recipeIds(KJSHTTPRequest req) {
        return HTTPResponse.ok().json(ProbeJS.GSON.toJson(GameStates.RECIPE_IDS.stream().toList()));
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

    public static void register(LocalWebServerRegistry registry) {
        registry.get("/api/recipe-ids", ProbeJSEndpoints::recipeIds);
        registry.get("/api/lang-keys", ProbeJSEndpoints::langKeys);
        registry.get("/api/missing-lang-keys", ProbeJSEndpoints::getMissingLangKeys);
    }
}
