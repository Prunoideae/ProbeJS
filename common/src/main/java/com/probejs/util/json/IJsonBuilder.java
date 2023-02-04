package com.probejs.util.json;

import com.google.gson.JsonElement;

public interface IJsonBuilder<T extends JsonElement> {
    T serialize();
}
