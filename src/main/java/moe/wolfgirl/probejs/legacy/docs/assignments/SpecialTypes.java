package moe.wolfgirl.probejs.legacy.docs.assignments;

import moe.wolfgirl.probejs.GameStates;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.legacy.lang.snippet.SnippetDump;
import moe.wolfgirl.probejs.legacy.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.legacy.docs.ProbeJSPlugin;
import moe.wolfgirl.probejs.legacy.lang.typescript.code.member.TypeDecl;
import moe.wolfgirl.probejs.legacy.lang.typescript.code.ts.Wrapped;
import moe.wolfgirl.probejs.legacy.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.legacy.lang.typescript.code.type.Types;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SpecialTypes extends ProbeJSPlugin {
    @Override
    public void addGlobals(ScriptDump scriptDump) {
        Wrapped.Namespace special = new Wrapped.Namespace("Special");

        // We define special types regardless of script type
        // because types might be sent to other scripts
        defineLiteralTypes(special, "LangKey", List.of("probejs$$translation"));
        defineLiteralTypes(special, "RecipeId", List.of("probejs$$recipeId"));
        defineLiteralTypes(special, "LootTable", GameStates.LOOT_TABLES);
        defineLiteralTypes(special, "Mod", List.of("probejs$$mod"));
        scriptDump.addGlobal("special_types", special);
    }

    @Override
    public void addVSCodeSnippets(SnippetDump dump) {
        defineLiteralSnippets(dump, "lang_key", GameStates.LANG_KEYS.get());
        defineLiteralSnippets(dump, "recipe_id", GameStates.RECIPE_IDS.keySet());
        defineLiteralSnippets(dump, "loot_table", GameStates.LOOT_TABLES);
        defineLiteralSnippets(dump, "texture", GameStates.TEXTURES.get());
        defineLiteralSnippets(dump, "mod", GameStates.MODS.get());
    }

    private static void defineLiteralTypes(Wrapped.Namespace special, String symbol, Collection<String> literals) {
        BaseType[] types = literals.stream().map(Types::literal).toArray(BaseType[]::new);
        TypeDecl declaration = new TypeDecl(symbol, Types.or(types));
        special.addCode(declaration);
    }

    private static void defineLiteralSnippets(SnippetDump dump, String symbol, Collection<String> literals) {
        dump.snippet(symbol)
                .prefix("@" + symbol)
                .choices(literals.stream()
                        .map(ProbeJS.GSON::toJson)
                        .collect(Collectors.toSet())
                );
    }
}
