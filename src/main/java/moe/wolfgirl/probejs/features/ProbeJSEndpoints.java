package moe.wolfgirl.probejs.features;

import dev.latvian.apps.tinyserver.http.response.HTTPResponse;
import dev.latvian.mods.kubejs.web.KJSHTTPRequest;
import dev.latvian.mods.kubejs.web.LocalWebServerRegistry;
import moe.wolfgirl.probejs.GlobalStates;
import moe.wolfgirl.probejs.ProbeJS;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.locale.Language;

public class ProbeJSEndpoints {
    private static HTTPResponse recipeIds(KJSHTTPRequest req) {
        return HTTPResponse.ok().json(ProbeJS.GSON.toJson(GlobalStates.RECIPE_IDS.stream().toList()));
    }

    private static HTTPResponse langKeys(KJSHTTPRequest req) {
        if (Language.getInstance() instanceof ClientLanguage clientLanguage) {
            return HTTPResponse.ok().json(ProbeJS.GSON.toJson(clientLanguage.storage));
        } else {
            return HTTPResponse.noContent();
        }
    }

    public static void register(LocalWebServerRegistry registry) {
        registry.get("/api/recipe-ids", ProbeJSEndpoints::recipeIds);
        registry.get("/api/lang-keys", ProbeJSEndpoints::langKeys);
    }
}
