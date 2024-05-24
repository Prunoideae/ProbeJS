package moe.wolfgirl.features.plugin;

import com.google.gson.JsonObject;
import moe.wolfgirl.ProbeJS;
import moe.wolfgirl.docs.formatter.formatter.IFormatter;
import moe.wolfgirl.jdoc.java.ClassInfo;
import moe.wolfgirl.jdoc.java.MethodInfo;
import moe.wolfgirl.jdoc.Serde;
import moe.wolfgirl.jdoc.document.DocumentClass;
import moe.wolfgirl.jdoc.property.AbstractProperty;
import moe.wolfgirl.util.json.JArray;
import moe.wolfgirl.util.json.JObject;
import moe.wolfgirl.util.json.JPrimitive;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.rhino.util.HideFromJS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DocGenerationEventJS extends EventJS {
    private final Map<String, List<Consumer<DocumentClass>>> transformers = new HashMap<>();
    private final List<IFormatter> specialFormatters = new ArrayList<>();

    private final List<Consumer<JsonObject>> snippets = new ArrayList<>();

    public DocGenerationEventJS specialType(String typeName, List<Object> elements) {
        specialFormatters.add((indent, stepIndent) -> List.of(
                "%stype %s = %s;".formatted(
                        " ".repeat(indent), typeName,
                        elements.stream()
                                .map(Object::toString)
                                .collect(Collectors.joining(" | "))
                )));
        return this;
    }

    public DocGenerationEventJS transformDocument(Class<?> clazz, Consumer<DocumentClass> transformer) {
        return transformByName(MethodInfo.getRemappedOrOriginalClass(clazz), transformer);
    }

    public DocGenerationEventJS transformByName(String clazz, Consumer<DocumentClass> transformer) {
        transformers.computeIfAbsent(clazz, s -> new ArrayList<>()).add(transformer);
        return this;
    }

    public DocumentClass getJavaClass(Class<?> clazz) {
        return DocumentClass.fromJava(ClassInfo.getOrCache(clazz));
    }

    public AbstractProperty<?> getProperty(JsonObject object) {
        return Serde.deserializeProperty(object);
    }

    public DocGenerationEventJS addSnippet(String name, List<Object> items) {
        return addSnippet(name, items, null);
    }

    public DocGenerationEventJS addSnippet(String name, List<Object> items, String desc) {
        snippets.add(resultJson -> resultJson.add(name, JObject.create()
                        .add("prefix", JArray.create().add(new JPrimitive("@" + name)))
                        .add("body", new JPrimitive("${1|%s|}".formatted(items.stream()
                                .map(ProbeJS.GSON::toJson)
                                .collect(Collectors.joining(","))
                        )))
                        .ifThen(desc != null, o -> o.add("description", new JPrimitive(desc)))
                        .serialize()
                )
        );
        return this;
    }

    public DocGenerationEventJS customSnippet(String type, List<String> prefixes, List<Object> body) {
        return customSnippet(type, prefixes, body, null);
    }

    public DocGenerationEventJS customSnippet(String type, List<String> prefixes, List<Object> body, String desc) {
        snippets.add(resultJson ->
                resultJson.add(type, JObject.create()
                        .add("prefix", JArray.create().addAll(prefixes.stream().map(JPrimitive::new)))
                        .add("body", JArray.create().addAll(body.stream().map(Object::toString).map(JPrimitive::new)))
                        .ifThen(desc != null, o -> o.add("description", new JPrimitive(desc)))
                        .serialize()
                )
        );
        return this;
    }

    @HideFromJS
    public Map<String, List<Consumer<DocumentClass>>> getTransformers() {
        return transformers;
    }

    @HideFromJS
    public List<IFormatter> getSpecialFormatters() {
        return specialFormatters;
    }

    @HideFromJS
    public List<Consumer<JsonObject>> getSnippets() {
        return snippets;
    }
}
