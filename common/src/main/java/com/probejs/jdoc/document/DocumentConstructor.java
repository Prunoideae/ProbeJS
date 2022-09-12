package com.probejs.jdoc.document;

import com.google.gson.JsonObject;
import com.probejs.info.ConstructorInfo;
import com.probejs.jdoc.Serde;
import com.probejs.jdoc.property.PropertyModify;
import com.probejs.jdoc.property.PropertyParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DocumentConstructor extends AbstractDocument<DocumentConstructor> {
    private final List<PropertyParam> params = new ArrayList<>();

    public DocumentConstructor() {

    }

    public DocumentConstructor(List<PropertyParam> params) {
        this.params.addAll(params);
    }


    @Override
    public JsonObject serialize() {
        JsonObject object = super.serialize();
        Serde.serializeCollection(object, "params", params);
        return object;
    }

    @Override
    public void deserialize(JsonObject object) {
        super.deserialize(object);
        Serde.deserializeDocuments(params, object.get("params"));
    }

    @Override
    public DocumentConstructor applyProperties() {
        DocumentConstructor copy = copy();
        copy.findPropertiesOf(PropertyModify.class).forEach(modify -> {
            PropertyParam param = copy.params.get(modify.getOrdinal());
            copy.params.set(modify.getOrdinal(), new PropertyParam(
                    modify.getName() != null ? modify.getName() : param.getName(),
                    modify.getNewType() != null ? modify.getNewType() : param.getType(),
                    param.isVarArg()
            ));
        });
        return copy;
    }

    @Override
    public DocumentConstructor copy() {
        return new DocumentConstructor(params);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentConstructor that = (DocumentConstructor) o;
        return Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(params);
    }

    public List<PropertyParam> getParams() {
        return params;
    }

    public static DocumentConstructor fromJava(ConstructorInfo info) {
        DocumentConstructor document = new DocumentConstructor();
        info.getParams().stream().map(PropertyParam::fromJava).forEach(document.params::add);
        return document;
    }
}
