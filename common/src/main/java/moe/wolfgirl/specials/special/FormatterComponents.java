package moe.wolfgirl.specials.special;

import moe.wolfgirl.ProbeJS;
import moe.wolfgirl.docs.formatter.formatter.IFormatter;
import moe.wolfgirl.jdoc.Serde;
import moe.wolfgirl.jdoc.java.type.InfoTypeResolver;
import moe.wolfgirl.jdoc.java.type.TypeInfoClass;
import moe.wolfgirl.jdoc.java.type.TypeInfoParameterized;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeComponentFactory;
import dev.latvian.mods.kubejs.recipe.schema.RecipeComponentFactoryRegistryEvent;
import dev.latvian.mods.kubejs.util.KubeJSPlugins;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FormatterComponents implements IFormatter {
    private static final Map<String, RecipeComponentFactory> components = new HashMap<>();

    private IFormatter getComponentType(RecipeComponentFactory factory) {
        if (factory instanceof RecipeComponentFactory.Simple simple) {
            return Serde.getTypeFormatter(Serde.deserializeFromJavaType(InfoTypeResolver.resolveType(simple.component().getClass())));
        } else {
            return Serde.getTypeFormatter(Serde.deserializeFromJavaType(new TypeInfoParameterized(new TypeInfoClass(RecipeComponent.class), List.of(
                    new TypeInfoClass(Object.class)
            ))));
        }
    }

    private String getComponentType(String id, RecipeComponentFactory factory) {
        String type = getComponentType(factory).formatFirst();
        return "%s: (%s) => %s".formatted(
                ProbeJS.GSON.toJson(id), factory instanceof RecipeComponentFactory.Simple ? "" : "...args", type
        );
    }

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        String formatted = """
                %sclass RecipeComponentMap{
                %sget<T extends keyof RecipeComponents>(id: T): RecipeComponents[T]
                }
                %stype RecipeComponents = {
                %s
                }""".formatted(
                " ".repeat(indent),
                " ".repeat(indent + stepIndent),
                " ".repeat(indent),
                components.entrySet()
                        .stream()
                        .map(e -> getComponentType(e.getKey(), e.getValue()))
                        .collect(Collectors.joining(",\n"))
        );
        return List.of(formatted);
    }

    public static Set<Class<?>> loadComponentsClasses() {
        if (components.isEmpty()) {
            KubeJSPlugins.forEachPlugin(new RecipeComponentFactoryRegistryEvent(components), KubeJSPlugin::registerRecipeComponents);
        }
        return Set.copyOf(
                components.values()
                        .stream()
                        .filter(factory -> factory instanceof RecipeComponentFactory.Simple)
                        .map(factory -> ((RecipeComponentFactory.Simple) factory).component().getClass()).collect(Collectors.toSet()));
    }
}
