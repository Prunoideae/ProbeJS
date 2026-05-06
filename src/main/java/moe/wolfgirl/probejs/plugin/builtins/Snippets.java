package moe.wolfgirl.probejs.plugin.builtins;

import moe.wolfgirl.probejs.GameStates;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.snippet.SnippetRegisterer;
import moe.wolfgirl.probejs.snippet.parts.Variable;
import net.minecraft.core.registries.Registries;

import java.util.List;

public class Snippets extends ProbeJSPlugin {
    @Override
    public void addSnippets(SnippetRegisterer registerer) {
        defineHeader(registerer, "priority", "0");
        defineHeader(registerer, "packmode", null);

        registerer.snippet("ignored")
                .prefix("#ignored")
                .description("Creates the file header for `ignored`.")
                .literal("// ignored: ")
                .choices(List.of("true", "false"));

        registerer.snippet("requires")
                .prefix("#requires")
                .description("Creates the file header for `requires`.")
                .literal("// requires: ")
                .choices(GameStates.MODS.get());

        registerer.snippet("uuid")
                .prefix("uuid")
                .description("Generates a random version 4 UUID.")
                .literal("\"")
                .variable(Variable.UUID)
                .literal("\"");

        registerer.snippet("recipes")
                .prefix("recipes")
                .description("Creates a recipe event listener.")
                .literal("ServerEvents.recipes(event => {")
                .newline()
                .literal("    const { ")
                .tabStop(1)
                .literal(" } = event.recipes")
                .newline()
                .literal("    ")
                .tabStop(0)
                .newline()
                .literal("})");

        registerer.snippet("itemstack")
                .prefix("itemstack")
                .description("Creates a `nx item string.")
                .literal("\"")
                .tabStop(1, "1")
                .literal("x ")
                .registry(Registries.ITEM)
                .literal("\"");

        registerer.snippet("recipe_id")
                .prefix("recipe_id")
                .description("Insert a recipe ID")
                .literal("\"")
                .choices(GameStates.RECIPE_IDS.keySet())
                .literal("\"");
    }

    private static void defineHeader(SnippetRegisterer registerer, String symbol, String defaultValue) {
        registerer.snippet(symbol)
                .prefix("#" + symbol)
                .description("Creates the file header for `%s`.".formatted(symbol))
                .literal("// %s: ".formatted(symbol))
                .tabStop(0, defaultValue);
    }
}
