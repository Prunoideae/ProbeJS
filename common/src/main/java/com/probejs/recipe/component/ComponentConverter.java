package com.probejs.recipe.component;

import com.probejs.jdoc.property.PropertyType;
import dev.latvian.mods.kubejs.recipe.component.*;

import java.util.List;
import java.util.Map;

public class ComponentConverter {
    public static PropertyType<?> fromComponent(RecipeComponent<?> component) {
        if (component instanceof ArrayRecipeComponent<?> array) {
            if (array.canWriteSelf())
                return new PropertyType.Union(List.of(
                        new PropertyType.Array(fromComponent(array.component())),
                        fromComponent(array.component())
                ));
            else {
                return new PropertyType.Array(fromComponent(array.component()));
            }
        }

        if (component instanceof OrRecipeComponent<?, ?> or) {
            return new PropertyType.Union(List.of(
                    fromComponent(or.high()),
                    fromComponent(or.low()))
            );
        } else if (component instanceof AndRecipeComponent<?, ?> and) {
            return new PropertyType.JSArray(List.of(
                    fromComponent(and.a()),
                    fromComponent(and.b()))
            );
        }

        if (component instanceof MapRecipeComponent<?, ?> map) {
            return new PropertyType.Parameterized(
                    new PropertyType.Clazz(Map.class),
                    List.of(
                            fromComponent(map.key()),
                            fromComponent(map.component())
                    )
            );
        }

        return new PropertyType.Clazz(component.componentClass());
    }

}
