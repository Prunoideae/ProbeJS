package moe.wolfgirl.probejs.features;

import com.google.common.collect.Iterables;
import com.google.gson.JsonArray;
import com.mojang.brigadier.tree.CommandNode;
import dev.latvian.mods.kubejs.recipe.RecipeFunction;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.schema.*;
import dev.latvian.mods.kubejs.recipe.schema.function.RecipeFunctionInstance;
import dev.latvian.mods.kubejs.server.ServerScriptManager;
import dev.latvian.mods.rhino.type.EnumTypeInfo;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.lang.typescript.Declaration;
import moe.wolfgirl.probejs.lang.typescript.code.member.ParamDecl;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import moe.wolfgirl.probejs.lang.typescript.code.type.js.JSLambdaType;
import moe.wolfgirl.probejs.utils.GameUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A list of endpoints that provides docs in
 */
public class ProbeJSWebDoc {

    public static List<String> listRecipeTypes() {
        ServerScriptManager scriptManager = GameUtils.getServerScriptManager();
        if (scriptManager == null) throw new RuntimeException("Not in game yet. No recipe manager can be read from.");
        List<String> types = new ArrayList<>();
        for (RecipeNamespace namespace : scriptManager.recipeSchemaStorage.namespaces.values()) {
            for (RecipeSchemaType schemaType : namespace.values()) {
                if (schemaType instanceof UnknownRecipeSchemaType) continue;
                if (schemaType.schema.isHidden()) continue;
                if (!BuiltInRegistries.RECIPE_SERIALIZER.containsKey(schemaType.id)) continue;
                types.add(schemaType.id.toString());
            }
        }
        return types;
    }

    public static List<String> provideRecipeDocs(ResourceLocation recipeType) {
        ServerScriptManager scriptManager = GameUtils.getServerScriptManager();
        if (scriptManager == null) throw new RuntimeException("Not in game yet. No recipe manager can be read from.");
        var schema = scriptManager.recipeSchemaStorage.namespace(recipeType.getNamespace()).get(recipeType.getPath());
        if (schema == null) throw new RuntimeException("Invalid recipe type: %s".formatted(recipeType));
        List<String> formattedMethods = new ArrayList<>();
        for (RecipeConstructor value : schema.schema.constructors().values()) {
            formattedMethods.add("    event.recipes.%s.%s%s".formatted(recipeType.getNamespace(), recipeType.getPath(), value.toString()));
        }
        for (RecipeKey<?> key : schema.schema.keys) {
            var name = key.getPrimaryFunctionName();
            if (RecipeFunction.isValidIdentifier(name.toCharArray())) {
                formattedMethods.add("        .%s(%s)".formatted(name, key.component));
            }
        }
        for (RecipeFunctionInstance value : schema.schema.functions.values()) {
            if (RecipeFunction.isValidIdentifier(value.name().toCharArray())) {
                formattedMethods.add("        ." + value);
            }
        }
        return formattedMethods;
    }

    public static JsonArray provideCommands() {
        var server = GameUtils.getCurrentServer();
        if (server == null) throw new RuntimeException("Not in game yet, no command dispatcher can be read.");

        var dispatcher = server.getCommands().getDispatcher();
        var css = server.createCommandSourceStack();
        var commands = dispatcher.getSmartUsage(dispatcher.getRoot(), css);

        JsonArray results = new JsonArray();
        for (CommandNode<CommandSourceStack> command : commands.keySet()) {
            var parseResults = dispatcher.parse(command.getName(), css);
            if (!parseResults.getContext().getNodes().isEmpty()) {
                var commandUsages = dispatcher.getSmartUsage(Iterables.getLast(parseResults.getContext().getNodes()).getNode(), css);
                for (String value : commandUsages.values()) {
                    results.add("/" + parseResults.getReader().getString() + " " + value);
                }
            }
        }

        return results;
    }

    private static String transformComponent(RecipeComponent<?> type) {
        if (type.typeInfo() instanceof EnumTypeInfo enumTypeInfo) {
            List<String> enums = new ArrayList<>();
            for (Object object : enumTypeInfo.enumConstants()) {
                enums.add(ProbeJS.GSON.toJson(EnumTypeInfo.getName(object).toLowerCase(Locale.ROOT)));
            }
            return String.join(" | ", enums);
        }
        return type.toString();
    }
}
