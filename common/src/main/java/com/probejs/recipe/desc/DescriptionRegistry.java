package com.probejs.recipe.desc;

import com.google.gson.JsonObject;
import com.probejs.recipe.desc.impl.*;
import com.probejs.recipe.desc.impl.simple.DescriptionItemInput;
import com.probejs.recipe.desc.impl.simple.DescriptionItemOutput;
import com.probejs.recipe.desc.impl.simple.DescriptionString;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class DescriptionRegistry {
    public static final Map<String, Supplier<Description>> REGISTRY = new HashMap<>();

    public static void register(String name, Supplier<Description> description) {
        REGISTRY.put(name, description);
    }

    public static Description getDescription(JsonObject json) {
        String type = json.get("type").getAsString();
        Supplier<Description> description = REGISTRY.get(type);
        if (description == null) {
            return Description.FALLBACK;
        }
        Description desc = description.get();
        desc.deserialize(json);
        return desc;
    }

    static {
        register("string", DescriptionString::new);
        register("number", DescriptionNumber::new);
        register("array", DescriptionArray::new);
        register("optional", DescriptionOptional::new);
        register("pattern_key", DescriptionPatternKey::new);
        register("input_item", DescriptionItemInput::new);
        register("output_item", DescriptionItemOutput::new);
    }
}
