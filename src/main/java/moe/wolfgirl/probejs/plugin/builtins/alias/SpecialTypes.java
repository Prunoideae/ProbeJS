package moe.wolfgirl.probejs.plugin.builtins.alias;

import dev.latvian.mods.kubejs.plugin.builtin.wrapper.GLFWInputWrapper;
import moe.wolfgirl.probejs.GameStates;
import moe.wolfgirl.probejs.typescript.ClassPath;
import moe.wolfgirl.probejs.java.ClassRegistry;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.typescript.base.DocumentRegistrar;
import moe.wolfgirl.probejs.typescript.document.Members;
import moe.wolfgirl.probejs.typescript.document.TypeDecl;
import moe.wolfgirl.probejs.typescript.document.Types;
import moe.wolfgirl.probejs.typescript.document.base.KindAware;
import moe.wolfgirl.probejs.typescript.document.base.Type;
import moe.wolfgirl.probejs.typescript.document.builders.ClassBuilder;
import moe.wolfgirl.probejs.typescript.document.types.special.NamespacedType;

import java.util.Collection;

// Some literal unions that can be useful but not registry-based
// e.g. ModId, RecipeId, TranslationKey, ...
public class SpecialTypes extends ProbeJSPlugin {
    public static final ClassPath SPECIAL_TYPES = ClassPath.special("types.SpecialTypes");

    public static final NamespacedType MOD_ID = Types.namespaced(SPECIAL_TYPES, "ModId");
    public static final NamespacedType RECIPE_ID = Types.namespaced(SPECIAL_TYPES, "RecipeId");
    public static final NamespacedType TRANSLATION_KEY = Types.namespaced(SPECIAL_TYPES, "TranslationKey");
    public static final NamespacedType LOOT_TABLE = Types.namespaced(SPECIAL_TYPES, "LootTable");
    public static final NamespacedType CLASS_PATH = Types.namespaced(SPECIAL_TYPES, "ClassPath");
    public static final NamespacedType GLFW_INPUT = Types.namespaced(SPECIAL_TYPES, "GLFWInput");

    @Override
    public void addSpecialDocuments(DocumentRegistrar registrar) {
        ClassBuilder builder = Members.clazz(SPECIAL_TYPES).kind(KindAware.Kind.NAMESPACE);

        builder.member(makeSpecial(MOD_ID, GameStates.MODS.get()));
        builder.member(makeSpecial(RECIPE_ID, GameStates.RECIPE_IDS.keySet()));
        builder.member(makeSpecial(TRANSLATION_KEY, GameStates.LANG_KEYS.get()));
        builder.member(makeSpecial(LOOT_TABLE, GameStates.LOOT_TABLES));
        builder.member(makeSpecial(CLASS_PATH, ClassRegistry.INSTANCE.getAllClasses().keySet().stream().map(ClassPath::asJavaPath).toList()));
        builder.member(makeSpecial(GLFW_INPUT, GLFWInputWrapper.MAP.get().keySet()));
        registrar.addDocument(SPECIAL_TYPES, builder.build());
    }

    private static TypeDecl makeSpecial(NamespacedType type, Collection<String> literals) {
        return new TypeDecl(
                type.asClassPath(),
                Types.union(literals.stream().map(Types::literal).map(t -> (Type) t).toList()),
                false
        );
    }
}
