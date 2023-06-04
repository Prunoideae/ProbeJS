package com.probejs.recipe.component;

import com.probejs.jdoc.property.PropertyType;
import dev.latvian.mods.kubejs.typings.desc.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ComponentConverter {
    public static PropertyType<?> fromDescription(TypeDescJS description) {
        if (description instanceof ArrayDescJS array) {
            return new PropertyType.Array(fromDescription(array.type()));
        } else if (description instanceof FixedArrayDescJS fixedArray) {
            return new PropertyType.JSArray(Arrays.stream(fixedArray.types()).map(ComponentConverter::fromDescription).collect(Collectors.toList()));
        } else if (description instanceof GenericDescJS parameterized) {
            return new PropertyType.Parameterized(
                    fromDescription(parameterized.type()),
                    Arrays.stream(parameterized.types())
                            .map(ComponentConverter::fromDescription)
                            .collect(Collectors.toList())
            );
        } else if (description instanceof ObjectDescJS object) {
            return new PropertyType.JSObject(
                    object.types().stream()
                            .map(pair -> Pair.of(
                                    new PropertyType.JSObjectKey().withName(pair.getLeft()),
                                    fromDescription(pair.getRight())
                            ))
                            .collect(Collectors.toMap(Pair::getLeft, Pair::getRight))
            );
        } else if (description instanceof OrDescJS or) {
            return new PropertyType.Union(
                    Arrays.stream(or.types())
                            .map(ComponentConverter::fromDescription)
                            .collect(Collectors.toList())
            );
        } else if (description instanceof PrimitiveDescJS primitive) {
            String name = primitive.type();
            if (name.equals("null")) // return any here because we don't know what to do with null
                return new PropertyType.Clazz(Object.class);
            return name.contains(".") ? new PropertyType.Clazz(name) : new PropertyType.Native(name);
        } else {
            return new PropertyType.Clazz(Object.class);
        }
    }

}
