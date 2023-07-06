package com.probejs.recipe.component;

import com.probejs.compiler.formatter.formatter.IFormatter;
import com.probejs.jdoc.Serde;
import com.probejs.jdoc.property.PropertyComment;
import com.probejs.jdoc.property.PropertyType;
import com.probejs.jdoc.property.PropertyValue;
import com.probejs.util.Util;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FormatterRecipeKey implements IFormatter {
    private final RecipeKey<?> key;

    public FormatterRecipeKey(RecipeKey<?> key) {
        this.key = key;
    }

    private Stream<String> getNames() {
        return key.names.stream().map(Util::getSafeName);
    }

    public List<IFormatter> getBuilders() {
        return getNames().map(
                name -> (IFormatter) (indent, stepIndent) -> List.of(
                        "%s(%s: %s): this".formatted(name, name.contains("\"") ? "arg" : name,
                                Serde.getTypeFormatter(ComponentConverter.fromDescription(key.component.constructorDescription(ComponentConverter.PROBEJS_CONTEXT)))
                                        .underscored()
                                        .formatFirst())
                )
        ).collect(Collectors.toList());
    }

    public PropertyComment getComments(RecipeSchemaType type) {
        PropertyComment comment = new PropertyComment();
        List<String> hints = new ArrayList<>();
        if (key.optional != null) {
            PropertyValue<?, ?> formatter = Serde.getValueProperty(key.optional.getDefaultValue(type));
            if (!(formatter instanceof PropertyValue.FallbackValue)) {
                hints.add("defaults to `%s`".formatted(Objects.requireNonNull(Serde.getValueFormatter(formatter)).formatFirst()));
            }
        }

        if (!hints.isEmpty()) {
            comment.add("@param %s %s".formatted(Util.getSafeName(key.preferred), String.join(", ", hints)));
        }
        return comment;
    }

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        String name = Util.getSafeName(key.preferred);
        PropertyType<?> type = ComponentConverter.fromDescription(key.component.constructorDescription(ComponentConverter.PROBEJS_CONTEXT));

        if (key.optional != null)
            name += "?";

        return List.of("%s: %s".formatted(name, Serde.getTypeFormatter(type).underscored().formatFirst()));
    }
}
