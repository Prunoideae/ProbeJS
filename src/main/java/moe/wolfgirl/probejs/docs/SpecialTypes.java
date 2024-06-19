package moe.wolfgirl.probejs.docs;

import moe.wolfgirl.probejs.GlobalStates;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.lang.snippet.SnippetDump;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.lang.typescript.code.member.TypeDecl;
import moe.wolfgirl.probejs.lang.typescript.code.ts.Wrapped;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;

import java.util.Collection;
import java.util.stream.Collectors;

public class SpecialTypes extends ProbeJSPlugin {
    @Override
    public void addGlobals(ScriptDump scriptDump) {
        Wrapped.Namespace special = new Wrapped.Namespace("Special");

        // We define special types regardless of script type
        // because types might be sent to other scripts
        defineLiteralTypes(special, "LangKey", GlobalStates.LANG_KEYS.get());
        defineLiteralTypes(special, "RecipeId", GlobalStates.RECIPE_IDS);
        defineLiteralTypes(special, "LootTable", GlobalStates.LOOT_TABLES);
        defineLiteralTypes(special, "RawTexture", GlobalStates.RAW_TEXTURES.get());
        defineLiteralTypes(special, "Texture", GlobalStates.TEXTURES.get());
        defineLiteralTypes(special, "Mod", GlobalStates.MODS.get());

        scriptDump.addGlobal("special_types", special);
    }

    @Override
    public void addVSCodeSnippets(SnippetDump dump) {
        defineLiteralSnippets(dump, "lang_key", GlobalStates.LANG_KEYS.get());
        defineLiteralSnippets(dump, "recipe_id", GlobalStates.RECIPE_IDS);
        defineLiteralSnippets(dump, "loot_table", GlobalStates.LOOT_TABLES);
        defineLiteralSnippets(dump, "texture", GlobalStates.TEXTURES.get());
        defineLiteralSnippets(dump, "mod", GlobalStates.MODS.get());
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
