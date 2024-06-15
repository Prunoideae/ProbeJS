package moe.wolfgirl.probejs.docs;

import moe.wolfgirl.probejs.GlobalStates;
import moe.wolfgirl.probejs.typescript.ScriptDump;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.typescript.code.member.TypeDecl;
import moe.wolfgirl.probejs.typescript.code.ts.Wrapped;
import moe.wolfgirl.probejs.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.typescript.code.type.Types;

import java.util.Collection;

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

    private static void defineLiteralTypes(Wrapped.Namespace special, String symbol, Collection<String> literals) {
        BaseType[] types = literals.stream().map(Types::literal).toArray(BaseType[]::new);
        TypeDecl declaration = new TypeDecl(symbol, Types.or(types));
        special.addCode(declaration);
    }
}
