package moe.wolfgirl.probejs.plugin.builtins.alias;

import moe.wolfgirl.probejs.GameStates;
import moe.wolfgirl.probejs.ClassPath;
import moe.wolfgirl.probejs.java.ClassRegistry;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.typescript.Documents;
import moe.wolfgirl.probejs.typescript.base.DocumentRegistrar;
import moe.wolfgirl.probejs.typescript.document.Members;
import moe.wolfgirl.probejs.typescript.document.TypeDecl;
import moe.wolfgirl.probejs.typescript.document.Types;
import moe.wolfgirl.probejs.typescript.document.base.KindAware;
import moe.wolfgirl.probejs.typescript.document.base.Type;
import moe.wolfgirl.probejs.typescript.document.builders.ClassBuilder;

import java.util.Collection;

// Some literal unions that can be useful but not registry-based
// e.g. ModId, RecipeId, TranslationKey, ...
public class SpecialTypes extends ProbeJSPlugin {
    public static final ClassPath SPECIAL_TYPES = ClassPath.special("types.SpecialTypes");

    public static final Type MOD_ID = Types.namespaced(SPECIAL_TYPES, "ModId");
    public static final Type RECIPE_ID = Types.namespaced(SPECIAL_TYPES, "RecipeId");
    public static final Type TRANSLATION_KEY = Types.namespaced(SPECIAL_TYPES, "TranslationKey");
    public static final Type LOOT_TABLE = Types.namespaced(SPECIAL_TYPES, "LootTable");
    public static final Type CLASS_PATH = Types.namespaced(SPECIAL_TYPES, "ClassPath");

    @Override
    public void addSpecialDocuments(DocumentRegistrar registrar) {
        ClassBuilder builder = Members.clazz(SPECIAL_TYPES).kind(KindAware.Kind.NAMESPACE);

        builder.member(makeSpecial("ModId", GameStates.MODS.get()));
        builder.member(makeSpecial("RecipeId", GameStates.RECIPE_IDS.keySet()));
        builder.member(makeSpecial("TranslationKey", GameStates.LANG_KEYS.get()));
        builder.member(makeSpecial("LootTable", GameStates.LOOT_TABLES));
        builder.member(makeSpecial("ClassPath", ClassRegistry.INSTANCE.getAllClasses().keySet().stream().map(ClassPath::asJavaPath).toList()));

        registrar.addDocument(SPECIAL_TYPES, builder.build());
    }

    private static TypeDecl makeSpecial(String name, Collection<String> literals) {
        return new TypeDecl(
                SPECIAL_TYPES.append(name),
                Types.union(literals.stream().map(Types::literal).map(t -> (Type) t).toList()),
                false
        );
    }
}
