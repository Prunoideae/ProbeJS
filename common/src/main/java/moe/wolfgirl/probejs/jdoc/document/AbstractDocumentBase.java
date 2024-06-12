package moe.wolfgirl.probejs.jdoc.document;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import moe.wolfgirl.probejs.jdoc.IConditional;
import moe.wolfgirl.probejs.jdoc.ISerde;
import moe.wolfgirl.probejs.jdoc.Serde;
import moe.wolfgirl.probejs.jdoc.property.AbstractProperty;
import moe.wolfgirl.probejs.jdoc.property.PropertyComment;
import moe.wolfgirl.probejs.jdoc.property.PropertyHide;
import dev.latvian.mods.rhino.util.HideFromJS;

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
 * <p>
 * The document can be (partially) overwritten by another document. Which is especially
 * used in the user docs.
 * <p>
 * Despite the auto-generated docs will always be overwritten by
 * user docs, there's no guarantee for a doc to load first or second.
 */
public abstract class AbstractDocumentBase<T extends AbstractDocumentBase<T>> implements ISerde {
    public static final BiMap<Class<? extends AbstractDocumentBase<?>>, String> DOCUMENT_TYPE_REGISTRY = HashBiMap.create();

    protected List<AbstractProperty<?>> properties = new ArrayList<>();


    /**
     * Apply properties modification to current document.
     * <p>
     * This returns a new document.
     *
     * @return the document with properties applied
     */
    public abstract T applyProperties();

    /**
     * Merges other document into this document.
     * <p>
     * Returns a new document without modifying either document, however, subfields might be shallow-copy.
     * <p>
     * For most of the time it's similar to copy(). However, it merges comments automatically too.
     *
     * @param other the other document
     * @return a merged new document
     */
    public T merge(T other) {
        if (this == other)
            return other;
        T document = other.copy();
        properties.addAll(0, properties.stream().filter(prop -> prop instanceof PropertyComment).toList());
        return document;
    }

    public abstract T copy();

    public JsonObject serialize() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", DOCUMENT_TYPE_REGISTRY.get(this.getClass()));
        Serde.serializeCollection(obj, "properties", properties, true);
        return obj;
    }

    public void deserialize(JsonObject object) {
        Serde.deserializeDocuments(properties, object.get("properties"));
    }

    @SuppressWarnings("unchecked")
    public final <P extends AbstractProperty<P>> Optional<P> findProperty(Class<P> property) {
        return this.properties.stream().filter(p -> p.getClass() == property).map(p -> (P) p).findFirst();
    }

    public final <P extends AbstractProperty<P>> boolean hasProperty(Class<P> property) {
        return findProperty(property).isPresent();
    }

    public final List<AbstractProperty<?>> findProperties(Predicate<AbstractProperty<?>> predicate) {
        return this.properties.stream().filter(predicate).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public final <E extends AbstractProperty<?>> List<E> findPropertiesOf(Class<E> property) {
        return this.properties.stream().filter(prop -> property.isAssignableFrom(prop.getClass())).map(prop -> (E) prop).collect(Collectors.toList());
    }

    @HideFromJS
    public final void addProperty(AbstractProperty<?> property) {
        this.properties.add(property);
    }

    public final AbstractDocumentBase<T> addPropertyJson(JsonObject json) {
        addProperty(Serde.deserializeProperty(json));
        return this;
    }

    public final boolean fulfillsConditions() {
        return findProperties(prop -> prop instanceof IConditional).stream().allMatch(prop -> ((IConditional) prop).test());
    }

    public final boolean isHidden() {
        return hasProperty(PropertyHide.class);
    }


}
