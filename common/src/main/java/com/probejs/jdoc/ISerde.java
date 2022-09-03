package com.probejs.jdoc;

import com.google.gson.JsonObject;

public interface ISerde {
    JsonObject serialize();

    void deserialize(JsonObject object);
}
