package com.probejs.features.repl;

import com.google.gson.*;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.kubejs.server.ServerScriptManager;
import dev.latvian.mods.rhino.*;

import java.util.*;
import java.util.function.Supplier;

public class EvalManager {

    public static EvalManager STARTUP_SCRIPTS = new EvalManager(KubeJS::getStartupScriptManager);
    public static EvalManager CLIENT_SCRIPTS = new EvalManager(KubeJS::getClientScriptManager);
    public static EvalManager SERVER_SCRIPTS = new EvalManager(ServerScriptManager::getScriptManager);

    private final Map<String, Evaluator> scriptPacks = new HashMap<>();
    private Supplier<ScriptManager> scriptManager;
    private boolean loaded = false;

    public static JsonElement jsToJson(Context context, Object obj) {
        obj = context == null ? obj : Context.jsToJava(context, obj, Object.class);
        return jsToJsonInternal(context, obj, true);
    }

    private static JsonElement jsToJsonInternal(Context context, Object obj, boolean deep) {
        if (obj == null) return JsonNull.INSTANCE;
        if (obj instanceof Number number) return new JsonPrimitive(number);
        if (obj instanceof String string) return new JsonPrimitive(string);
        if (obj instanceof Boolean bool) return new JsonPrimitive(bool);
        if (Undefined.isUndefined(obj)) return new JsonPrimitive("$$ProbeJS$$undefined$$ProbeJS$$");
        if (obj.getClass().isArray()) {
            Object[] arrayObject = (Object[]) obj;
            obj = Arrays.asList(arrayObject);
        }
        if (obj instanceof List<?> list) {
            JsonArray array = new JsonArray(list.size());
            for (Object o : list) {
                array.add(jsToJsonInternal(context, o, deep));
            }
            return array;
        }
        if (obj instanceof Map<?, ?> map) {
            JsonObject jsonObject = new JsonObject();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object key = entry.getKey();
                Object value = entry.getValue();
                jsonObject.add(key.toString(), jsToJsonInternal(context, value, deep));
            }
        }
        if (deep && obj instanceof BaseFunction) {
            return new JsonPrimitive("$$ProbeJS$$Function$$ProbeJS$$");
        }
        if (obj instanceof NativeJavaObject nativeJavaObject) {
            if (deep) {
                JsonObject jsonObject = new JsonObject();
                for (Object id : nativeJavaObject.getIds(context)) {
                    if (id instanceof String idString) {
                        var property = nativeJavaObject.get(context, idString, nativeJavaObject);
                        if (!(property instanceof BaseFunction)) // Too annoying
                            jsonObject.add(idString, jsToJsonInternal(context, property, false));
                    }
                }
                return jsonObject;
            } else {
                obj = nativeJavaObject.unwrap();
            }
        }
        if (obj instanceof ScriptableObject scriptable
                && !(obj instanceof BaseFunction)) {
            JsonObject jsonObject = new JsonObject();
            for (Object id : scriptable.getIds(context)) {
                if (id instanceof String idString) {
                    jsonObject.add(idString, jsToJsonInternal(context, scriptable.get(context, idString, scriptable), false));
                }
            }
            return jsonObject;
        }

        return new JsonPrimitive("$$ProbeJS$$%s$$ProbeJS$$".formatted(obj.toString()));
        // throw new RuntimeException("Doesn't know how to interpret JS object %s".formatted(obj.toString()));
    }


    private EvalManager(Supplier<ScriptManager> manager) {
        scriptManager = manager;
    }

    private static EvalManager getManager(String type) {
        return switch (type) {
            case "startup_scripts" -> STARTUP_SCRIPTS;
            case "client_scripts" -> CLIENT_SCRIPTS;
            case "server_scripts" -> SERVER_SCRIPTS;
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
        return jsToJson(evaluator.getContext(), evaluator.evaluate(input));
    }

    public static JsonElement getPacks(JsonObject payload) {
        /*
         * {
         *  "type": "server_script", etc
         * }
         */
        String type = payload.get("type").getAsString();
        return jsToJson(null, getManager(type).getScriptPacks());
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
        return jsToJson(null, getManager(type).getEvaluator(pack).getTopLevelVariables());
    }

    private void tryLoad() {
        if (!loaded) {
            ScriptManager manager = scriptManager.get();
            if (manager == null) throw new RuntimeException("Script manager is not loaded yet.");
            manager.packs.forEach((s, scriptPack) -> scriptPacks.put(s, new Evaluator(scriptPack)));
        }
    }

    public Evaluator getEvaluator(String scriptPack) {
        tryLoad();
        if (!scriptPacks.containsKey(scriptPack))
            throw new RuntimeException("Script pack %s not found.".formatted(scriptPack));
        return scriptPacks.get(scriptPack);
    }

    public List<String> getScriptPacks() {
        tryLoad();
        return scriptPacks.keySet().stream().toList();
    }

    public void setScriptManager(Supplier<ScriptManager> manager) {
        scriptManager = manager;
        reset();
    }

    public void reset() {
        scriptPacks.clear();
        loaded = false;
    }
}
