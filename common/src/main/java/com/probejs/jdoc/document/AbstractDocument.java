package com.probejs.jdoc.document;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.probejs.jdoc.ISerde;
import com.probejs.jdoc.Serde;
import com.probejs.jdoc.property.AbstractProperty;
import com.probejs.jdoc.property.PropertyHide;
import com.probejs.jdoc.property.PropertyMod;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Represents a document.
 * <p>
 * You must ensure that the parameter-less constructor <b>does not</b> depend
 * on any other stateful objects. As this document might be constructed at any time.
 */
public abstract class AbstractDocument<T extends AbstractDocument<T>> implements ISerde {
    public static final BiMap<Class<? extends AbstractDocument<?>>, String> DOCUMENT_TYPE_REGISTRY = HashBiMap.create();

    protected List<AbstractProperty> properties = new ArrayList<>();

    public abstract T merge(T other);

    public abstract T copy();

    public JsonObject serialize() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", DOCUMENT_TYPE_REGISTRY.get(this.getClass()));
        JsonArray jsonProperties = new JsonArray();
        for (AbstractProperty property : properties) {
            jsonProperties.add(property.serialize());
        }
        obj.add("properties", jsonProperties);
        return obj;
    }

    public void deserialize(JsonObject object) {
        JsonArray propertiesJson = object.get("properties").getAsJsonArray();
        for (JsonElement element : propertiesJson) {
            AbstractProperty property = Serde.deserializeProperty(element.getAsJsonObject());
            this.properties.add(property);
        }
    }

    public Optional<AbstractProperty> findProperty(Class<? extends AbstractProperty> property) {
        return this.properties.stream().filter(p -> p.getClass() == property).findFirst();
    }

    public boolean hasProperty(Class<? extends AbstractProperty> property) {
        return findProperty(property).isPresent();
    }

    public List<AbstractProperty> findProperties(Predicate<AbstractProperty> predicate) {
        return this.properties.stream().filter(predicate).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractProperty> List<T> findPropertiesOf(Class<T> property) {
        return this.properties.stream().filter(prop -> property.isAssignableFrom(prop.getClass())).map(prop -> (T) prop).collect(Collectors.toList());
    }

    public boolean allModsLoaded() {
        return findPropertiesOf(PropertyMod.class).stream().allMatch(PropertyMod::isModLoaded);
    }

    public boolean isHidden() {
        return hasProperty(PropertyHide.class);
    }
}
